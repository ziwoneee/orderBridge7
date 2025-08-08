<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<%
  String sid = (request.getAttribute("supplierId") != null) 
               ? (String)request.getAttribute("supplierId") 
               : "";
%>
<c:if test="${empty cri.perPageNum}">
  <c:set var="cri.perPageNum" value="10" />
</c:if>


<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        <div class="row">
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">${supplier.supplierName} 공급 품목 목록</h3>
            <p class="text-muted">${supplier.supplierName} 협력사의 공급 가능한 자재 목록을 확인하세요.</p>
          </div>
          
          <!-- 등록 버튼 -->
          <div class="col-12 mb-3 text-right">
            <button class="btn btn-success mb-2" id="btnAddItem" data-supplier-id="${supplier.supplierId}">공급 품목 등록</button>
          </div>
                    
	          <!-- 공급 품목 테이블 -->
	          <div class="col-12">
                <div class="table-responsive">
                  <table class="table table-hover" id="itemTable">
                    <thead>
                      <tr>
                        <th>자재명</th>
                        <th>유형</th>
                        <th>단가</th>
                        <th>단위</th>
                        <th>공급 상태</th>
                        <th>비고</th>
                        <th>수정</th>
                      </tr>
                    </thead>
                    <tbody>
				    <c:choose>
				      <c:when test="${empty itemList}">
				        <tr>
				          <td colspan="7" class="text-center text-muted">등록된 공급 품목이 없습니다.</td>
				        </tr>22-9999	2025-07-23	
				      </c:when>
				      <c:otherwise>
				        <c:forEach var="item" items="${itemList}">
				          <tr class="${item.supplyAvailable eq 'N' ? 'inactive-row' : ''}" data-item-id="${item.id}">
				            <td>${item.materialName}</td>
				            <td>${item.materialType}</td>
				            <td><fmt:formatNumber value="${item.unitPrice}" pattern="#,##0" /></td>
				            <td>${item.unit}</td>
				            <td>
				              <span class="badge ${item.supplyAvailable eq 'Y' ? 'badge-success' : 'badge-secondary'}">
				                ${item.supplyAvailable eq 'Y' ? '활성' : '비활성'}
				              </span>
				            </td>
				            <td>${item.note}</td>
				            <td>
				              <button class="btn btn-sm btn-outline-warning btn-edit">수정</button>
				            </td>
				          </tr>
				        </c:forEach>
				      </c:otherwise>
				    </c:choose>
				  </tbody>
                  </table>
                </div>
              </div>
            </div>
            
            <!-- 페이징 처리 -->
			<div class="d-flex justify-content-center mt-4">
			  <nav>
			    <ul class="pagination justify-content-center mt-4">
			
			      <c:if test="${pageMaker.cri.page > 1}">
			        <li class="page-item">
			          <a class="page-link"
			             href="?supplierId=${supplierId}&page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}">
			            &laquo;
			          </a>
			        </li>
			      </c:if>
			
			      <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			        <li class="page-item ${p == cri.page ? 'active' : ''}">
			          <a class="page-link"
			             href="?supplierId=${supplierId}&page=${p}&perPageNum=${cri.perPageNum}">
			            ${p}
			          </a>
			        </li>
			      </c:forEach>
			
			      <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
			        <li class="page-item">
			          <a class="page-link"
			             href="?supplierId=${supplierId}&page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}">
			            &raquo;
			          </a>
			        </li>
			      </c:if>
			
			    </ul>
			  </nav>
			</div>
			<!-- 페이징 끝 -->
			            
            
          </div>
        <!-- content-wrapper 끝 -->
      <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>  
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
	  <script src="${pageContext.request.contextPath}/resources/vendors/select2/select2.min.js"></script>
      <script src="${pageContext.request.contextPath}/resources/js/supplierItem.js"></script>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   

<!-- 모달 -->
<div class="modal fade" id="itemModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <form id="itemForm">
      	<input type="hidden" name="id" id="itemId" value="">
      	
        <div class="modal-header">
          <h5 class="modal-title">공급 품목 등록</h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
       
        <div class="modal-body">
          <input type="hidden" id="supplierId" name="supplierId" value="<%= sid %>">

		<div class="form-group invisible-select2-wrapper" id="materialIdWrapper">
		  <label>자재 선택</label>
		  <select id="materialId" name="materialId" class="form-control select2" style="width: 100%;">
		    <option value="">자재를 선택하세요</option>
		    <c:forEach var="m" items="${materialList}">
		      <option 
		        value="${m.materialId}" 
		        data-unitprice="${m.unitPrice}" 
		        data-unit="${m.unit}">
		        ${m.materialName} (${m.materialId})
		      </option>
		    </c:forEach>
		  </select>
		</div>


          <div class="form-group">
            <label>단가</label>
            <input type="number" name="unitPrice" id="unitPrice" class="form-control" required>
          </div>
          <div class="form-group">
            <label>단위</label>
            <input type="text" name="unit" id="unit" class="form-control" required>
          </div>
          <div class="form-group">
            <label>공급 상태</label>
            <select name="supplyAvailable" class="form-control">
              <option value="Y">활성</option>
              <option value="N">비활성</option>
            </select>
          </div>
          <div class="form-group">
            <label>비고</label>
            <textarea name="note" class="form-control"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">목록</button>
          <button type="submit" id="btnAddItemSubmit" class="btn btn-primary">저장</button>
        </div>
      </form>
    </div>
  </div>
</div>

<style>
/* ✅ select2 깜빡임 완전 방지용 CSS */
/* ✅ 더 이상 숨김/보임 처리가 필요없으므로 기본적인 스타일만 */
#materialIdWrapper {
  margin-bottom: 1rem;
}
</style>