<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" />

<%
  java.time.LocalDate today = java.time.LocalDate.now();
  String todayStr = today.toString();
  request.setAttribute("today", todayStr);
  
  java.util.Date now = new java.util.Date();
  request.setAttribute("now", now);
  
  java.text.SimpleDateFormat sdfHour = new java.text.SimpleDateFormat("HH");
  int currentHour = Integer.parseInt(sdfHour.format(now));
  request.setAttribute("currentHour", String.valueOf(currentHour));
%>

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
            <h3 class="font-weight-bold">출하 관리</h3>
            <h6 class="font-weight-normal text-muted">출하 대기 및 완료 내역을 관리할 수 있습니다.</h6>
          </div>

          <!-- 검색 영역 -->
          <div class="d-flex justify-content-between align-items-center mb-2">
            <form method="get" class="form-inline flex-wrap">
              <input type="hidden" name="tab" value="${param.tab}"/>
              
              <c:choose>
                <c:when test="${param.tab == 'completed'}">
                  <label for="startDate" class="mr-1">출하일자</label>
                </c:when>
                <c:otherwise>
                  <label for="startDate" class="mr-1">납기일자</label>
                </c:otherwise>
              </c:choose>
              
              <input type="date" id="startDate" name="startDate" value="${empty cri.startDate ? '' : cri.startDate}" max="${today}" class="form-control mr-2">
              <span class="mx-1">~</span>
              <input type="date" id="endDate" name="endDate" value="${empty cri.endDate ? '' : cri.endDate}" max="${today}" class="form-control mr-2">

              <input type="text" name="keyword" value="${cri.keyword}" class="form-control mr-2" placeholder="수주번호/제품명/거래처 검색">

              <button type="submit" class="btn btn-primary mr-2">검색</button>
              <a href="/shipment/list?tab=${param.tab}" class="btn btn-light">
                <i class="ti-reload"></i> 초기화
              </a>
            </form>
          </div>

          <!-- 상태별 탭 -->
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-0">
              <ul class="nav nav-underline-custom" id="shipmentTab" role="tablist">
                <!-- 출하 대기 -->
                <li class="nav-item">
                  <a class="nav-link ${empty param.tab || param.tab == 'pending' ? 'active' : ''}"
                     href="/shipment/list?tab=pending&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&page=1&perPageNum=${cri.perPageNum}">
                    출하 대기
                    <span class="badge badge-light ms-1">${pendingCount}</span>
                  </a>
                </li>

                <!-- 예약 관리 -->
                <li class="nav-item">
                  <a class="nav-link ${param.tab == 'reservation' ? 'active' : ''}"
                     href="/shipment/list?tab=reservation&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&page=1&perPageNum=${cri.perPageNum}">
                    예약 관리
                    <span class="badge badge-light ms-1">${reservationCount}</span>
                  </a>
                </li>

                <!-- 출하 완료 -->
                <li class="nav-item">
                  <a class="nav-link ${param.tab == 'completed' ? 'active' : ''}"
                     href="/shipment/list?tab=completed&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&page=1&perPageNum=${cri.perPageNum}">
                    출하 완료
                    <span class="badge badge-light ms-1">${completedCount}</span>
                  </a>
                </li>
              </ul>
            </div>

            <!-- 출하 대기 탭 내용 -->
            <c:if test="${empty param.tab || param.tab == 'pending'}">
              <div class="table-responsive">
                <!-- 메시지 표시 -->
                <c:if test="${not empty message}">
                  <div class="alert alert-${messageType} text-center fw-bold mx-auto">
                    ${message}
                  </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/shipment/process" method="post" onsubmit="return confirmShipment();">
                  <table class="table table-hover">
                    <thead>
                      <tr>
                        <th><input type="checkbox" id="selectAll"></th>
                        <th>수주번호</th>
                        <th>거래처명</th>
                        <th>제품명</th>
                        <th>수주수량</th>
                        <th>현재재고</th>
                        <th>납기요청일</th>
                        <th>출하가능여부</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach var="group" items="${groupedList}">
                        <c:set var="shippable" value="true"/>
                        <c:forEach var="item" items="${group.productList}">
                          <c:if test="${item.stockQty lt item.orderQty}">
                            <c:set var="shippable" value="false"/>
                          </c:if>
                        </c:forEach>

                        <c:set var="rowCount" value="${fn:length(group.productList)}"/>
                        
                        <c:forEach var="item" items="${group.productList}" varStatus="status">
                          <tr>
                            <c:if test="${status.first}">
                              <c:set var="orderId" value="${fn:trim(group.clOrderId)}" />
                              <c:set var="isReserved" value="${reservedMap[orderId] eq true}" />

                              <c:if test="${not empty reserveFailedId and orderId eq reserveFailedId}">
                                <c:set var="isReserved" value="false" />
                              </c:if>

                              <td rowspan="${rowCount}">
                                <div class="d-flex justify-content-center align-items-center">
                                  <input type="checkbox" name="clOrderIds" value="${orderId}" 
                                         <c:if test="${not isReserved or not shippable}">disabled</c:if> />
                                  <c:if test="${shippable}">
                                    <span class="badge border border-success text-success ml-2 d-flex align-items-center">
                                      <i class="fas fa-shipping-fast"></i> 출하
                                    </span>
                                  </c:if>
                                </div>
                              </td>
                              <td rowspan="${rowCount}">${group.clOrderId}</td>
                              <td rowspan="${rowCount}">${group.clientName}</td>
                            </c:if>

                            <td>${item.productName}</td>
                            <td class="text-end"><fmt:formatNumber value="${item.orderQty}" pattern="#,###"/></td>
                            <td class="text-end"><fmt:formatNumber value="${item.stockQty}" pattern="#,###"/></td>
                            <td>
                              <fmt:formatDate value="${item.clDeliveryDate}" pattern="yyyy-MM-dd"/>
                              <c:if test="${not empty item.clDeliveryDate}">
                                <c:if test="${(item.clDeliveryDate.time - now.time)/(1000*60*60*24) le 5 && (item.clDeliveryDate.time - now.time)/(1000*60*60*24) ge 0}">
                                  <span class="badge badge-danger ml-2">임박</span>
                                </c:if>
                              </c:if>
                            </td>
                            <td>
                              <c:set var="isReserved" value="${reservedMap[group.clOrderId] == true}"/>
                              <c:if test="${not empty reserveFailedId and reserveFailedId eq group.clOrderId}">
                                <c:set var="isReserved" value="false"/>
                              </c:if>

                              <c:choose>
                                <c:when test="${item.stockQty ge item.orderQty}">
                                  <c:choose>
                                    <c:when test="${isReserved}">
                                      <button type="button" class="btn btn-sm btn-outline-secondary"
                                              onclick="toggleReservation('${group.clOrderId}', true)">
                                        <i class="fas fa-times-circle"></i> 예약중
                                      </button>
                                    </c:when>
                                    <c:otherwise>
                                      <button type="button" class="btn btn-sm btn-outline-info"
                                              onclick="toggleReservation('${group.clOrderId}', false)">
                                        예약
                                      </button>
                                    </c:otherwise>
                                  </c:choose>
                                </c:when>
                                <c:otherwise>
                                  <span class="btn btn-sm btn-danger">부족</span>
                                </c:otherwise>
                              </c:choose>
                            </td>
                          </tr>
                        </c:forEach>
                      </c:forEach>

                      <!-- 데이터가 없을 때 -->
                      <c:if test="${empty groupedList}">
                        <tr>
                          <td colspan="8" class="text-center py-4">
                            <div class="text-muted">
                              <i class="ti-info-alt" style="font-size: 24px;"></i>
                              <p class="mt-2">출하 대기 중인 항목이 없습니다.</p>
                            </div>
                          </td>
                        </tr>
                      </c:if>
                    </tbody>
                  </table>

                  <div class="text mt-3 mb-3">
                    <button type="submit" class="btn btn-primary mb-2">
                      <i class="fas fa-shipping-fast"></i> 출하처리
                    </button>
                  </div>
                </form>
              </div>

              <!-- 출하대기 페이징 -->
              <div class="d-flex justify-content-center mt-4">
                <nav>
                  <ul class="pagination justify-content-center mt-4">
                    <c:if test="${pendingPage.cri.page > 1}">
                      <li class="page-item">
                        <a class="page-link" href="?tab=pending&page=${pendingPage.startPage - 1}&perPageNum=${pendingPage.cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}">&laquo;</a>
                      </li>
                    </c:if>
                    <c:forEach var="p" begin="${pendingPage.startPage}" end="${pendingPage.endPage}">
                      <li class="page-item ${p == pendingPage.cri.page ? 'active' : ''}">
                        <a class="page-link" href="?tab=pending&page=${p}&perPageNum=${pendingPage.cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}">${p}</a>
                      </li>
                    </c:forEach>
                    <c:if test="${pendingPage.cri.page < pendingPage.endPage}">
                      <li class="page-item">
                        <a class="page-link" href="?tab=pending&page=${pendingPage.cri.page + 1}&perPageNum=${pendingPage.cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}">&raquo;</a>
                      </li>
                    </c:if>
                  </ul>
                </nav>
              </div>
            </c:if>

            <!-- 예약 관리 탭 내용 -->
            <c:if test="${param.tab == 'reservation'}">
              <div class="table-responsive mt-4">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>LOT번호</th>
                      <th>수주번호</th>
                      <th>제품명</th>
                      <th>거래처</th>
                      <th>예약수량</th>
                      <th>예약일시</th>
                      <th>담당자</th>
                      <th>상태</th>
                      <th>상세내역</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="item" items="${reservationList}">
                      <tr>
                        <td>${item.lotNo}</td>
                        <td>${item.clOrderId}</td>
                        <td>${item.productName}</td>
                        <td>${item.clientName}</td>
                        <td class="text-end"><fmt:formatNumber value="${item.reservedQty}" pattern="#,###"/></td>
                        <td><fmt:formatDate value="${item.reservedAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                        <td>${item.manager}</td>
                        <td><span class="badge bg-warning text-dark">예약</span></td>
                        <td>
                          <button class="btn btn-sm btn-outline-info btn-reservation-detail"
                                  data-lot="${item.lotNo}"
                                  data-order="${item.clOrderId}">
                            상세
                          </button>
                        </td>
                      </tr>
                    </c:forEach>

                    <c:if test="${empty reservationList}">
                      <tr>
                        <td colspan="8" class="text-center py-4">
                          <div class="text-muted">
                            <i class="ti-info-alt" style="font-size: 24px;"></i>
                            <p class="mt-2">예약 내역이 없습니다.</p>
                          </div>
                        </td>
                      </tr>
                    </c:if>
                  </tbody>
                </table>
              </div>

              <!-- 예약관리 페이징 -->
              <div class="d-flex justify-content-center mt-4">
                <nav>
                  <ul class="pagination justify-content-center mt-4">
                    <c:if test="${reservationPage.cri.page > 1}">
                      <li class="page-item">
                        <a class="page-link" href="?tab=reservation&page=${reservationPage.startPage - 1}&perPageNum=${reservationPage.cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}">&laquo;</a>
                      </li>
                    </c:if>
                    <c:forEach var="p" begin="${reservationPage.startPage}" end="${reservationPage.endPage}">
                      <li class="page-item ${p == reservationPage.cri.page ? 'active' : ''}">
                        <a class="page-link" href="?tab=reservation&page=${p}&perPageNum=${reservationPage.cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}">${p}</a>
                      </li>
                    </c:forEach>
                    <c:if test="${reservationPage.cri.page < reservationPage.endPage}">
                      <li class="page-item">
                        <a class="page-link" href="?tab=reservation&page=${reservationPage.cri.page + 1}&perPageNum=${reservationPage.cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}">&raquo;</a>
                      </li>
                    </c:if>
                  </ul>
                </nav>
              </div>
            </c:if>

            <!-- 출하 완료 탭 내용 -->
            <c:if test="${param.tab == 'completed'}">
              <!-- 출하취소 안내 -->
              <div class="text-right mb-3">
                <h5 class="fw-bold text-danger" style="display: inline-block; background-color: #fff3cd; padding: 6px 12px; border: 1px solid #ffeeba; border-radius: 8px;">
                  <i class="fas fa-exclamation-triangle me-1"></i>
                  출하취소는 당일 오후 2시 이전까지만 가능
                </h5>
              </div>

              <div class="table-responsive mt-4">
                <table class="table table-hover">
                  <thead>
                    <tr>
                       <th>
      
      <a href="/shipment/list?tab=completed&page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=clOrderId&sortOrder=${cri.sortColumn eq 'clOrderId' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}" 
         class="text-white text-decoration-none">
        수주번호
        <c:choose>
          <c:when test="${cri.sortColumn eq 'clOrderId'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>
    <th>
     
      <a href="/shipment/list?tab=completed&page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=clientName&sortOrder=${cri.sortColumn eq 'clientName' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}" 
         class="text-white text-decoration-none">
        거래처명
        <c:choose>
          <c:when test="${cri.sortColumn eq 'clientName'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>
    <th>
     
      <a href="/shipment/list?tab=completed&page=${cri.page}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=deliveryDate&sortOrder=${cri.sortColumn eq 'deliveryDate' and cri.sortOrder eq 'asc' ? 'desc' : 'asc'}" 
         class="text-white text-decoration-none">
        출하일자
        <c:choose>
          <c:when test="${cri.sortColumn eq 'deliveryDate'}">
            <span>${cri.sortOrder eq 'asc' ? '▲' : '▼'}</span>
          </c:when>
          <c:otherwise>
            <span class="neutral-arrow">⇅</span>
          </c:otherwise>
        </c:choose>
      </a>
    </th>
                      <th>상세내역</th>
                      <th>관리</th>
                    </tr>
                  </thead>
                  <tbody>                 
                    <c:forEach var="group" items="${groupedCompletedList}" varStatus="status">
                      <tr>
                        <td>${group.clOrderId}</td>
                        <td>${group.clientName}</td>
                        <td>
                          <fmt:formatDate value="${group.deliveryDate}" pattern="yyyy-MM-dd" /><br />
                          <small class="text-muted">
                            <i class="bi bi-clock me-1"></i>
                            <fmt:formatDate value="${group.deliveryDate}" pattern="HH:mm:ss" />
                          </small>
                        </td>
                        <td>
                          <button type="button" class="btn btn-sm btn-outline-info"
                                  data-bs-toggle="modal"
                                  data-bs-target="#modal-${status.index}">
                            상세
                          </button>
                        </td>
                        <td>
                          <fmt:formatDate value="${group.productList[0].createdAt}" pattern="yyyy-MM-dd" var="createdDate" />
                          <c:choose>
                            <c:when test="${createdDate == today and currentHour lt 18}">
                              <form method="post" action="/shipment/cancel" style="display: inline;" onsubmit="return confirm('정말로 출하를 취소하시겠습니까?');">
                                <input type="hidden" name="deliveryId" value="${group.productList[0].deliveryId}" />
                                <button type="submit" class="btn btn-sm btn-outline-danger">출하 취소</button>
                              </form>
                            </c:when>
                            <c:otherwise>
                              <span class="btn btn-sm btn-success">출하 완료</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                      </tr>
                    </c:forEach>

                    <c:if test="${empty groupedCompletedList}">
                      <tr>
                        <td colspan="5" class="text-center py-4">
                          <div class="text-muted">
                            <i class="ti-info-alt" style="font-size: 24px;"></i>
                            <p class="mt-2">출하 완료된 항목이 없습니다.</p>
                          </div>
                        </td>
                      </tr>
                    </c:if>
                  </tbody>
                </table>
              </div>

              <!-- 출하완료 페이징 -->
              <div class="d-flex justify-content-center mt-4">
                <nav>
                  <ul class="pagination justify-content-center mt-4">
                    <c:if test="${pageMaker.cri.page > 1}">
                      <li class="page-item">
                        <a class="page-link" href="?tab=completed&page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
                      </li>
                    </c:if>
                    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                      <li class="page-item ${p == cri.page ? 'active' : ''}">
                        <a class="page-link" href="?tab=completed&page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
                      </li>
                    </c:forEach>
                    <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
                      <li class="page-item">
                        <a class="page-link" href="?tab=completed&page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&startDate=${cri.startDate}&endDate=${cri.endDate}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
                      </li>
                    </c:if>
                  </ul>
                </nav>
              </div>
            </c:if>
          </div>
        </div>
      </div>
      
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>
</div>

