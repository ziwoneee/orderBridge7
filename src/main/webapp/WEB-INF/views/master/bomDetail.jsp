<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script>
        // 수정모드 전환
        function enableEdit(rowId) {
            var tr = document.getElementById("row-" + rowId);
            tr.querySelectorAll(".view-cell").forEach(function(td) { td.style.display = "none"; });
            tr.querySelectorAll(".edit-cell").forEach(function(td) { td.style.display = ""; });
            document.getElementById("edit-btn-" + rowId).style.display = "none";
            document.getElementById("save-btn-" + rowId).style.display = "";
            document.getElementById("cancel-btn-" + rowId).style.display = "";
        }
        // 수정취소
        function cancelEdit(rowId) {
            var tr = document.getElementById("row-" + rowId);
            tr.querySelectorAll(".view-cell").forEach(function(td) { td.style.display = ""; });
            tr.querySelectorAll(".edit-cell").forEach(function(td) { td.style.display = "none"; });
            document.getElementById("edit-btn-" + rowId).style.display = "";
            document.getElementById("save-btn-" + rowId).style.display = "none";
            document.getElementById("cancel-btn-" + rowId).style.display = "none";
        }
        // 저장시 input 값 hidden에 복사 후 submit
        function submitEdit(rowId) {
            // 수정 input값 가져오기
            var qty = document.getElementById("edit-qty-" + rowId).value;
            var unit = document.getElementById("edit-unit-" + rowId).value;
            var materialName = document.getElementById("edit-materialName-" + rowId).value;

            // hidden input에 복사
            var form = document.getElementById("form-" + rowId);
            form.querySelector('input[name="qty"]').value = qty;
            form.querySelector('input[name="unit"]').value = unit;
            form.querySelector('input[name="materialName"]').value = materialName;

            form.submit();
        }
    </script>
    
    <script>
function saveStatus(bomId) {
    var status = document.getElementById('statusSelect').value;
    fetch('${pageContext.request.contextPath}/master/bom/updateStatus', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'bomId=' + encodeURIComponent(bomId) + '&status=' + encodeURIComponent(status)
    })
    .then(res => res.text())
    .then(data => {
        if(data.trim() === 'success') {
            alert('상태가 저장되었습니다!');
            // 필요시 location.reload(); 또는 상태 값만 화면에서 갱신
        } else {
            alert('상태 저장에 실패했습니다!');
        }
    })
    .catch(e => {
        alert('요청 중 오류가 발생했습니다!');
    });
}
</script>
<!-- 헤더, 사이드바,개인설정 끝 -->
  <div class="main-panel">
        <div class="content-wrapper">
            <div class="row">
            
            
            
          <div class="col-12 mb-4">
			  <h3 class="font-weight-bold">BOM 정보</h3>
			</div>

  
 <table class="table table-bordered text-center align-middle">
  <thead>
    <tr style="background-color:#1c355e; color:white;">
      <th>BOM ID</th>
      <th>대상제품</th>
      <th>BOM명</th>
      <th>상태</th>
      <th>비고</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>${bomMaster.bomId}</td>
      <td>${bomMaster.productName}</td>
      <td>${bomMaster.bomName}</td>
      <td>
        <div class="d-flex justify-content-center">
          <div class="form-inline">
            <select id="statusSelect" class="form-control form-control-sm mr-2" style="width:auto;">
              <option value="ACTIVE" <c:if test="${bomMaster.status eq 'ACTIVE'}">selected</c:if>>활성</option>
              <option value="INACTIVE" <c:if test="${bomMaster.status eq 'INACTIVE'}">selected</c:if>>비활성</option>
            </select>
            <button id="statusSaveBtn" class="btn btn-success btn-sm" 
                    type="button" onclick="saveStatus('${bomMaster.bomId}')">저장</button>
          </div>
        </div>
      </td>
      <td>${bomMaster.bomEtc}</td>
    </tr>
  </tbody>
</table>

    <!-- 육수용 원자재 테이블 -->
    <h5 class="mt-4">육수용 원자재</h5>
    <table class="table table-bordered">
    <colgroup>
  <col style="width:25%">
  <col style="width:25%">
  <col style="width:25%">
  <col style="width:25%">
