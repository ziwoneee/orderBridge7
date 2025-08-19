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
                <h3 class="font-weight-bold">자재 출고 관리</h3>
              </div>
            </div>
          </div>
            
            <!-- 검색 영역 -->
            <div class="col-12 mb-3">
              <form method="get" action="/material/outbound/list" class="forms-sample">
                <div class="row align-items-end">
                  <div class="col-md-2 form-group">
                    <label class="form-label text-muted small">출고일자</label>
                    <input type="date" class="form-control" id="startDate" name="startDate" value="${param.startDate}">
                  </div>
                  <div class="col-md-auto form-group text-center px-2">
                    <label class="form-label text-muted small">&nbsp;</label>
                    <div class="mt-2">~</div>
                  </div>
                  <div class="col-md-2 form-group">
                    <label class="form-label text-muted small">&nbsp;</label>
                    <input type="date" class="form-control" id="endDate" name="endDate" value="${param.endDate}">
                  </div>
                  <div class="col-md-4 form-group">
                    <input type="text" class="form-control" id="keyword" name="keyword" 
                           placeholder="출고관리번호, 작업지시번호, 품명 검색" value="${param.keyword}">
                  </div>
                  <div class="col-md-3 form-group">
                    <button type="submit" class="btn btn-primary me-2" style="background-color: #1C355E; border-color: #1C355E;">
                      <i class="ti-search"></i> 검색
                    </button>
                    
                    <a href="/material/outbound/list" class="btn btn-light">
                      <i class="ti-reload"></i> 초기화
                    </a>
                  </div>
                </div>
                <!-- 숨겨진 파라미터 -->
                <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
                <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="perPageNum" value="${cri.perPageNum}">
              </form>
            </div>
            
            
            <!-- 자재 출고 목록 -->
            <div class="col-12">
            
	        <div class="d-flex justify-content-end align-items-center mb-3">
			  <a type="button" class="btn btn-success mr-2" id="btnLoadOrder">
			    작업지시 불러오기
			  </a>
			  <button id="btnPickInbound"   class="btn btn-info mr-2">입고완료건 불러오기</button>
			</div>
			
              <!-- 탭 -->
			<div class="d-flex justify-content-between align-items-center mb-0">
			  <ul class="nav nav-underline-custom" id="statusTab" role="tablist">
			    <li class="nav-item">
			      <a class="nav-link ${empty param.status ? 'active' : ''}" 
			         href="/material/outbound/list?keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
			        전체 
			      </a>
			    </li>
			    <li class="nav-item">
			      <a class="nav-link ${param.status eq 'DRAFT' ? 'active' : ''}" 
			         href="/material/outbound/list?status=DRAFT&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
			        미출고 <span class="badge badge-light ms-1">${pendingCount}</span>
			      </a>
			    </li>
			    <li class="nav-item">
			      <a class="nav-link ${param.status eq 'ISSUED' ? 'active' : ''}" 
			         href="/material/outbound/list?status=ISSUED&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
			        출고완료 <span class="badge badge-light ms-1">${completedCount}</span>
			      </a>
			    </li>
			  </ul>
			</div>
                  
              <!-- 테이블 -->
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead style="background-color: #1C355E; color: white; border-top: none;">
                    <tr>
                      <th>
                        <a href="/material/outbound/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=outbound_id&sortOrder=${cri.sortColumn == 'outbound_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          출고관리번호 
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'outbound_id'}">
						        <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
						      </c:when>
						      <c:otherwise>
						        ⇅
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>
                        <a href="/material/outbound/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=outbound_date&sortOrder=${cri.sortColumn == 'outbound_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          출고일자
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'outbound_date'}">
						        <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
						      </c:when>
						      <c:otherwise>
						        ⇅
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>출고상태</th>
                      <th>작업지시번호</th>
                      <th>
                        <a href="/material/outbound/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=due_date&sortOrder=${cri.sortColumn == 'due_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          납기일자
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'due_date'}">
						        <i class="ti-arrow-${cri.sortOrder == 'asc' ? 'up' : 'down'}"></i>
						      </c:when>
						      <c:otherwise>
						        ⇅
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>담당자</th>
                      <th>상세</th>
                      <th>출고처리</th>
                    </tr>
                  </thead>
                  
                  <!-- 테이블 내용! -->
                  <tbody>
                    <c:forEach var="item" items="${list}">
                      	<tr>
                 		 <!-- 출고관리번호: rowNum == 1일 때만 표시 -->
   						  <td class="font-weight-medium">${item.outboundId}</td>
   						  
   						 <!-- 출고일자 -->
   						 <td>
						  <c:if test="${item.outboundDate != null}">
						    ${item.outboundDate.toLocalDate()}
						  </c:if>
						</td>

  
				         <!-- 출고상태 -->
						<td>
						  <c:choose>
						    <c:when test="${item.statusCode eq 'ISSUED'}">
						      <span class="badge badge-success">${item.statusDisplay}</span>
						    </c:when>
						    <c:otherwise>
						      <span class="badge badge-danger">${item.statusDisplay}</span>
						    </c:otherwise>
						  </c:choose>
						</td>
  
			 	         <!-- 작업지시번호 -->
			 	         <td>${item.workOrderId}</td>
				
						<!-- 납기일자 + D-Day -->
						<td>
						  <fmt:formatDate value="${item.dueDate}" pattern="yyyy-MM-dd"/>
						  
						  <!-- 오늘 날짜 (시분초 제거해서 0시 기준) -->
						  <jsp:useBean id="__now" class="java.util.Date" />
						  <fmt:formatDate value="${__now}" pattern="yyyy-MM-dd" var="todayStr"/>
						  <fmt:parseDate value="${todayStr}" pattern="yyyy-MM-dd" var="today0"/>
						  
						  <!-- 납기 날짜 (시분초 제거) -->
						  <fmt:formatDate value="${item.dueDate}" pattern="yyyy-MM-dd" var="dueStr"/>
						  <fmt:parseDate value="${dueStr}" pattern="yyyy-MM-dd" var="due0"/>
						  
						  <!-- 일수 차이 계산 -->
						  <c:set var="daysDiff" value="${(due0.time - today0.time) / (1000*60*60*24)}"/>
						
						  <c:choose>
						    <c:when test="${daysDiff == 0}">
						      <span class="badge badge-info badge-pill">오늘</span>
						    </c:when>
						    <c:when test="${daysDiff > 0 && daysDiff <= 2}">
							  <span class="badge badge-warning badge-pill">
							    D-<fmt:formatNumber value="${daysDiff}" pattern="#"/>
							  </span>
							</c:when>
						    <c:when test="${daysDiff < 0}">
						      <span class="badge badge-danger badge-pill">지연</span>
						    </c:when>
						  </c:choose>
						</td>


				         
				         <!-- 담당자 -->
				         <td>${item.handledBy}</td>
                        
						<td>
						  <button type="button" class="btn btn-sm btn-outline-info" onclick="loadOutboundDetail('${item.outboundId}')">상세</button>
						</td>
						
						<td>
						  <c:if test="${item.statusCode ne 'ISSUED'}">
						    <button type="button" class="btn btn-outline-success btn-sm"
						            onclick="processOutbound('${item.outboundId}', this)">출고처리</button>
						  </c:if>
						</td>
                        
                       </tr>
                    </c:forEach>
                    
                    
                    <!-- 데이터가 없을 때 -->
                    <c:if test="${empty list}">
                      <tr>
                        <td colspan="11" class="text-center py-4">
                          <div class="text-muted">
                            <i class="ti-info-alt" style="font-size: 24px;"></i>
                            <p class="mt-2">조회된 출고 건이 없습니다.</p>
                          </div>
                        </td>
                      </tr>
                    </c:if>
                  </tbody>
                </table>
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
                       href="/material/outbound/list?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
                      &laquo;
                    </a>
                  </li>
                </c:if>
                
                <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                  <li class="page-item ${p == cri.page ? 'active' : ''}">
                    <a class="page-link" 
                       href="/material/outbound/list?page=${p}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
                      ${p}
                    </a>
                  </li>
                </c:forEach>
                
                <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
                  <li class="page-item">
                    <a class="page-link" 
                       href="/material/outbound/list?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
                      &raquo;
                    </a>
                  </li>
                </c:if>
                
              </ul>
            </nav>
          </div>
          <!-- 페이징 처리 끝 -->
          
		  <!-- 출고 상세 모달  -->
			<div class="modal fade" id="outboundDetailModal" tabindex="-1" role="dialog" aria-hidden="true">
			  <div class="modal-dialog modal-lg" role="document">
			    <div class="modal-content">
			
			      <div class="modal-header" style="background:#1c355e;color:#fff;">
			        <h5 class="modal-title">출고 상세</h5>
			        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
			      </div>
			
			      <div class="modal-body">
			        <!-- 기본 정보 -->
			        <h6 class="mt-4">출고 정보</h6>
			        <table class="table table-bordered">
			          <tbody>
			            <tr>
			              <th class="bg-light">출고번호</th><td id="modalOutboundId"></td>
			              <th class="bg-light">작업지시</th><td id="modalWorkOrderId"></td>
			            </tr>
			            <tr>
			              <th class="bg-light">출고일시</th><td id="modalIssuedAt"></td>
			              <th class="bg-light">담당자</th><td id="modalHandledBy"></td>
			            </tr>
			            <tr>
			              <th class="bg-light">상태</th><td id="modalStatus"></td>
			              <th class="bg-light">비고</th><td id="modalNote"></td>
			            </tr>
			          </tbody>
			        </table>
			
			        <!-- 상세 항목 -->
			        <h6 class="mt-4">출고 항목</h6>
			        <table class="table table-bordered text-center">
			          <thead>
			            <tr>
			              <th>자재ID</th>
			              <th>품명</th>
			              <th>LOT</th>
			              <th>출고수량</th>
			              <th>창고</th>
			              <th>유통기한</th>
			              <th>비고</th>
			            </tr>
			          </thead>
			          <tbody id="outboundItemsInfo">
			            <tr><td colspan="7" class="text-muted">불러오는 중...</td></tr>
			          </tbody>
			        </table>
			      </div>
			
			      <div class="modal-footer">
			        <button class="btn btn-secondary" data-dismiss="modal">닫기</button>
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
<jsp:include page="inboundModal.jsp"/>
<jsp:include page="orderModal.jsp"/>
<script>var ctx='${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/resources/js/materialOutbound.js?v=${System.currentTimeMillis()}"></script>