<!-- 출하 완료 상세 모달들 -->
<c:forEach var="group" items="${groupedCompletedList}" varStatus="status">
  <div class="modal fade" id="modal-${status.index}" tabindex="-1" aria-labelledby="modalLabel-${status.index}" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header bg-primary">
          <h5 class="modal-title text-white" id="modalLabel-${status.index}">
            출하 상세 내역 - ${group.clOrderId}
          </h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
        </div>
        <div class="modal-body">
          <table class="table table-bordered table-sm">
            <thead class="table-secondary">
              <tr>
                <th class="bg-light text-dark">출하 ID</th>
                <th class="bg-light text-dark">제품명</th>
                <th class="bg-light text-dark">LOT번호</th>
                <th class="bg-light text-dark">출하 수량</th>
                <th class="bg-light text-dark">송장번호</th>
                <th class="bg-light text-dark">상태</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="item" items="${group.productList}">
                <tr>
                  <td>${item.deliveryId}</td>
                  <td>${item.productName}</td>
                  <td>${item.lotNo}</td>
                  <td class="text-end"><fmt:formatNumber value="${item.deliveryQty}" pattern="#,###"/></td>
                  <td>${item.trackingNumber}</td>
				   <td>
				  <c:choose>
				    <c:when test="${item.deliveryStatus == '배송준비'}">
				      <span class="badge bg-warning text-dark">${item.deliveryStatus}</span>
				    </c:when>
				    <c:when test="${item.deliveryStatus == '배송완료'}">
				      <span class="badge bg-success text-white">${item.deliveryStatus}</span>
				    </c:when>
				    <c:when test="${item.deliveryStatus == 'CANCELLED'}">
				      <span class="badge bg-secondary text-white">${item.deliveryStatus}</span>
				    </c:when>
				    <c:otherwise>
				      <span class="badge bg-light text-dark">${item.deliveryStatus}</span>
				    </c:otherwise>
				  </c:choose>
				</td>

                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
        </div>
      </div>
    </div>
  </div>
