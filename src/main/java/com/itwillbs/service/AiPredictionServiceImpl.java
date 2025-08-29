package com.itwillbs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itwillbs.dto.PredictionInputDTO;
import com.itwillbs.dto.PredictionResultDTO;
import com.itwillbs.mapper.WorkOrderQueryMapper;
import com.itwillbs.persistence.AiPredictionDAO;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AiPredictionServiceImpl implements AiPredictionService {
	
	private static final Logger logger = LoggerFactory.getLogger(AiPredictionServiceImpl.class);

    @Inject
    private AiPredictionDAO apDAO;
    
    @Inject
    private WorkOrderQueryMapper woMapper;
    
    @Inject
    private AiPredictionLogService logService;

    @Value("${openai.apiKey:}")
    private String apiKeyProp;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.baseUrl:https://api.openai.com/v1}")
    private String baseUrl;

    private final ObjectMapper om = new ObjectMapper();
    
    
    
    // [변경됨] 기존 메서드는 오버로드 호출
    @Transactional
    @Override
    public PredictionResultDTO predictEtaForWorkOrder(String workOrderId) throws Exception {
        return predictEtaForWorkOrder(workOrderId, null); // ★ 추가된 오버로드 호출
    }

    

    // ★ 신규 오버로드: 세션 사용자 ID를 받아 로그에 남김
    @Transactional
    @Override
    public PredictionResultDTO predictEtaForWorkOrder(String workOrderId, String requestedBy) throws Exception {
        Map<String, Object> wo = apDAO.getWorkOrderSummary(workOrderId);
        if (wo == null) throw new IllegalArgumentException("작업지시서 없음: " + workOrderId);

        final String stage = woMapper.resolveAiStage(workOrderId);

        if ("READY".equals(stage) || "CHECKED_ONLY".equals(stage)) {
            PredictionResultDTO r = new PredictionResultDTO();
            r.setWorkOrderId(workOrderId);
            r.setStage(stage);
            r.setRiskLevel("LOW");
            r.setReason("자재 확보·출고 완료 → 즉시 투입 가능");
            r.setActions(java.util.Arrays.asList("생산 착수"));
            r.setEtaDays(0);
            return logAndReturn("ETA_READY_BYPASS", workOrderId, stage, wo, r, requestedBy);
        }

        String status = Objects.toString(wo.get("status"), "");
        if ("COMPLETED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("이미 완료된 작업지시서입니다. (status=COMPLETED)");
        }

        List<Map<String, Object>> shortages = apDAO.getShortageByMaterial(workOrderId);
        List<String> mats = shortages.stream()
                .map(m -> Objects.toString(m.get("materialId"), null))
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Map<String, Object>> leadStats = apDAO.getLeadTimeStatsByMaterials(mats);

        PredictionInputDTO in = new PredictionInputDTO();
        in.setWorkOrderId(Objects.toString(wo.get("workOrderId"), null));
        in.setProductId(Objects.toString(wo.get("productId"), null));
        in.setDueDate(Objects.toString(wo.get("dueDate"), null));
        in.setOrderQty((Integer) (wo.get("orderQty")));
        in.setShortages(shortages);
        in.setLeadStats(leadStats);

        if ("PO_PLACED".equals(stage)) {
            int baseline = computeBaselineEta(in);
            PredictionResultDTO r = new PredictionResultDTO();
            r.setWorkOrderId(in.getWorkOrderId());
            r.setStage(stage);
            r.setEtaDays(baseline);
            int sc = (int) shortages.stream().filter(m -> toInt(m.get("shortageQty"), 0) > 0).count();
            r.setRiskLevel(sc >= 5 ? "HIGH" : "MEDIUM");
            r.setReason("부족 자재 " + sc + "건, 공급사 리드타임/입고 ETA 반영 예측");
            r.setActions(Arrays.asList("공급사 납기 재확인", "부분입고 시 부분생산 검토", "대체 자재/공급사 검토"));
            return logAndReturn("ETA_PO_PLACED", in.getWorkOrderId(), stage, in, r, requestedBy); // ★
        }

        if ("PLANNED".equals(stage)) {
            int baseline = Math.max(1, Math.min(3, computeBaselineEta(in)));
            PredictionResultDTO r = new PredictionResultDTO();
            r.setWorkOrderId(in.getWorkOrderId());
            r.setStage(stage);
            r.setEtaDays(baseline);
            r.setRiskLevel("MEDIUM");
            r.setReason("초기 베이스라인(재고/라인/표준시간 가정). 부족 검증 전 단계");
            r.setActions(Arrays.asList("부족 검증 실행", "예약/발주 진행", "라인 슬롯 임시 배정"));
            return logAndReturn("ETA_PLANNED", in.getWorkOrderId(), stage, in, r, requestedBy); // ★
        }

        int baselineEta = computeBaselineEta(in);
        try {
            PredictionResultDTO out = callLLM(in, baselineEta);
            out = normalizeLLMResult(out, in, baselineEta);
            out.setStage(stage);
            out.setWorkOrderId(in.getWorkOrderId());

            Map<String,Object> req = new LinkedHashMap<>();
            req.put("baseline_eta", baselineEta);
            req.put("shortage_count", (int) in.getShortages().stream().filter(m -> toInt(m.get("shortageQty"),0)>0).count());
            req.put("product_id", in.getProductId());

            return logAndReturn("ETA_LLM", in.getWorkOrderId(), stage, req, out, requestedBy); // ★
        } catch (Exception e) {
            PredictionResultDTO out = fallbackHeuristic(in);
            out.setStage(stage);
            out.setWorkOrderId(in.getWorkOrderId());

            Map<String,Object> req = new LinkedHashMap<>();
            req.put("baseline_eta", baselineEta);
            req.put("fallback_reason", e.toString());

            return logAndReturn("ETA_FALLBACK", in.getWorkOrderId(), stage, req, out, requestedBy); // ★
        }
    }

    
    
	 // 베이스라인 ETA: 부족 품목 중 리드타임 평균의 최댓값(없으면 최소값)
	 // 최소 ETA: 부족 있으면 3일, 없으면 1일. 상한 60일.
	 private int computeBaselineEta(PredictionInputDTO in) {
	     Set<String> shortageIds = in.getShortages().stream()
	             .filter(m -> toInt(m.get("shortageQty"), 0) > 0)
	             .map(m -> Objects.toString(m.get("materialId"), ""))
	             .collect(Collectors.toSet());
	
	     int minEta = shortageIds.isEmpty() ? 1 : 3;
	
	     double maxAvg = in.getLeadStats().stream()
	             .filter(s -> shortageIds.contains(Objects.toString(s.get("materialId"), "")))
	             .mapToDouble(s -> toDouble(s.get("avgLeadDays"), Double.NaN))
	             .filter(d -> !Double.isNaN(d) && d > 0.0)
	             .max().orElse(Double.NaN);
	
	     int base = Double.isNaN(maxAvg) ? minEta : (int) Math.ceil(maxAvg);
	     return Math.max(minEta, Math.min(60, base));
	 }
	 
	 
	
	 // LLM 결과가 이상하면(예: 730일, 영어) 안전하게 보정
	 private PredictionResultDTO normalizeLLMResult(PredictionResultDTO out, PredictionInputDTO in, int baselineEta) {
	     int shortageCount = (int) in.getShortages().stream().filter(m -> toInt(m.get("shortageQty"), 0) > 0).count();
	     int minEta = shortageCount > 0 ? 3 : 1;
	
	     // ETA 보정: [minEta, 60] 범위로, 벗어나면 baseline으로
	     int eta = out.getEtaDays();
	     if (eta < minEta || eta > 60) {
	         eta = Math.max(minEta, Math.min(60, baselineEta));
	         out.setEtaDays(eta);
	     }
	
	     // 위험도 보정
	     if (!Arrays.asList("LOW","MEDIUM","HIGH").contains(
	             Optional.ofNullable(out.getRiskLevel()).orElse("MEDIUM"))) {
	         out.setRiskLevel(shortageCount >= 5 ? "HIGH" : (shortageCount > 0 ? "MEDIUM" : "LOW"));
	     }
	
	     // 한국어 강제 & 기본 액션 확보
	     if (out.getActions() == null || out.getActions().isEmpty()) {
	         out.setActions(Arrays.asList(
	                 "부족 자재 우선 발주(리드타임 긴 품목부터)",
	                 "대체 공급사/대체 자재 검토",
	                 "생산 일정(라인/순서) 재배치 검토"
	         ));
	     }
	     if (out.getReason() == null || out.getReason().trim().isEmpty()
	         || out.getReason().matches(".*[A-Za-z].*")) { // 영문 위주면 한국어로 교체
	         out.setReason(String.format("부족 자재 %d건 및 과거 리드타임을 고려한 예측 결과입니다.", shortageCount));
	     }
	     return out;
	 }

 
 


    private PredictionResultDTO fallbackHeuristic(PredictionInputDTO in) {
        // 평균 리드타임 + 부족분 존재 여부 기반 간단 추정
        double avg = in.getLeadStats().stream()
                .mapToDouble(m -> toDouble(m.get("avgLeadDays"), 5.0))
                .average().orElse(5.0);
        int shortageCount = (int) in.getShortages().stream()
                .filter(m -> toInt(m.get("shortageQty"), 0) > 0).count();

        int eta = (int) Math.ceil(avg);
        String risk = shortageCount > 0 ? "HIGH" : "LOW";

        PredictionResultDTO out = new PredictionResultDTO();
        out.setWorkOrderId(in.getWorkOrderId());
        out.setEtaDays(eta);
        out.setRiskLevel(risk);
        out.setReason("휴리스틱: 과거 평균 리드타임 " + avg + "일, 부족 자재 " + shortageCount + "건");
        out.setActions(Arrays.asList(
                "부족 자재 우선 발주 및 리드타임 긴 품목 선조치",
                "대체 자재/공급사 검토",
                "생산 일정 재배치 고려"
        ));
        return out;
    }
    
    
    

    private double toDouble(Object o, double def) {
        if (o == null) return def; try { return Double.parseDouble(o.toString()); } catch (Exception e) { return def; }
    }
    
    
    
    private int toInt(Object o, int def) {
        if (o == null) return def; try { return (int) Math.round(Double.parseDouble(o.toString())); } catch (Exception e) { return def; }
    }
    

    private PredictionResultDTO callLLM(PredictionInputDTO in, int baselineEta) throws IOException {
        String apiKey = (apiKeyProp != null && !apiKeyProp.isEmpty()) ? apiKeyProp : System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) throw new IllegalStateException("OPENAI_API_KEY 누락");

        int shortageCount = (int) in.getShortages().stream().filter(m -> toInt(m.get("shortageQty"), 0) > 0).count();

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("work_order", in.getWorkOrderId());
        userPayload.put("product_id", in.getProductId());
        userPayload.put("due_date", in.getDueDate());
        userPayload.put("order_qty", in.getOrderQty());
        userPayload.put("shortages", in.getShortages());
        userPayload.put("lead_stats", in.getLeadStats());
        userPayload.put("baseline_eta_hint_days", baselineEta);
        userPayload.put("shortage_count", shortageCount);

        String instruction =
            "당신은 제조 계획 AI입니다. 주어진 부족 현황과 과거 리드타임 통계를 바탕으로 '오늘 기준' 작업지시서 완료까지 남은 ETA(일)와 " +
            "지연 위험도를 예측하세요. 한국어로만 답해주세요. " +
            "반드시 아래 JSON만 반환하세요: {eta_days:int(1..60), risk_level:\"LOW|MEDIUM|HIGH\", reason:string, actions:string[]} " +
            "규칙: eta_days는 1~60 사이 값으로, baseline_eta_hint_days를 ±20% 내에서 설정하되 명확한 사유가 있으면 벗어날 수 있습니다. " +
            "shortage_count>0이면 eta_days는 최소 3일 이상으로 하세요. 여분 문장/설명은 금지. JSON 외 텍스트 금지.";

        // JSON 모드 + 낮은 temperature로 과장 방지
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("temperature", 0.2);
        bodyMap.put("top_p", 0.1);
        bodyMap.put("response_format", Collections.singletonMap("type", "json_object"));
        bodyMap.put("messages", Arrays.asList(
            new HashMap<String, String>() {{ put("role", "system"); put("content", instruction); }},
            new HashMap<String, String>() {{ put("role", "user"); put("content", om.writeValueAsString(userPayload)); }}
        ));
        String body = om.writeValueAsString(bodyMap);

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("OpenAI API error: " + resp.code());
            JsonNode json = om.readTree(resp.body().string());
            String content = json.path("choices").get(0).path("message").path("content").asText();

            JsonNode data = om.readTree(content); // JSON 모드라 content가 곧 JSON
            PredictionResultDTO out = new PredictionResultDTO();
            out.setWorkOrderId(in.getWorkOrderId());
            out.setEtaDays(data.path("eta_days").asInt(5));
            out.setRiskLevel(data.path("risk_level").asText("MEDIUM"));
            out.setReason(data.path("reason").asText("예측 근거 없음"));
            List<String> actions = new ArrayList<>();
            if (data.path("actions").isArray()) for (JsonNode a : data.path("actions")) actions.add(a.asText());
            out.setActions(actions);
            return out;
        }
    }
    
    
    @Override
    public PredictionResultDTO predict(PredictionInputDTO in) {
        final String orderId = in.getWorkOrderId();
        final String stage   = woMapper.resolveAiStage(orderId); // 이미 추가한 쿼리

        PredictionResultDTO r = new PredictionResultDTO();
        r.setWorkOrderId(orderId);
        r.setStage(stage); // DTO에 stage 필드 추가되어 있어야 함 (String)

        switch (stage) {
            case "READY":
                r.setRiskLevel("LOW");
                r.setReason("자재 확보·출고 완료 → 즉시 투입 가능");
                r.setActions(Arrays.asList("생산 착수"));
                r.setEtaDays(0); // 임시값
                break;

            case "CHECKED_ONLY":
                r.setRiskLevel("LOW");
                r.setReason("부족 없음(창고 확보). 출고처리만 남음");
                r.setActions(Arrays.asList("출고처리 실행", "라인 배정"));
                r.setEtaDays(1); // 임시값
                break;

            case "PO_PLACED":
                r.setRiskLevel("HIGH");
                r.setReason("부족 자재 존재, 발주/입고 대기");
                r.setActions(Arrays.asList("공급사 리마인드", "부분생산 검토"));
                r.setEtaDays(5); // 임시값
                break;

            case "PLANNED":
                r.setRiskLevel("MEDIUM");
                r.setReason("초기 베이스라인(재고/라인/표준시간 기준)");
                r.setActions(Arrays.asList("부족 검증", "예약/발주 진행"));
                r.setEtaDays(3); // 임시값
                break;

            case "IN_PROGRESS":
                r.setRiskLevel("MEDIUM");
                r.setReason("진행률/다운타임 반영(간이 추정)");
                r.setActions(Arrays.asList("자원/교대 조정 검토"));
                r.setEtaDays(1); // 임시값
                break;

            case "COMPLETED":
            default:
                r.setRiskLevel("LOW");
                r.setReason("생산 완료");
                r.setActions(Arrays.asList());
                r.setEtaDays(0);
                break;
        }
        if (!stage.equals(r.getStage())) r.setStage(stage);
        return r;
    }
    
    // === 공통 로그 저장 + 결과 리턴 ===
    private PredictionResultDTO logAndReturn(
            String requestType,       // "ETA_PLANNED" / "ETA_PO_PLACED" / "ETA_LLM" / "ETA_FALLBACK" / "ETA_READY_BYPASS"
            String workOrderId,
            String stage,
            Object inputPayload,      // 요청 요약 또는 in DTO
            PredictionResultDTO out,  // 예측 결과
            String requestedBy        // ★ 추가: 세션 사용자 ID
    ) {
        try {
            String actor = (requestedBy != null && !requestedBy.trim().isEmpty()) ? requestedBy : "SYSTEM";

            var dto = new com.itwillbs.dto.AiPredictionLogDTO();
            dto.setRequestedBy(actor);                 // ★ 이제 세팅됨
            dto.setRequestType(requestType);

            Map<String,Object> wrapper = new LinkedHashMap<>();
            wrapper.put("work_order", workOrderId);
            wrapper.put("stage", stage);
            wrapper.put("payload", inputPayload);
            dto.setInputDataJson(om.writeValueAsString(wrapper));
            dto.setPredictionResultJson(om.writeValueAsString(out));

            logService.log(dto); // (REQUIRES_NEW면 더 좋아요)
            logger.info("[AI-LOG] saved log_id={}", dto.getLogId());
        } catch (Exception e) {
            logger.warn("[AI-LOG] save failed", e); // 실패해도 본 흐름은 유지
        }
        return out;
    }

    
    
    

}
