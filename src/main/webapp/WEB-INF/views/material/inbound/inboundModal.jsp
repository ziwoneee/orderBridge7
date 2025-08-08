<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!-- 입고처리 모달 -->
<div class="modal fade" id="inboundModal" tabindex="-1" role="dialog" aria-labelledby="inboundModalLabel">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <form id="inboundForm">
        <div class="modal-header" style="background-color: #1c355e; color: #ffffff;">
          <h5 class="modal-title">입고처리</h5>
        </div>
        
        <div class="modal-body">
          <!-- 숨은 필드 -->
          <input type="hidden" id="materialId">
          <input type="hidden" id="inboundId">
          <input type="hidden" id="orderItemId" />
          <input type="hidden" id="inboundItemId" />
        
          <!-- 자재명 -->
          <div class="form-group">
            <label>자재명</label>
            <input type="text" class="form-control" id="materialName" readonly>
          </div>

          <!-- LOT 번호 -->
          <div class="form-group">
            <label>LOT 번호 <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="lotNo" name="lotNo" required>
          </div>

          <!-- 유통기한 -->
          <div class="form-group">
            <label>유통기한 <span class="text-danger">*</span></label>
            <input type="date" class="form-control" id="expirationDate" name="expirationDate" required>
          </div>

          <!-- 입고 수량 -->
          <div class="form-group">
            <label>입고 수량 <span class="text-danger">*</span></label>
            <input type="number" class="form-control" id="quantity" name="quantity" min="1" required>
          </div>

          <!-- 창고 선택 -->
          <div class="form-group">
            <label>창고 선택 <span class="text-danger">*</span></label>
            <select class="form-control" id="warehouseCode" name="warehouseCode" required>
              <option value="">-- 창고 선택 --</option>
              <option value="WH001">WH001</option>
              <option value="WH002">WH002</option>
              <option value="WH003">WH003</option>
            </select>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" id="btnSaveInbound">입고처리</button>
        </div>
      </form>
    </div>
  </div>
</div>
