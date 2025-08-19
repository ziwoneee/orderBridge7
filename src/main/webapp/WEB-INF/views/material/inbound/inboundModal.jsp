<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
	<!-- 미입고 발주 목록 보기 모달 -->
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
	
	
	
	
	<!-- 입고 처리 모달 -->
	<div class="modal fade" id="inboundModal" tabindex="-1" role="dialog" aria-hidden="true">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header" style="background:#1c355e;color:#fff;">
	        <h5 class="modal-title">입고처리</h5>
	        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
	      </div>
	      <div class="modal-body">
	        <input type="hidden" id="inboundId">
	        <input type="hidden" id="orderItemId">
	        <input type="hidden" id="inboundItemId">
	
	        <div class="form-group">
	          <label>자재</label>
	          <input id="materialName" class="form-control" readonly>
	          <input id="materialId" type="hidden">
	        </div>
	
	        <div class="form-group">
	          <label>LOT No</label>
	          <input id="lotNo" class="form-control">
	        </div>
	
	        <div class="form-group">
	          <label>유통기한</label>
	          <input id="expirationDate" type="date" class="form-control">
	        </div>
	
	        <div class="form-group">
	          <label>수량</label>
	          <input id="quantity" type="number" min="1" class="form-control">
	        </div>
	
	        <div class="form-group">
	          <label>창고</label>
	          <input id="warehouseCode" class="form-control" value="WH001">
	        </div>
	      </div>
	      <div class="modal-footer">
	        <button id="btnSaveInbound" type="button" class="btn btn-success">저장</button>
	        <button class="btn btn-secondary" data-dismiss="modal">닫기</button>
	      </div>
	    </div>
	  </div>
	</div>
	
	
	
	<!-- 입고관리 상세 모달 -->
	<div class="modal fade" id="inboundDetailModal" tabindex="-1" role="dialog" aria-labelledby="modalTitle" aria-hidden="true">
	  <div class="modal-dialog modal-lg" role="document">
	    <div class="modal-content">
	
	      <div class="modal-header" style="background-color: #1c355e; color: #ffffff;">
	        <h5 class="modal-title">입고관리 상세</h5>
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
	          <span aria-hidden="true">&times;</span>
	        </button>
	      </div>
	
	      <div class="modal-body">
	        <!-- 기본 정보 -->
	        <table class="table table-bordered">
	          <tbody>
	            <tr>
	              <th class="bg-light">발주관리번호</th>
	              <td id="orderId"></td>
	              <th class="bg-light">예상입고일</th>
	              <td id="expectedArrivedDate"></td>
	            </tr>
	            <tr>
	              <th class="bg-light">발주일자</th>
	              <td id="orderDate"></td>
	              <th class="bg-light">거래처</th>
			              <td id="supplierName"></td>
	            </tr>
	            <tr>
	              <th class="bg-light">입고관리번호</th>
	              <td id="inboundId"></td>
	              <th class="bg-light">입고진행현황</th>
	              <td id="modalStatus"></td>
	            </tr>
	            <tr>
	              <th class="bg-light">입고일자</th>
	              <td id="inboundDate"></td>
	              <th class="bg-light">입고담당자</th>
	              <td id="handledBy"></td>
	            </tr>
	          </tbody>
	        </table>
	
	        <!-- 자재 입고 정보 -->
	        <h6 class="mt-4">자재 입고 정보</h6>
	         <table class="table table-bordered text-center">
	          <thead style="background-color: #1C355E; color: white;">
	            <tr>
	              <th>품목코드</th>
	              <th>품명</th>
	              <th>발주수량</th>
	              <th>입고수량</th>
	              <th>처리</th>
	            </tr>
	          </thead>
	          <tbody id="inboundInfo">
	            <!-- JS로 추가 -->
	          </tbody>
	        </table>
	      </div>
	
	      <div class="modal-footer">
	        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
	      </div>
	
	    </div>
	  </div>
	</div>
