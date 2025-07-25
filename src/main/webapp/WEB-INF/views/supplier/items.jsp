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
        <div class="row">
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">공급 품목 목록</h3>
            <p class="text-muted">해당 거래처의 공급 가능한 자재 목록을 확인하세요.</p>
          </div>

          <!-- 등록 버튼 -->
          <div class="col-12 mb-3 text-right">
            <button class="btn btn-primary" id="btnAddItem">공급 품목 등록</button>
          </div>

          <!-- 공급 품목 테이블 -->
          <div class="col-12">
            <div class="card">
              <div class="card-body">
                <div class="table-responsive">
                  <table class="table table-hover" id="itemTable">
                    <thead class="thead-light">
                      <tr>
                        <th>자재명</th>
                        <th>유형</th>
                        <th>단가</th>
                        <th>단위</th>
                        <th>공급 가능</th>
                        <th>비고</th>
                        <th>작업</th>
                      </tr>
                    </thead>
                    <tbody>
                      <!-- JavaScript로 동적 로딩 -->
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

        </div>
        </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   

<!-- 등록/수정 모달 -->
<div class="modal fade" id="itemModal" tabindex="-1" role="dialog" aria-labelledby="itemModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="itemModalLabel">공급 품목 등록/수정</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <form id="itemForm">
          <input type="hidden" name="id" id="itemId">
          <input type="hidden" name="supplierId" id="supplierId" value="${supplierId}">

          <div class="form-group">
            <label for="materialId">자재 ID</label>
            <input type="text" class="form-control" id="materialId" name="materialId" required>
          </div>
          <div class="form-group">
            <label for="unitPrice">단가</label>
            <input type="number" class="form-control" id="unitPrice" name="unitPrice" required>
          </div>
          <div class="form-group">
            <label for="unit">단위</label>
            <input type="text" class="form-control" id="unit" name="unit" required>
          </div>
          <div class="form-group">
            <label for="supplyAvailable">공급 가능 여부</label>
            <select class="form-control" id="supplyAvailable" name="supplyAvailable">
              <option value="Y">Y</option>
              <option value="N">N</option>
            </select>
          </div>
          <div class="form-group">
            <label for="note">비고</label>
            <textarea class="form-control" id="note" name="note" rows="3"></textarea>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
        <button type="submit" class="btn btn-primary" id="saveItemBtn">저장</button>
      </div>
    </div>
  </div>
</div>

<script>
$(document).ready(function() {
  const supplierId = "${supplierId}";

  // 공급 품목 목록 불러오기
  function loadItems() {
    $.getJSON("/supplierItem/list", { supplierId: supplierId }, function(data) {
      const tbody = $("#itemTable tbody");
      tbody.empty();
      if (data.length === 0) {
        tbody.append("<tr><td colspan='7' class='text-center'>등록된 공급 품목이 없습니다.</td></tr>");
        return;
      }
      $.each(data, function(index, item) {
        const row = `
          <tr>
            <td>${item.materialName}</td>
            <td>${item.materialType}</td>
            <td>${item.unitPrice}</td>
            <td>${item.unit}</td>
            <td>${item.supplyAvailable == 'Y' ? '가능' : '불가'}</td>
            <td>${item.note || ''}</td>
            <td>
              <button class='btn btn-sm btn-warning'>수정</button>
              <button class='btn btn-sm btn-danger'>삭제</button>
            </td>
          </tr>
        `;
        tbody.append(row);
      });
    });
  }

  // 페이지 로드 시 실행
  loadItems();

  // 등록 버튼 클릭 시 모달 열기
  $("#btnAddItem").click(function() {
    $("#itemForm")[0].reset();
    $("#itemModal").modal("show");
  });
});
</script>