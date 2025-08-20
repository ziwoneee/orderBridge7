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
                  <h3 class="font-weight-bold">자재 입고 관리</h3>
                </div>
              </div>
            </div>
            
            <!-- 검색 영역 -->
            <div class="col-12 mb-3">
              <form method="get" action="/material/inbound/list" class="forms-sample">
                <div class="row align-items-end">
                  <div class="col-md-2 form-group">
                    <label class="form-label text-muted small">입고일자</label>
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
                           placeholder="입고관리번호, 발주번호, 품명, 담당자 검색" value="${param.keyword}">
                  </div>
                  <div class="col-md-3 form-group">
                    <button type="submit" class="btn btn-primary me-2" style="background-color: #1C355E; border-color: #1C355E;">
                      <i class="ti-search"></i> 검색
                    </button>
                    <a href="/material/inbound/list" class="btn btn-light">
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
            
			<div class="col-12">             
			<!-- 미입고 발주 목록 불러오기 버튼 -->
			<div class="d-flex justify-content-end align-items-center mb-3">
				<a onclick="loadUnreceivedOrders()" class="btn btn-success mb-3">
				  미입고 발주 목록 보기
				</a>
			</div>
			</div>
			
			
			
            
            <!-- 자재 입고 목록 -->
            <div class="col-12">
              <!-- 탭 -->
              <div class="d-flex justify-content-between align-items-center mb-0">
                <ul class="nav nav-underline-custom" id="statusTab" role="tablist">
                  <li class="nav-item">
                    <a class="nav-link ${empty param.status ? 'active' : ''}" 
                       href="/material/inbound/list?keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      전체 <span class="badge badge-light ms-1"></span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '미입고' ? 'active' : ''}" 
                       href="/material/inbound/list?status=미입고&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      미입고 <span class="badge badge-light ms-1">${pendingCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '부분입고' ? 'active' : ''}" 
                       href="/material/inbound/list?status=부분입고&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      부분입고 <span class="badge badge-light ms-1">${partialCount}</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link ${param.status eq '입고완료' ? 'active' : ''}" 
                       href="/material/inbound/list?status=입고완료&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}&page=1&perPageNum=${cri.perPageNum}">
                      입고완료 <span class="badge badge-light ms-1">${completedCount}</span>
                    </a>
                  </li>
                </ul>
              </div>
                  
              <!-- 테이블 -->
              <div class="table-responsive">
                <table class="table table-hover" id="inboundTable">
                  <thead>
                    <tr>
                      <th>
                        <a href="/material/inbound/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=inbound_id&sortOrder=${cri.sortColumn == 'inbound_id' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          입고관리번호 
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'inbound_id'}">
						        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
						      </c:when>
						      <c:otherwise>
						        <span class="neutral-arrow">⇅</span>
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>
                        <a href="/material/inbound/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=inbound_date&sortOrder=${cri.sortColumn == 'inbound_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          입고일자
                           <c:choose>
						      <c:when test="${cri.sortColumn == 'inbound_date'}">
						        <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
						      </c:when>
						      <c:otherwise>
						        <span class="neutral-arrow">⇅</span>
						      </c:otherwise>
						   </c:choose>
                        </a>
                      </th>
                      <th>입고상태</th>
                      <th>품명</th>
                      <th>발주수량</th>
                      <th>입고수량</th>
                      <th>발주관리번호</th>
                      <th>
                        <a href="/material/inbound/list?page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=expected_arrived_date&sortOrder=${cri.sortColumn == 'expected_arrived_date' && cri.sortOrder == 'asc' ? 'desc' : 'asc'}" 
                           class="text-white text-decoration-none">
                          예상입고일
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
                      <th>상세</th>
                      <th>입고처리</th>
                    </tr>
                  </thead>
                  
                  <!-- 테이블 내용 -->
                  <tbody>
                    <c:forEach var="inbound" items="${inboundList}">
                      <tr>
                        <!-- 입고관리번호 -->
                        <td class="font-weight-medium">${inbound.inboundId}</td>
                        
                        <!-- 입고일자 -->
                        <td>
                          <c:if test="${inbound.inboundDate != null}">
                            <fmt:formatDate value="${inbound.inboundDate}" pattern="yyyy-MM-dd"/>
                          </c:if>
                        </td>
                        
                        <!-- 입고상태 -->
                        <td>
                          <c:choose>
                            <c:when test="${inbound.status eq '미입고'}">
                              <span class="badge badge-danger">미입고</span>
                            </c:when>
                            <c:when test="${inbound.status eq '부분입고'}">
                              <span class="badge badge-warning">부분입고</span>
                            </c:when>
                            <c:when test="${inbound.status eq '입고완료'}">
                              <span class="badge badge-success">입고완료</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge badge-secondary">${inbound.status}</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        
                        <!-- 대표 품명 -->
                        <td>${inbound.representativeName}</td>
                        
                        <!-- 발주수량 -->
                        <td class="text-end">
                          <fmt:formatNumber value="${inbound.totalOrderQty}" pattern="#,###"/>
                        </td>
                        
                        <!-- 입고수량 -->
                        <td class="text-end">
                          <fmt:formatNumber value="${inbound.totalInboundQty}" pattern="#,###"/>
                        </td>
                        
                        <!-- 발주관리번호 -->
                        <td>${inbound.orderId}</td>
                        
						<!-- 예상입고일 + D-Day -->
						<td>
						  <fmt:formatDate value="${inbound.expectedArrivedDate}" pattern="yyyy-MM-dd"/>
						
						  <!-- 오늘 날짜 -->
						  <c:set var="today" value="<%=new java.util.Date()%>" />
						  <c:set var="daysDiff" value="${(inbound.expectedArrivedDate.time - today.time) / (1000*60*60*24)}" />
						
						  <!-- D-Day 뱃지 (입고완료 제외) -->
						  <c:if test="${inbound.status != '입고완료'}">
						    
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
                        <td>${inbound.handledBy}</td>
                        
                        <!-- 상세 -->
                        <td>
                          <button class="btn btn-sm btn-outline-info" 
                                  onclick="loadInboundDetail('${inbound.inboundId}')">
                            상세
                          </button>
                        </td>
                        
                        <!-- 입고처리 -->
                        <td>
                          <c:if test="${inbound.status eq '미입고'}">
                            <button class="btn btn-outline-success btn-sm"
                                    onclick="openInboundModal('${inbound.inboundId}')">
                              입고처리
                            </button>
                          </c:if>
                          <c:if test="${inbound.status eq '부분입고'}">
                            <button class="btn btn-outline-warning btn-sm"
                                    onclick="showInboundDetail('${inbound.inboundId}')">
                              추가입고
                            </button>
                          </c:if>
                        </td>
                      </tr>
                    </c:forEach>
                    
                    <!-- 데이터가 없을 때 -->
                    <c:if test="${empty inboundList}">
                      <tr>
                        <td colspan="11" class="text-center py-4">
                          <div class="text-muted">
                            <i class="ti-info-alt" style="font-size: 24px;"></i>
                            <p class="mt-2">조회된 입고 건이 없습니다.</p>
                          </div>
                        </td>
                      </tr>
                    </c:if>
                  </tbody>
                </table>
                
                <!-- 미입고 페이징 영역 -->
				<div class="mt-3 d-flex justify-content-center" id="unreceivedPagination"></div>
                
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
                       href="/material/inbound/list?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
                      &laquo;
                    </a>
                  </li>
                </c:if>
                
                <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                  <li class="page-item ${p == cri.page ? 'active' : ''}">
                    <a class="page-link" 
                       href="/material/inbound/list?page=${p}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
                      ${p}
                    </a>
                  </li>
                </c:forEach>
                
                <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
                  <li class="page-item">
                    <a class="page-link" 
                       href="/material/inbound/list?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${param.keyword}&status=${param.status}&startDate=${param.startDate}&endDate=${param.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">
                      &raquo;
                    </a>
                  </li>
                </c:if>
                
              </ul>
            </nav>
          </div>
          <!-- 페이징 처리 끝 -->
          
         <%@ include file="/WEB-INF/views/material/inbound/inboundModal.jsp" %>
        </div>
        <!-- content-wrapper 끝 -->
     <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
     </div>
     <!-- 본문.jsp main-panel ends -->
  </div>   
  <!-- container-fluid page-body-wrapper 끝 -->
</div>
<!-- container-scroller 끝-->

<script src="${pageContext.request.contextPath}/resources/js/materialInbound.js"></script>