package com.itwillbs.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.ClientService;

@Controller
public class ClientController {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Inject
    private ClientService clientService;
    
    //고객사 목록 보기
 // 고객사 목록 페이지
    @GetMapping("/client/list")
    public String listClients(SearchCriteria cri, Model model) {

        // ✅ 허용 정렬 컬럼 목록
        List<String> allowed = List.of("clientName", "businessNumber", "createdAt", "statusCode");

        // ✅ 정렬 컬럼 유효성 검사 및 기본값 설정
        if (cri.getSortColumn() == null) {
            cri.setSortColumn("createdAt");
        }

        // ✅ 정렬 방향 유효성 검사 및 소문자 변환
        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc");
        } else {
            cri.setSortOrder(cri.getSortOrder().toLowerCase());
        }

        // ✅ 로그 출력 (정렬 정보 확인용)
        logger.info("▶ [고객사 목록] 정렬 컬럼: {}", cri.getSortColumn());
        logger.info("▶ [고객사 목록] 정렬 방향: {}", cri.getSortOrder());

        // ✅ 전체 건수 조회 및 페이지 계산
        int totalCount = clientService.getClientCount(cri);
        cri.setTotalCount(totalCount); // PageMaker 내부 계산에 사용 가능

        PageMaker pageMaker = new PageMaker(cri, totalCount);

        // ✅ 실제 고객사 리스트 조회
        List<ClientVO> clientList = clientService.getClientList(cri);

        // ✅ 모델에 데이터 바인딩
        model.addAttribute("clientList", clientList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri); // 검색 조건 유지용
        

        // ✅ 사이드바 메뉴 활성화용
        model.addAttribute("menu", "basic");
 

        return "client/list";
    }


                
    
    
    //고객사 신규 등록하기
        @GetMapping("/client/register")
        public String registerForm(Model model) {
            model.addAttribute("client", new ClientVO());
            model.addAttribute("menu", "basic");
            return "client/register"; // → /WEB-INF/views/client/register.jsp
        }

        @PostMapping("/client/register")
        public String registerPost(ClientVO client, Model model) {
            try {
                // 고객사 ID 생성
                String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
                String uuidPart = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 3); 
                String clientId = "CLIENT-" + datePart + uuidPart;

                client.setClientId(clientId);
                
                // 등록 시도
                clientService.registerClient(client);
                return "redirect:/client/list";

            } catch (org.springframework.dao.DuplicateKeyException e) {
                logger.warn("중복된 사업자등록번호: {}", client.getBusinessNumber());

                model.addAttribute("error", "중복된 사업자등록번호입니다. 다시 확인해주세요.");
                model.addAttribute("client", client);  // 기존 입력값 유지
                model.addAttribute("menu", "basic");
                return "client/register"; // ★ 리다이렉트(X) → 뷰 포워딩(O)
                

            } catch (Exception e) {
                logger.error("고객사 등록 중 오류 발생", e);

                model.addAttribute("error", "등록 중 오류가 발생했습니다. 관리자에게 문의하세요.");
                model.addAttribute("client", client); // 기존 입력값 유지
                return "client/register";
            }
        }

    
        
     // 상세보기 이동
        @GetMapping("/client/detail")
        public String clientDetail(@RequestParam("clientId") String clientId, Model model) {
            ClientVO client = clientService.getClientById(clientId);
            model.addAttribute("client", client);
            model.addAttribute("menu", "basic");
            return "client/detail"; // 예: /WEB-INF/views/client/client_detail.jsp
        }

     // ✅ 수정 폼 이동 처리
        @GetMapping("/client/edit")
        public String editClientForm(@RequestParam("clientId") String clientId, Model model) {
            ClientVO client = clientService.getClientById(clientId);
            model.addAttribute("client", client);
            model.addAttribute("menu", "basic");
            return "client/edit"; // /WEB-INF/views/client/edit.jsp
        }
   
        
     // 고객사 수정 처리
        @PostMapping("/client/update")
        public String updateClient(ClientVO client, RedirectAttributes rttr) {
            try {
                clientService.updateClient(client);
                rttr.addFlashAttribute("success", "수정이 완료되었습니다.");
                return "redirect:/client/detail?clientId=" + client.getClientId();
            } catch (Exception e) {
                logger.error("고객사 수정 중 오류 발생", e);
                rttr.addFlashAttribute("error", "수정 중 오류가 발생했습니다.");
                return "redirect:/client/edit?clientId=" + client.getClientId();
            }
        }
        

     // 사업자번호 중복확인
        @GetMapping("/client/checkBizNo")
        @ResponseBody
        public Map<String, Boolean> checkBusinessNumber(@RequestParam("businessNumber") String businessNumber)
                throws Exception {
            boolean exists = clientService.isBusinessNumberExists(businessNumber);
            return Collections.singletonMap("exists", exists);
        }


        
        
        
}
