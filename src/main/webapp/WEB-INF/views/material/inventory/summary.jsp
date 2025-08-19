<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      

  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

      <!-- 본문 시작 -->
      <div class="main-panel">
        <div class="content-wrapper">
          <div class="row">
          
          	<!-- 페이지 헤더 -->
			<div class="col-md-12 grid-margin">
			  <div class="row">
			    <div class="col-12 col-xl-8 mb-4 mb-xl-0">
			      <h3 class="font-weight-bold">자재 재고 현황</h3>
			    </div>
			  </div>
			</div>
			
			<!-- 검색 영역 -->
			<div class="col-12 mb-3">
			  <form method="get" action="/material/inventory/summary" class="forms-sample">
			    <div class="row align-items-end">
			      <!-- 자재유형 -->
			      <div class="col-md-2 form-group">
			        <label class="form-label text-muted small">자재유형</label>
			        <select name="materialType" class="form-control" onchange="this.form.submit()">
			          <option value="">전체</option>
			          <option value="생육" ${cri.materialType eq '생육' ? 'selected' : ''}>생육</option>
			          <option value="채소류" ${cri.materialType eq '채소류' ? 'selected' : ''}>채소류</option>
			          <option value="향신료" ${cri.materialType eq '향신료' ? 'selected' : ''}>향신료</option>
			          <option value="조미료" ${cri.materialType eq '조미료' ? 'selected' : ''}>조미료</option>
			          <option value="액상조미료" ${cri.materialType eq '액상조미료' ? 'selected' : ''}>액상조미료</option>
			          <option value="외주가공" ${cri.materialType eq '외주가공' ? 'selected' : ''}>외주가공</option>
			          <option value="포장재" ${cri.materialType eq '포장재' ? 'selected' : ''}>포장재</option>
			          <option value="기타" ${cri.materialType eq '기타' ? 'selected' : ''}>기타</option>
			        </select>
			      </div>
			      
			      <!-- 통합 검색 -->
			      <div class="col-md-4 form-group">
			        <label class="form-label text-muted small">통합검색</label>
			        <input type="text" class="form-control" name="keyword" 
			               placeholder="자재코드/명/유형 검색" value="${cri.keyword}">
			      </div>
			      
			      <!-- 검색 버튼 -->
			      <div class="col-md-3 form-group">
			        <button type="submit" class="btn btn-primary me-2" style="background-color: #1C355E; border-color: #1C355E;">
			          <i class="ti-search"></i> 검색
			        </button>
			        <a href="/material/inventory/summary" class="btn btn-light">
			          <i class="ti-reload"></i> 초기화
			        </a>
			      </div>
			    </div>
			    
			    <!-- 숨겨진 파라미터 (정렬 상태 유지용) -->
			    <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
			    <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
			    <input type="hidden" name="page" value="1">
			    <input type="hidden" name="perPageNum" value="${cri.perPageNum}">
			  </form>
			</div>
          
          
    <!-- 재고 목록 테이블 -->      
	<div class="col-12">
	
		<!-- 상태별 탭 -->
		<div class="d-flex justify-content-between align-items-center mb-3">
		    <ul class="nav nav-underline-custom" id="statusTab" role="tablist">
		        <li class="nav-item">
		            <a class="nav-link ${empty param.status ? 'active' : ''}" 
		               href="/material/inventory/summary?keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
		                전체 <span class="badge badge-light ms-1">${totalCount}</span>
		            </a>
		        </li>
		        <li class="nav-item">
		            <a class="nav-link ${param.status eq '정상' ? 'active' : ''}" 
		               href="/material/inventory/summary?status=정상&keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
		                정상 <span class="badge badge-light  ms-1">${normalCount}</span>
		            </a>
		        </li>
		        <li class="nav-item">
		            <a class="nav-link ${param.status eq '부족' ? 'active' : ''}" 
		               href="/material/inventory/summary?status=부족&keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
		                부족 <span class="badge badge-light  ms-1">${shortageCount}</span>
		            </a>
		        </li>
		        <li class="nav-item">
		            <a class="nav-link ${param.status eq '소진' ? 'active' : ''}" 
		               href="/material/inventory/summary?status=소진&keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
		                소진 <span class="badge badge-light ms-1">${exhaustedCount}</span>
		            </a>
		        </li>
		
		    </ul>
		</div>
	
	  <div id="table_content" class="table-responsive">
		<table class="table table-hover">
			<thead style="background-color: #1C355E; color: white; border-top: none;">
				<tr>
			        <!-- 자재코드 (정렬 가능) -->
			        <th>
			          <a href="/material/inventory/summary?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&materialType=${cri.materialType}&sortColumn=material_id&sortOrder=${cri.sortColumn == 'material_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
			             class="text-white text-decoration-none">
			            자재코드
			            <c:choose>
			              <c:when test="${cri.sortColumn == 'material_id'}">
			                <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
			              </c:when>
			              <c:otherwise>⇅</c:otherwise>
			            </c:choose>
			          </a>
			        </th>
			        
			        <!-- 자재명 (정렬 가능) -->
			        <th>
			          <a href="/material/inventory/summary?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&materialType=${cri.materialType}&sortColumn=material_name&sortOrder=${cri.sortColumn == 'material_name' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
			             class="text-white text-decoration-none">
			            자재명
			            <c:choose>
			              <c:when test="${cri.sortColumn == 'material_name'}">
			                <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
			              </c:when>
			              <c:otherwise>⇅</c:otherwise>
			            </c:choose>
			          </a>
			        </th>
			        
			        <!-- 유형 (정렬 가능) -->
			        <th>
			          <a href="/material/inventory/summary?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&materialType=${cri.materialType}&sortColumn=material_type&sortOrder=${cri.sortColumn == 'material_type' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
			             class="text-white text-decoration-none">
			            유형
			            <c:choose>
			              <c:when test="${cri.sortColumn == 'material_type'}">
			                <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
			              </c:when>
			              <c:otherwise>⇅</c:otherwise>
			            </c:choose>
			          </a>
			        </th>
			        
			        <!-- 현재고 (정렬 가능) -->
			        <th>
			          <a href="/material/inventory/summary?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&materialType=${cri.materialType}&sortColumn=quantity&sortOrder=${cri.sortColumn == 'quantity' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
			             class="text-white text-decoration-none">
			            현재고
			            <c:choose>
			              <c:when test="${cri.sortColumn == 'quantity'}">
			                <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
			              </c:when>
			              <c:otherwise>⇅</c:otherwise>
			            </c:choose>
			          </a>
			        </th>
			        
			        <th>안전재고</th>
			        <th>상태</th>
			        <th>단위</th>
			        
			        <!-- 유통기한 (정렬 가능) -->
			        <th>
			          <a href="/material/inventory/summary?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&materialType=${cri.materialType}&sortColumn=expiration_date&sortOrder=${cri.sortColumn == 'expiration_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
			             class="text-white text-decoration-none">
			            유통기한
			            <c:choose>
			              <c:when test="${cri.sortColumn == 'expiration_date'}">
			                <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
			              </c:when>
			              <c:otherwise>⇅</c:otherwise>
			            </c:choose>
			          </a>
			        </th>
			        
			        <!-- 최근입출고일 (정렬 가능) -->
			        <th>
			          <a href="/material/inventory/summary?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&materialType=${cri.materialType}&sortColumn=last_movement_date&sortOrder=${cri.sortColumn == 'last_movement_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
			             class="text-white text-decoration-none">
			            최근입출고일
			            <c:choose>
			              <c:when test="${cri.sortColumn == 'last_movement_date'}">
			                <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
			              </c:when>
			              <c:otherwise>⇅</c:otherwise>
			            </c:choose>
			          </a>
			        </th>
			        
			        <th>보관창고</th>
			        <th>LOT 번호</th>
		        </tr>
			</thead>
			
			<tbody>
				 <c:forEach var="inv" items="${summaryList}">
			        <tr>
			          <td class="font-weight-medium">${inv.materialId}</td>
			          <td>${inv.materialName}</td>
			          <td>${inv.materialType}</td>
			          <td class="text-end"><fmt:formatNumber value="${inv.quantity}" pattern="#,###"/></td>
			          <td class="text-end"><fmt:formatNumber value="${inv.safetyStock}" pattern="#,###"/></td>
					  <td>
					    <c:choose>
					        <c:when test="${inv.stockStatus == '정상'}">
					            <span class="badge badge-success">정상</span>
					        </c:when>
					        <c:when test="${inv.stockStatus == '부족'}">
					            <span class="badge badge-warning">부족</span>
					        </c:when>
					        <c:when test="${inv.stockStatus == '소진'}">
					            <span class="badge badge-danger">소진</span>
					        </c:when>
					        <c:otherwise>
					        </c:otherwise>
					    </c:choose>
					  </td>
			          <td>${inv.unit}</td>
					  <td>
			          	  <fmt:formatDate value="${inv.expirationDate}" pattern="yyyy-MM-dd" />
						  <c:if test="${inv.expirationDate.time - now.time le 3 * 24 * 60 * 60 * 1000}">
						    <span class="badge badge-danger ml-1">임박</span>
						  </c:if>
		          	  </td>
			          <td><fmt:formatDate value="${inv.lastMovementDate}" pattern="yyyy-MM-dd" /></td>
			          <td>${inv.warehouseCode}</td>
			          <!-- LOT 상세 버튼 -->
			          <td>
			            <button class="btn btn-sm btn-outline-info" onclick="showLotDetails('${inv.materialId}')">
			              상세
			            </button>
			          </td>
			        </tr>
				</c:forEach>
				
				<!-- 데이터가 없을 경우 -->
				<c:if test="${empty summaryList}">
				  <tr>
				    <td colspan="12" class="text-center text-muted py-4">
				      검색된 재고 정보가 없습니다.
				    </td>
				  </tr>
				</c:if>
		  </tbody>
	  </table>
	  </div>
	  
		<!-- LOT 상세 모달 -->
		<div class="modal fade" id="lotModal" tabindex="-1" role="dialog" aria-labelledby="lotModalLabel" aria-hidden="true">
		  <div class="modal-dialog modal-lg" role="document">
		    <div class="modal-content">
		      <div class="modal-header" style="background-color: #1c355e; color: #ffffff;">
		        <h5 class="modal-title" id="lotModalLabel">LOT 상세 정보</h5>
		        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
		          <span aria-hidden="true">&times;</span>
		        </button>
		      </div>
		      
		      <div class="modal-body">
		        <div class="table-responsive">
		        
		        <!-- ✅ 자재 기본 정보 -->
		        <h5 class="font-weight-bold mb-3">자재 기본 정보</h5>
		        <table class="table table-bordered table-sm mb-4">
		          <tbody>
		            <tr>
		              <th class="bg-light">자재코드</th>
		              <td id="modalMaterialId">-</td>
		              <th class="bg-light">자재명</th>
		              <td id="modalMaterialName">-</td>
		            </tr>
		            <tr>
		              <th class="bg-light">자재유형</th>
		              <td id="modalMaterialType">-</td>
		              <th class="bg-light">단위</th>
		              <td id="modalMaterialUnit">-</td>
		            </tr>
		          </tbody>
		        </table>
		        
		          <h5 class="font-weight-bold mb-3">LOT 상세</h5>
		          <table class="table table-bordered text-center">
		            <thead>
		             <tr id="lotTableHeader">
				      <th data-column="lotNo">
					    <a href="#" onclick="sortLotBy('lotNo'); return false;">LOT 번호</a>
					  </th>
				      <th data-column="quantity">
					    <a href="#" onclick="sortLotBy('quantity'); return false;">현재고</a>
					  </th>
				      <th data-column="expirationDate">
					    <a href="#" onclick="sortLotBy('expirationDate'); return false;">유통기한</a>
					  </th>
				      <th>보관창고</th>
				      <th>재고상태</th>
				    </tr>
		            </thead>
		            <tbody id="lotTableBody">
		              <!-- JavaScript로 동적 생성 -->
		            </tbody>
		          </table>
		          
		          <!-- LOT 테이블 페이징 버튼 영역 -->
				  <ul class="pagination justify-content-center mt-3" id="lotPagination"></ul>
		          
		        </div>
		      </div>
		      
		      <div class="modal-footer">
		        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
		      </div>
		    </div>
		  </div>
		</div>
	  
	  
	  <!-- 페이징 처리 시작 -->
		<div class="d-flex justify-content-center mt-4">
		  <nav>
		    <ul class="pagination justify-content-center mt-4">
		      
		      <c:if test="${pageMaker.cri.page > 1}">
		        <li class="page-item">
		          <a class="page-link" 
		             href="/material/inventory/summary?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
		            &laquo;
		          </a>
		        </li>
		      </c:if>
		      
		      <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
		        <li class="page-item ${p == cri.page ? 'active' : ''}">
		          <a class="page-link" 
		             href="/material/inventory/summary?page=${p}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
		            ${p}
		          </a>
		        </li>
		      </c:forEach>
		      
		      <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
		        <li class="page-item">
		          <a class="page-link" 
		             href="/material/inventory/summary?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&materialType=${param.materialType}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
		            &raquo;
		          </a>
		        </li>
		      </c:if>
		      
		    </ul>
		  </nav>
		</div>
		<!-- 페이징 처리 끝 -->
			
		 </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->

<script src="${pageContext.request.contextPath}/resources/js/materialInventoryLot.js"></script>