</colgroup>
        <thead>
        <tr>
            <th>원자재명</th>
            <th>소요량</th>
            <th>단위</th>
            <th>관리</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="d" items="${soupList}">
            <tr id="row-${d.bomDetailId}">
                <!-- 원자재명 -->
                <td>
                    <span class="view-cell">${d.materialName}</span>
                    <input id="edit-materialName-${d.bomDetailId}" class="edit-cell form-control" type="text" value="${d.materialName}" style="display:none;">
                </td>
                <!-- 소요량 -->
                <td>
                    <span class="view-cell">${d.qty}</span>
                    <input id="edit-qty-${d.bomDetailId}" class="edit-cell form-control" type="number" value="${d.qty}" style="display:none;">
                </td>
                <!-- 단위 -->
                <td>
                    <span class="view-cell">${d.unit}</span>
                    <input id="edit-unit-${d.bomDetailId}" class="edit-cell form-control" type="text" value="${d.unit}" style="display:none;">
                </td>
                <!-- 관리 -->
                <td>
                    <button id="edit-btn-${d.bomDetailId}" class="btn btn-warning btn-sm" type="button" onclick="enableEdit('${d.bomDetailId}')">수정</button>
                    <form id="form-${d.bomDetailId}" method="post" action="${pageContext.request.contextPath}/master/bom/detail/update" style="display:inline;">
                        <input type="hidden" name="bomDetailId" value="${d.bomDetailId}">
                        <input type="hidden" name="bomId" value="${bomMaster.bomId}">
                        <input type="hidden" name="materialId" value="${d.materialId}">
                        <input type="hidden" name="materialType" value="${d.materialType}">
                        <input type="hidden" name="materialName" value="${d.materialName}">
                        <input type="hidden" name="qty" value="${d.qty}">
                        <input type="hidden" name="unit" value="${d.unit}">
                        <button id="save-btn-${d.bomDetailId}" type="button" class="btn btn-success btn-sm edit-cell" style="display:none;" onclick="submitEdit('${d.bomDetailId}')">저장</button>
                        <button id="cancel-btn-${d.bomDetailId}" type="button" class="btn btn-secondary btn-sm edit-cell" onclick="cancelEdit('${d.bomDetailId}')" style="display:none;">취소</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/master/bom/detail/delete" style="display:inline;" onsubmit="return confirm('삭제할까요?');">
                        <input type="hidden" name="bomDetailId" value="${d.bomDetailId}">
                        <input type="hidden" name="bomId" value="${bomMaster.bomId}">
                        <button type="submit" class="btn btn-danger btn-sm">삭제</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <!-- 원료용 원자재 테이블 -->
    <h5 class="mt-4">원료용 원자재</h5>
    <table class="table table-bordered">
    <colgroup>
  <col style="width:25%">
  <col style="width:25%">
  <col style="width:25%">
  <col style="width:25%">
