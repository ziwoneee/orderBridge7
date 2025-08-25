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
			
			<!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">발주 관리</h3>
			</div>
			
			 <!-- 검색 영역 -->
            <div class="col-12 mb-3">
              <form method="get" action="/material/order/list" class="forms-sample">
                <div class="row align-items-end">
                  <div class="col-md-2 form-group">
                    <label class="form-label text-muted small">발주일</label>
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
                           placeholder="발주번호, 거래처명 검색" value="${param.keyword}">
                  </div>
                  <div class="col-md-3 form-group">
                    <button type="submit" class="btn btn-primary me-2" style="background-color: #1C355E; border-color: #1C355E;">
                      <i class="ti-search"></i> 검색
                    </button>
                    <a href="/material/order/list" class="btn btn-light">
                      <i class="ti-reload"></i> 초기화
                    </a>
                  </div>
                </div>
                <!-- 숨겨진 파라미터 -->
                <input type="hidden" name="sortColumn" value="${cri.sortColumn}">
                <input type="hidden" name="sortOrder" value="${cri.sortOrder}">
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="perPageNum" value="${cri.perPageNum}">
                <input type="hidden" name="status" value="${empty param.status ? 'ALL' : param.status}">
              </form>
            </div>
            
             <!-- 발주 목록 -->
            <div class="col-12">

			<!-- 상단 제목 + 버튼 -->
			<div class="d-flex justify-content-end align-items-center mb-3">
			  <a href="/material/order/register" class="btn btn-success mb-2">
			    발주 등록
			  </a>
			</div>

              <!-- 탭 -->
              <div class="d-flex justify-content-between align-items-center mb-0">
                <ul class="nav nav-underline-custom" id="statusTab" role="tablist">
                  <li class="nav-item">
                    <a class="nav-link ${empty param.status || param.status eq 'ALL' ? 'active' : ''}"
					   href="/material/order/list?status=ALL&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
					  전체 <span class="badge badge-light ms-1"></span>
					</a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '초안' ? 'active' : ''}" 
                       href="/material/order/list?status=초안&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      초안 <span class="badge badge-light ms-1">${draftCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '요청' ? 'active' : ''}" 
                       href="/material/order/list?status=요청&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      요청 <span class="badge badge-light ms-1">${requestCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '승인' ? 'active' : ''}" 
                       href="/material/order/list?status=승인&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      승인 <span class="badge badge-light ms-1">${approvedCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '입고완료' ? 'active' : ''}" 
                       href="/material/order/list?status=입고완료&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      입고완료 <span class="badge badge-light ms-1">${completedCount}</span>
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
                        <a href="/material/order/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=order_id&sortOrder=${cri.sortColumn == 'order_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          발주번호 
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'order_id'}">
						        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
						      </c:when>
						      <c:otherwise>
						        <span class="neutral-arrow">⇅</span>
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>거래처명</th>
                      <th>
                        <a href="/material/order/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=order_date&sortOrder=${cri.sortColumn == 'order_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          발주일
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'order_date'}">
						        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
						      </c:when>
						      <c:otherwise>
						        <span class="neutral-arrow">⇅</span>
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>
                        <a href="/material/order/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=expected_arrived_date&sortOrder=${cri.sortColumn == 'expected_arrived_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          납기일
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'expected_arrived_date'}">
						        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
						      </c:when>
						      <c:otherwise>
						        <span class="neutral-arrow">⇅</span>
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>담당자</th>
                      <th>발주상태</th>
                      <th>상세</th>
                      <th>발주요청</th>
                    </tr>
                  </thead>
                  
                  <!-- 테이블 내용 -->
                  <tbody>
                    <c:forEach var="order" items="${orderList}">
                      <tr>
                        <!-- 발주번호 -->
                        <td class="font-weight-medium">${order.orderId}</td>
                        
                        <!-- 거래처명 -->
                        <td>${order.supplierName}</td>
                        
                        <!-- 발주일 -->
                        <td>
                          <fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd"/>
                        </td>
                        
					    <!-- 납기일 -->
						<td>
						  <fmt:formatDate value="${order.expectedArrivedDate}" pattern="yyyy-MM-dd"/>
						
						  <!-- 오늘 날짜 -->
						  <c:set var="today" value="<%=new java.util.Date()%>" />
						  <c:set var="daysDiff" value="${(order.expectedArrivedDate.time - today.time) / (1000*60*60*24)}" />
						
						  <!-- D-Day 뱃지 (입고완료 제외) -->
						  <c:if test="${order.orderStatus != '입고완료'}">
						    
						    <!-- 임박 -->
						    <c:if test="${daysDiff <= 2 && daysDiff >= 0}">
						      <span class="badge badge-warning badge-pill">
						        D-<fmt:formatNumber value="${daysDiff}" maxFractionDigits="0" />
						      </span>
						    </c:if>
						
						    <!-- 지연 -->
						    <c:if test="${daysDiff < 0}">
						      <span class="badge badge-danger badge-pill">지연</span>
						    </c:if>
						
						  </c:if>
						</td>

                        
                        <!-- 담당자 -->
                        <td>
						  <c:choose>
						    <c:when test="${not empty order.handlerName}">${order.handlerName}</c:when>
						    <c:when test="${not empty order.handledBy}">${order.handledBy}</c:when>
						    <c:otherwise>-</c:otherwise>
						  </c:choose>
						</td>
                        
						<!-- 상태 -->
						<td>
						  <c:choose>
						    <c:when test="${order.orderStatus eq '요청'}"><span class="badge badge-warning">요청</span></c:when>
						    <c:when test="${order.orderStatus eq '승인'}"><span class="badge badge-primary">승인</span></c:when>
						    <c:when test="${order.orderStatus eq '입고완료'}"><span class="badge badge-success">입고완료</span></c:when>
						    <c:when test="${order.orderStatus eq '초안'}"><span class="badge badge-secondary">초안</span></c:when>
						    <c:otherwise><span class="badge badge-secondary">${order.orderStatus}</span></c:otherwise>
						  </c:choose>
						</td>
						
						<!-- 상세 -->
						<td>
						  <button type="button" class="btn btn-sm btn-outline-info btnOrderDetail"
						          data-id="${order.orderId}">상세</button>
						</td>
						
						<!-- 발주요청 -->
						<td>
						  <c:choose>
						    <c:when test="${order.orderStatus eq '초안'}">
						      <button type="button" class="btn btn-sm btn-outline-success btnSubmitOrder"
						              data-id="${order.orderId}">발주요청</button>
						    </c:when>
						    <c:otherwise></c:otherwise>
						  </c:choose>
						</td>
                      </tr>
                    </c:forEach>
                    
                    <!-- 데이터가 없을 때 -->
                    <c:if test="${empty orderList}">
                      <tr>
                        <td colspan="8" class="text-center py-4">
                          <div class="text-muted">
                            <i class="ti-info-alt" style="font-size: 24px;"></i>
                            <p class="mt-2">조회된 발주 건이 없습니다.</p>
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
			
			      <!-- 이전 블록(«) : 블록이 있을 때만 노출 -->
			      <c:if test="${pageMaker.prev}">
			        <li class="page-item">
			          <a class="page-link"
			             href="/material/order/list?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
			            &laquo;
			          </a>
			        </li>
			      </c:if>
			
			      <!-- 현재 블록의 페이지들 -->
			      <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			        <li class="page-item ${p == cri.page ? 'active' : ''}">
			          <a class="page-link"
			             href="/material/order/list?page=${p}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
			            ${p}
			          </a>
			        </li>
			      </c:forEach>
			
			      <!-- 다음 블록(») : 더 페이지가 있을 때만 노출 -->
			      <c:if test="${pageMaker.next}">
			        <li class="page-item">
			          <a class="page-link"
			             href="/material/order/list?page=${pageMaker.endPage + 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
			            &raquo;
			          </a>
			        </li>
			      </c:if>
			
			    </ul>
			  </nav>
			</div>
          <!-- 페이징 처리 끝 -->
          
		    <!-- 발주 상세 모달 -->
			<div class="modal fade" id="orderDetailModal" tabindex="-1" role="dialog" aria-labelledby="modalTitle" aria-hidden="true">
			  <div class="modal-dialog modal-lg" role="document">
			    <div class="modal-content">
			
			      <div class="modal-header" style="background-color: #1c355e; color: #ffffff; ">
			        <h5 class="modal-title">발주 상세</h5>
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
			          <span aria-hidden="true">&times;</span>
			        </button>
			      </div>
			
			      <div class="modal-body">
			        <!-- 기본 정보 -->
			        <table class="table table-bordered">
			          <tbody>
			            <tr>
			              <th class="bg-light">발주번호</th>
			              <td id="modalOrderId"></td>
			              <th class="bg-light">거래처</th>
			              <td id="modalSupplierId"></td>
			            </tr>
			            <tr>
			              <th class="bg-light">발주일</th>
			              <td id="modalOrderDate"></td>
			              <th class="bg-light">예상입고일</th>
			              <td id="modalExpectedDate"></td>
			            </tr>
			            <tr>
			              <th class="bg-light">발주상태</th>
			              <td id="modalOrderStatus"></td>
			              <th class="bg-light">담당자</th>
			              <td id="modalHandler"></td>
			            </tr>
			            <tr>
			              <th class="bg-light">비고</th>
			              <td colspan="3" id="modalNote"></td>
			            </tr>
			          </tbody>
			        </table>
			
			        <!-- 발주 상세 항목 -->
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
			          <tbody id="orderItemsInfo">
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
          
        </div>
        <!-- content-wrapper 끝 -->
	  <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->   

<script>var ctx='${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/resources/js/materialOrderList.js"></script>
<c:if test="${not empty mailMsg}">
  <script>
    // 페이지가 그려진 뒤 실행되도록 보장
    window.addEventListener('DOMContentLoaded', function(){
      // c:out로 XSS 방지, 템플릿 리터럴로 따옴표 문제 회피
      alert(`<c:out value='${mailMsg}'/>`);
    });
  </script>
</c:if>
