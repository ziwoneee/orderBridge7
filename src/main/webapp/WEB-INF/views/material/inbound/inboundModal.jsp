<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!-- 입고처리 모달 -->
<!-- 미입고 발주 모달 -->
<div class="modal fade" id="unreceivedOrdersModal" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-xl" role="document">
    <div class="modal-content">
      <div class="modal-header" style="background:#1c355e;color:#fff;">
        <h5 class="modal-title">미입고 발주 목록</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>

      <div class="modal-body">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <small class="text-muted">체크 후 “입고등록” 클릭</small>
          <button id="btn-insert-unreceived-modal" class="btn btn-outline-success btn-sm">
            <i class="ti-upload"></i> 선택된 발주 입고등록
          </button>
        </div>

        <div class="table-responsive">
          <table class="table table-hover" id="unreceivedOrderTableModal">
            <thead style="background:#f8f9fa;">
              <tr>
                <th width="40"><input type="checkbox" id="checkAllModal"></th>
                <th>발주관리번호</th>
                <th>품명</th>
                <th>발주수량</th>
                <th>예상입고일</th>
                <th>발주담당자</th>
                <th>상세</th>
              </tr>
            </thead>
            <tbody><!-- JS 로우 주입 --></tbody>
          </table>
        </div>

        <div class="mt-3 d-flex justify-content-center" id="unreceivedPaginationModal"></div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>

<!-- 발주 상세 모달 (재사용) -->
<div class="modal fade" id="orderDetailModal" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">

      <div class="modal-header" style="background:#1c355e;color:#fff;">
        <h5 class="modal-title">발주 상세</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>

      <div class="modal-body">
        <!-- 기본 정보 -->
        <table class="table table-bordered">
          <tbody>
            <tr>
              <th>발주번호</th><td id="modalOrderId"></td>
              <th>거래처</th><td id="modalSupplierId"></td>
            </tr>
            <tr>
              <th>발주일</th><td id="modalOrderDate"></td>
              <th>예상입고일</th><td id="modalExpectedDate"></td>
            </tr>
            <tr>
              <th>발주상태</th><td id="modalOrderStatus"></td>
              <th>담당자</th><td id="modalCreatedBy"></td>
            </tr>
            <tr>
              <th>비고</th><td colspan="3" id="modalNote"></td>
            </tr>
          </tbody>
        </table>

        <!-- 상세 항목 -->
        <h6 class="mt-4">발주 상세 항목</h6>
        <table class="table table-bordered text-center">
          <thead>
            <tr>
              <th>자재ID</th>
              <th>품명</th>
              <th>수량</th>
              <th>단가</th>
              <th>총금액</th>
              <th>입고창고</th>
            </tr>
          </thead>
          <tbody id="orderItemsInfo"></tbody>
        </table>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>

    </div>
  </div>
</div>