</colgroup>
        <thead>
        <tr>
            <th>원자재명</th>
            <th>소요량</th>
            <th>단위</th>
            <th>관리</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="d" items="${solidList}">
            <tr id="row-${d.bomDetailId}">
                <td>
                    <span class="view-cell">${d.materialName}</span>
                    <input id="edit-materialName-${d.bomDetailId}" class="edit-cell form-control" type="text" value="${d.materialName}" style="display:none;">
                </td>
                <td>
                    <span class="view-cell">${d.qty}</span>
                    <input id="edit-qty-${d.bomDetailId}" class="edit-cell form-control" type="number" value="${d.qty}" style="display:none;">
                </td>
                <td>
                    <span class="view-cell">${d.unit}</span>
                    <input id="edit-unit-${d.bomDetailId}" class="edit-cell form-control" type="text" value="${d.unit}" style="display:none;">
                </td>
                <td>
                    <button id="edit-btn-${d.bomDetailId}" class="btn btn-warning btn-sm" type="button" onclick="enableEdit('${d.bomDetailId}')">수정</button>
                    <form id="form-${d.bomDetailId}" method="post" action="${pageContext.request.contextPath}/master/bom/detail/update" style="display:inline;">
                        <input type="hidden" name="bomDetailId" value="${d.bomDetailId}">
                        <input type="hidden" name="bomId" value="${bomMaster.bomId}">
                        <input type="hidden" name="materialId" value="${d.materialId}">
                        <input type="hidden" name="materialType" value="${d.materialType}">
                        <input type="hidden" name="materialName" value="${d.materialName}">
                        <input type="hidden" name="qty" value="${d.qty}">
                        <input type="hidden" name="unit" value="${d.unit}">
                        <button id="save-btn-${d.bomDetailId}" type="button" class="btn btn-success btn-sm edit-cell" style="display:none;" onclick="submitEdit('${d.bomDetailId}')">저장</button>
                        <button id="cancel-btn-${d.bomDetailId}" type="button" class="btn btn-secondary btn-sm edit-cell" onclick="cancelEdit('${d.bomDetailId}')" style="display:none;">취소</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/master/bom/detail/delete" style="display:inline;" onsubmit="return confirm('삭제할까요?');">
                        <input type="hidden" name="bomDetailId" value="${d.bomDetailId}">
                        <input type="hidden" name="bomId" value="${bomMaster.bomId}">
                        <button type="submit" class="btn btn-danger btn-sm">삭제</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
  
  <!-- 포장재 테이블 -->
<h5 class="mt-4">포장재</h5>
<table class="table table-bordered">
<colgroup>
  <col style="width:25%">
  <col style="width:25%">
  <col style="width:25%">
  <col style="width:25%">
</colgroup>

    <thead>
    <tr>
        <th>원자재명</th>
        <th>소요량</th>
        <th>단위</th>
        <th>관리</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="d" items="${packagingList}">
        <tr id="row-${d.bomDetailId}">
            <td>
                <span class="view-cell">${d.materialName}</span>
                <input id="edit-materialName-${d.bomDetailId}" class="edit-cell form-control" type="text" value="${d.materialName}" style="display:none;">
            </td>
            <td>
                <span class="view-cell">${d.qty}</span>
                <input id="edit-qty-${d.bomDetailId}" class="edit-cell form-control" type="number" value="${d.qty}" style="display:none;">
            </td>
            <td>
                <span class="view-cell">${d.unit}</span>
                <input id="edit-unit-${d.bomDetailId}" class="edit-cell form-control" type="text" value="${d.unit}" style="display:none;">
            </td>
            <td>
                <button id="edit-btn-${d.bomDetailId}" class="btn btn-warning btn-sm" type="button" onclick="enableEdit('${d.bomDetailId}')">수정</button>
                <form id="form-${d.bomDetailId}" method="post" action="${pageContext.request.contextPath}/master/bom/detail/update" style="display:inline;">
                    <input type="hidden" name="bomDetailId" value="${d.bomDetailId}">
                    <input type="hidden" name="bomId" value="${bomMaster.bomId}">
                    <input type="hidden" name="materialId" value="${d.materialId}">
                    <input type="hidden" name="materialType" value="${d.materialType}">
                    <input type="hidden" name="materialName" value="${d.materialName}">
                    <input type="hidden" name="qty" value="${d.qty}">
                    <input type="hidden" name="unit" value="${d.unit}">
                    <button id="save-btn-${d.bomDetailId}" type="button" class="btn btn-success btn-sm edit-cell" style="display:none;" onclick="submitEdit('${d.bomDetailId}')">저장</button>
                    <button id="cancel-btn-${d.bomDetailId}" type="button" class="btn btn-secondary btn-sm edit-cell" onclick="cancelEdit('${d.bomDetailId}')" style="display:none;">취소</button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/master/bom/detail/delete" style="display:inline;" onsubmit="return confirm('삭제할까요?');">
                    <input type="hidden" name="bomDetailId" value="${d.bomDetailId}">
                    <input type="hidden" name="bomId" value="${bomMaster.bomId}">
                    <button type="submit" class="btn btn-danger btn-sm">삭제</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
  


    <a href="${pageContext.request.contextPath}/master/bom/list" class="btn btn-secondary mt-3">목록</a>
 
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