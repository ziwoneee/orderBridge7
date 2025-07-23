<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">

  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">

    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
      
    <style>
        .table th, .table td { vertical-align: middle; }
        .table thead th { background-color: #f5f5f5; }
        .input-inline { width: 100%; }
    </style>
    <script>
        function showInsertRow() {
            document.getElementById('insert-row').style.display = '';
            document.getElementById('insert-btn').style.display = 'none';
        }
        function cancelInsert() {
            document.getElementById('insert-row').style.display = 'none';
            document.getElementById('insert-btn').style.display = '';
            // 입력값 초기화는 JS로 하셔도 되고, 그냥 리로드로 처리해도 무방
        }
        function editRow(productId) {
            document.querySelectorAll('.edit-row').forEach(function(tr) { tr.style.display = 'none'; });
            document.querySelectorAll('.display-row').forEach(function(tr) { tr.style.display = ''; });
            document.getElementById('display-' + productId).style.display = 'none';
            document.getElementById('edit-' + productId).style.display = '';
        }
        function cancelEdit(productId) {
            document.getElementById('display-' + productId).style.display = '';
            document.getElementById('edit-' + productId).style.display = 'none';
        }
    </script>

	<!-- 본문 시작 -->
      <div class="main-panel">
        <div class="content-wrapper">
          <div class="row">
          
          
                <div class="col-md-12 grid-margin">
                    <div class="col-12 col-xl-8 mb-4 mb-xl-0">
    
<div class="container mt-5 ">
    <h2 class="mb-4">완제품 목록</h2>
    <button id="insert-btn" type="button" class="btn btn-success mb-2" onclick="showInsertRow()">신규등록</button>
    <table class="table table-bordered table-hover" >
        <thead>
            <tr>
                <th  style="width: 10%;">제품코드</th>
                <th  style="width: 20%;">제품명</th>
                <th  style="width: 20%;">판매가</th>
                <th  style="width: 10%;">단위</th>
                <th  style="width: 10%;">최소주문수량</th>
                <th  style="width: 15%;">보관방법</th>
                <th  style="width: 10%;">유통기한</th>
                <th  style="width: 10%;">레시피코드</th>
                <th  style="width: 18%;">제품상태</th>
                <th  style="width: 10%;">등록일자</th>
                <th  style="width: 10%;">수정일자</th>
                <th  style="width: 10%;">관리</th>
<!--                 <th>삭제</th> -->
            </tr>
        </thead>
        <tbody>
            <!-- 신규등록 입력행 (기본은 숨김) -->
            <tr id="insert-row" style="display:none;">
    <form method="post" action="${pageContext.request.contextPath}/master/product/insert" onsubmit="return validateForm();">
        <td>
    <input type="text" class="form-control input-inline" name="productId" value="자동생성" readonly style="background:#f2f2f2;"/>
</td>
        <td>
            <input type="text" class="form-control input-inline" name="productName" id="productName" required />
        </td>
                <td>
            <input type="number" class="form-control input-inline" name="unitPrice" required />
        </td>
        <td>
    <input type="text" class="form-control input-inline" name="unit" value="팩" readonly />
</td>
        <td>
            <input type="number" class="form-control input-inline" name="minOrderQty" required min="10" step="10" value="10" />
        </td>
        <td>
            <select class="form-control input-inline" name="storageMethod" onchange="console.log(this.value)" required>
                <option value="">선택</option>
                <option value="냉장">냉장</option>
                <option value="냉동">냉동</option>
                <option value="상온">상온</option>
            </select>
        </td>
        <td>
            <select class="form-control input-inline" name="expirationType" required>
                <option value="">선택</option>
                <option value="30일">30일</option>
                <option value="6개월">6개월</option>
                <option value="1년">1년</option>
                <!-- 필요에 따라 추가 -->
            </select>
        </td>
        <td>
            <input type="text" class="form-control input-inline" name="recipeCode" id="recipeCode" required />
        </td>
        <td>
            <select class="form-control input-inline" name="productStatus">
                <option value="생산">생산</option>
                <option value="중지">중지</option>
            </select>
        </td>
        <td>-</td>
        <td>-</td>
        <td>
            <button type="submit" class="btn btn-success btn-sm">등록</button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="cancelInsert()">취소</button>
        </td>
    </form>
</tr>

            
            <!-- 기존 목록 표시, 인라인 수정 가능 -->
       <c:forEach var="product" items="${productList}">
    <tr class="display-row" id="display-${product.productId}"
        <c:if test="${product.deleteYn eq 'Y' || product.productStatus eq '중지'}"> style="color: #bbb; background: #f9f9f9; text-decoration:line-through;" </c:if>
    >
        <td>${product.productId}</td>
        <td>
            ${product.productName}
            <c:if test="${product.deleteYn eq 'Y'}">
                <span class="badge badge-secondary ml-1">(삭제됨)</span>
            </c:if>
        </td>
       <td><fmt:formatNumber value="${product.unitPrice}" type="number"/>원</td>
        <td>${product.unit}</td>
        <td>${product.minOrderQty}</td>
        <td>${product.storageMethod}</td>
        <td>${product.expirationType}</td>
        <td>${product.recipeCode}</td>
        <td>${product.productStatus}</td>
        <td><fmt:formatDate value="${product.regDate}" pattern="yyyy-MM-dd"/></td>
        <td><fmt:formatDate value="${product.updDate}" pattern="yyyy-MM-dd"/></td>
        <td>
            <button type="button" class="btn btn-primary btn-sm"
                onclick="editRow('${product.productId}')"
                <c:if test="${product.deleteYn eq 'Y'}"> disabled</c:if>
            >수정</button>
        </td>
<!--         <td> -->
<%--             <c:choose> --%>
<%--                 <c:when test="${product.deleteYn eq 'Y'}"> --%>
<!--                     복구 버튼 -->
<%--                     <form method="post" action="${pageContext.request.contextPath}/master/product/restore" style="display:inline;"> --%>
<%--                         <input type="hidden" name="productId" value="${product.productId}" /> --%>
<!--                         <button type="submit" class="btn btn-warning btn-sm" -->
<!--                             onclick="return confirm('이 제품을 복구하시겠습니까?');" -->
<!--                         >복구</button> -->
<!--                     </form> -->
<%--                 </c:when> --%>
<%--                 <c:otherwise> --%>
<!--                     삭제 버튼 -->
<!--                     <button type="button" class="btn btn-danger btn-sm" -->
<%--                         onclick="confirmDelete('${product.productId}')">삭제</button> --%>
<%--                 </c:otherwise> --%>
<%--             </c:choose> --%>
<!--         </td> -->
    </tr>
                <!-- 인라인 수정 행 -->
                <tr class="edit-row" id="edit-${product.productId}" style="display:none;">
                    <form method="post" action="${pageContext.request.contextPath}/master/product/update">
                        <input type="hidden" name="productId" value="${product.productId}"/>
                        <td>${product.productId}</td>
                        <td><input type="text" class="form-control input-inline" name="productName" value="${product.productName}"/></td>
                        <td><input type="number" class="form-control input-inline" name="unitPrice" value="${product.unitPrice}"/></td>
                        <td><input type="text" class="form-control input-inline" name="unit" value="${product.unit}"/></td>
                       <td><input type="number" class="form-control input-inline" name="minOrderQty" value="${product.minOrderQty}" min="10" step="10"/></td>
  <td>
    <select class="form-control input-inline" name="storageMethod">
        <option value="냉장" <c:if test="${product.storageMethod eq '냉장'}">selected</c:if>>냉장</option>
        <option value="냉동" <c:if test="${product.storageMethod eq '냉동'}">selected</c:if>>냉동</option>
        <option value="상온" <c:if test="${product.storageMethod eq '상온'}">selected</c:if>>상온</option>
    </select>
</td>
<td>
    <select class="form-control input-inline" name="expirationType">
        <option value="30일" <c:if test="${product.expirationType eq '30일'}">selected</c:if>>30일</option>
        <option value="6개월" <c:if test="${product.expirationType eq '6개월'}">selected</c:if>>6개월</option>
        <option value="1년" <c:if test="${product.expirationType eq '1년'}">selected</c:if>>1년</option>
        <!-- 필요시 추가 -->
    </select>
</td>
   <td><input type="text" class="form-control input-inline" name="recipeCode" value="${product.recipeCode}"/></td>
                        <td>
                            <select class="form-control input-inline" name="productStatus">
                                <option value="생산" <c:if test="${product.productStatus eq '생산'}">selected</c:if>>생산</option>
                                <option value="중지" <c:if test="${product.productStatus eq '중지'}">selected</c:if>>중지</option>
                            </select>
                        </td>
                        <td><fmt:formatDate value="${product.regDate}" pattern="yyyy-MM-dd"/></td>
                        <td><fmt:formatDate value="${product.updDate}" pattern="yyyy-MM-dd"/></td>
                        <td>
                            <button type="submit" class="btn btn-success btn-sm">저장</button>
                            <button type="button" class="btn btn-secondary btn-sm" onclick="cancelEdit('${product.productId}')">취소</button>
                        </td>
                    </form>
                </tr>
            </c:forEach>
        </tbody>
    </table>
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



<script>
function validateForm() {
    const pid = document.getElementById('productId').value.trim();
    const pname = document.getElementById('productName').value.trim();
    const rcode = document.getElementById('recipeCode').value.trim();
    // 이미 등록된 목록(productList)이 JS에서 접근 가능하다고 가정(예: 데이터 속성, 전역변수 등)

    // 예시: 중복 검사(프론트용, 서버에서도 반드시 체크 필요)
    let isDuplicate = false;
    <c:forEach var="product" items="${productList}">
        if (pid === "${product.productId}" || pname === "${product.productName}" || rcode === "${product.recipeCode}") {
            isDuplicate = true;
        }
    </c:forEach>
    if (isDuplicate) {
        alert("제품코드, 제품명, 레시피코드는 중복될 수 없습니다.");
        return false;
    }
    return true;
}



</script>

<script>
function confirmDelete(productId) {
    if (confirm('정말로 삭제하시겠습니까?\n삭제된 정보는 복구할 수 없습니다.')) {
        if (confirm('정말로 삭제하시겠습니까? (최종확인)')) {
            // form을 동적으로 생성해서 POST 전송
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '${pageContext.request.contextPath}/master/product/delete';
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'productId';
            input.value = productId;
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        }
    }
}
</script>
