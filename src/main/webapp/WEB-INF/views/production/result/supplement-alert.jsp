<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>

  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">

        <!-- 제목 -->
        <div class="mb-4">
          <h3 class="mb-0">보완생산 알림</h3>
        </div>

        <!-- 알림 카드 -->
        <div class="card" style="border-left: 4px solid #ffc107;">
          <div class="card-body">
            <div class="d-flex align-items-center mb-3">
              <i class="ti-alert text-warning" style="font-size: 2rem; margin-right: 15px;"></i>
              <div>
                <h4 class="mb-1 text-warning">보완생산이 필요합니다</h4>
                <p class="mb-0 text-muted">계획수량에 미달하여 추가 생산이 필요합니다.</p>
              </div>
            </div>

            <!-- 작업지시 정보 -->
            <div class="row mb-4">
              <div class="col-md-6">
                <div class="info-item">
                  <label class="info-label">작업지시번호</label>
                  <div class="info-value">${workOrder.orderId}</div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="info-item">
                  <label class="info-label">제품명</label>
                  <div class="info-value">${workOrder.productName}</div>
                </div>
              </div>
            </div>

            <div class="row mb-4">
              <div class="col-md-4">
                <div class="info-item">
                  <label class="info-label">계획수량</label>
                  <div class="info-value">
                    <fmt:formatNumber value="${workOrder.orderQty}" pattern="#,###" />개
                  </div>
                </div>
              </div>
              <div class="col-md-4">
                <div class="info-item">
                  <label class="info-label">현재 양품수량</label>
                  <div class="info-value">
                    <fmt:formatNumber value="${workOrder.orderQty - shortageQty}" pattern="#,###" />개
                  </div>
                </div>
              </div>
              <div class="col-md-4">
                <div class="info-item">
                  <label class="info-label" style="color: #dc3545;">부족수량</label>
                  <div class="info-value" style="color: #dc3545; font-weight: bold; font-size: 1.2em;">
                    <fmt:formatNumber value="${shortageQty}" pattern="#,###" />개
                  </div>
                </div>
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="d-flex justify-content-center gap-3">
              <button type="button" class="btn btn-warning" onclick="startSupplementProduction()">
                <i class="ti-plus"></i> 보완생산 시작
              </button>
              <a href="/production/result/list" class="btn btn-secondary">
                <i class="ti-list"></i> 목록으로 돌아가기
              </a>
            </div>
          </div>
        </div>

        <!-- 추가 정보 카드 -->
        <div class="card mt-4">
          <div class="card-header">
            <h5 class="mb-0">보완생산 안내</h5>
          </div>
          <div class="card-body">
            <div class="alert alert-info">
              <h6><i class="ti-info"></i> 보완생산 절차</h6>
              <ol class="mb-0">
                <li><strong>보완생산 시작</strong> 버튼을 클릭하여 추가 생산을 진행하세요.</li>
                <li>기존 작업지시(${orderId})를 선택하여 부족수량만큼 생산하세요.</li>
                <li>원자재 추가 소모량을 확인하고 재고를 준비하세요.</li>
                <li>생산 완료 후 실적을 등록하면 자동으로 완료 처리됩니다.</li>
              </ol>
            </div>
          </div>
        </div>

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
    <!-- 본문 끝 -->
  </div>
</div>

<style>
.info-item {
  margin-bottom: 15px;
}

.info-label {
  font-size: 0.9rem;
  color: #6c757d;
  font-weight: 500;
  margin-bottom: 5px;
  display: block;
}

.info-value {
  font-size: 1rem;
  color: #495057;
  font-weight: 600;
}

.card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.gap-3 {
  gap: 1rem;
}
</style>

<script>
function startSupplementProduction() {
  if (confirm('보완생산을 시작하시겠습니까?\n\n기존 작업지시를 이용하여 부족수량 ${shortageQty}개를 추가 생산합니다.')) {
    // 생산실적 등록 페이지로 이동 (작업지시 미리 선택된 상태)
    location.href = '/production/result/form?preselect=${orderId}';
  }
}
</script>