</c:forEach>

<!-- 예약 상세정보 모달 -->
<div class="modal fade" id="reservationDetailModal" tabindex="-1" aria-labelledby="reservationDetailModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header bg-primary text-white">
        <h5 class="modal-title fw-bold" id="reservationDetailModalLabel">예약 상세 정보</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <table class="table table-bordered table-sm">
          <thead class="table-light">
            <tr>
              <th>LOT 번호</th>
              <th>수주번호</th>
              <th>제품명</th>
              <th>거래처</th>
              <th>예약수량</th>
              <th>예약일시</th>
              <th>유통기한</th>
              <th>현재고</th>
              <th>납기 요청일</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td id="modal-lot-no"></td>
              <td id="modal-cl-order-id"></td>
              <td id="modal-product-name"></td>
              <td id="modal-client-name"></td>
              <td id="modal-reserved-qty"></td>
              <td id="modal-reserved-at"></td>
              <td id="modal-expire-date"></td>
              <td id="modal-current-stock"></td>
              <td id="modal-delivery-date"></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>

<!-- JavaScript -->
<script src="${pageContext.request.contextPath}/resources/js/shipment.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- 전체 선택 스크립트 -->
<script>
  // 전체 선택/해제
  document.getElementById('selectAll').onclick = function() {
    var checkboxes = document.getElementsByName('clOrderIds');
    for (var checkbox of checkboxes) {
      if (!checkbox.disabled) {
        checkbox.checked = this.checked;
      }
    }
  }

  // 출하 처리 확인
  function confirmShipment() {
    var checkedBoxes = document.querySelectorAll('input[name="clOrderIds"]:checked');
    if (checkedBoxes.length === 0) {
      alert('출하 처리할 항목을 선택해주세요.');
      return false;
    }
    return confirm('선택한 ' + checkedBoxes.length + '건의 출하를 처리하시겠습니까?');
  }

  // 예약 토글 함수
  function toggleReservation(orderId, isReserved) {
    var action = isReserved ? '해제' : '등록';
    if (confirm('예약을 ' + action + '하시겠습니까?')) {
      var form = document.createElement('form');
      form.method = 'post';
      form.action = isReserved ? '/shipment/unreserve' : '/shipment/reserve';
      
      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = 'clOrderId';
      input.value = orderId;
      
      form.appendChild(input);
      document.body.appendChild(form);
      form.submit();
    }
  }

  // 예약 상세 버튼 이벤트
  document.addEventListener('DOMContentLoaded', function() {
    const detailButtons = document.querySelectorAll('.btn-reservation-detail');
    
    detailButtons.forEach(button => {
      button.addEventListener('click', function() {
        const lotNo = this.getAttribute('data-lot');
        const orderId = this.getAttribute('data-order');
        
        // AJAX로 예약 상세 정보 조회
        fetch(`/shipment/reservation/detail?lotNo=${lotNo}&orderId=${orderId}`)
          .then(response => response.json())
          .then(data => {
            // 모달에 데이터 설정
            document.getElementById('modal-lot-no').textContent = data.lotNo;
            document.getElementById('modal-cl-order-id').textContent = data.clOrderId;
            document.getElementById('modal-product-name').textContent = data.productName;
            document.getElementById('modal-client-name').textContent = data.clientName;
            document.getElementById('modal-reserved-qty').textContent = data.reservedQty;
            document.getElementById('modal-reserved-at').textContent = data.reservedAt;
            document.getElementById('modal-expire-date').textContent = data.expireDate;
            document.getElementById('modal-current-stock').textContent = data.currentStock;
            document.getElementById('modal-delivery-date').textContent = data.deliveryDate;
            
            // 모달 표시
            var modal = new bootstrap.Modal(document.getElementById('reservationDetailModal'));
            modal.show();
          })
          .catch(error => {
            console.error('Error:', error);
            alert('상세 정보를 불러오는데 실패했습니다.');
          });
      });
    });
  });
</script>

<style>

/* 임박 배지 애니메이션 */
.badge-danger {
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% {
        box-shadow: 0 0 0 0 rgba(220, 53, 69, 0.7);
    }
    70% {
        box-shadow: 0 0 0 10px rgba(220, 53, 69, 0);
    }
    100% {
        box-shadow: 0 0 0 0 rgba(220, 53, 69, 0);
    }
}

</style>