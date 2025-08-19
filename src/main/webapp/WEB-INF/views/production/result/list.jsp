<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ include file="/WEB-INF/views/main/layout_head.jsp"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<div class="container-scroller">
	<%@ include file="/WEB-INF/views/main/top.jsp"%>
	<div class="container-fluid page-body-wrapper">
		<%@ include file="/WEB-INF/views/main/sidebar.jsp"%>

		<div class="main-panel">
			<div class="content-wrapper">

				<div class="mb-4">
					<h3 class="mb-0">생산 실적 목록</h3>
				</div>

				<!-- 검색 -->
				<form method="get" action="${cpath}/production/result/list">
					<div class="row g-3 mb-4 align-items-end">
						<div class="col-md-2">
							<label class="form-label text-muted small">시작일</label> <input
								type="date" name="startDate" value="${cri.startDate}"
								class="form-control form-control-sm">
						</div>
						<div
							class="col-md-auto d-flex align-items-center justify-content-center">
							<span class="text-muted">~</span>
						</div>
						<div class="col-md-2">
							<label class="form-label text-muted small">종료일</label> <input
								type="date" name="endDate" value="${cri.endDate}"
								class="form-control form-control-sm">
						</div>
						<div class="col-md-4">
							<label class="form-label text-muted small">통합검색</label> <input
								type="text" name="keyword" value="${cri.keyword}"
								class="form-control" placeholder="작업지시번호, 제품명, 작업자, 라인명 검색">
						</div>
						<div class="col-md-auto d-flex gap-2">
							<button type="submit" class="btn btn-primary"
								style="background-color: #1C355E; border-color: #1C355E;">
								<i class="ti-search"></i> 검색
							</button>
							<a href="${cpath}/production/result/list" class="btn btn-light">
								<i class="ti-reload"></i> 초기화
							</a>
						</div>
						<input type="hidden" name="perPageNum" value="${cri.perPageNum}">
						<input type="hidden" name="status" value="${cri.status}">
					</div>
				</form>

				<!-- 탭: IN_PROGRESS / COMPLETED 만 -->
				<div class="d-flex justify-content-between align-items-center mb-3">
					<ul class="nav nav-underline-custom">
						<li class="nav-item"><a
							class="nav-link ${empty cri.status ? 'active' : ''}"
							href="${cpath}/production/result/list?startDate=${cri.startDate}&endDate=${cri.endDate}&keyword=${cri.keyword}&perPageNum=${cri.perPageNum}">
								전체 </a></li>
						<li class="nav-item"><a
							class="nav-link ${cri.status eq 'IN_PROGRESS' ? 'active' : ''}"
							href="${cpath}/production/result/list?status=IN_PROGRESS&startDate=${cri.startDate}&endDate=${cri.endDate}&keyword=${cri.keyword}&perPageNum=${cri.perPageNum}">
								생산중 </a></li>
						<li class="nav-item"><a
							class="nav-link ${cri.status eq 'COMPLETED' ? 'active' : ''}"
							href="${cpath}/production/result/list?status=COMPLETED&startDate=${cri.startDate}&endDate=${cri.endDate}&keyword=${cri.keyword}&perPageNum=${cri.perPageNum}">
								생산 완료 </a></li>
					</ul>

					<!-- 신규등록: 폼으로 이동(폼에서 IN_PROGRESS만 선택 가능) -->
					<a href="${cpath}/production/result/form" class="btn btn-success">신규등록</a>
				</div>

				<!-- 목록 -->
				<div class="table-responsive">
					<table class="table table-hover">
						<thead class="table-header-dark">
							<tr>
								<th>실적번호</th>
								<th>작업지시번호</th>
								<th>제품명</th>
								<th>라인명</th>
								<th>LOT번호</th>
								<th>계획수량</th>
								<th>생산수량</th>
								<th>불량품</th>
								<th>작업자</th>
								<th>상태</th>
								<th>작업시간</th>
								<th>액션</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="row" items="${list}">
								<!-- 혹시 WAITING이 넘어와도 화면에서 제외(서버에서도 걸러짐이 정석) -->
								<c:if test="${row.status ne 'WAITING'}">
									<tr>
										<td>${row.resultId}</td>
										<td>${empty row.orderId ? '-' : row.orderId}</td>
										<td>${empty row.productName ? '-' : row.productName}</td>
										<td>${empty row.lineName ? '-' : row.lineName}</td>
										<td>${empty row.lotNo ? '-' : row.lotNo}</td>
										<td><c:choose>
												<c:when test="${empty row.orderQty}">-</c:when>
												<c:otherwise>
													<fmt:formatNumber value="${row.orderQty}" pattern="#,###" />
												</c:otherwise>
											</c:choose></td>
										<td><fmt:formatNumber value="${row.actualQty}"
												pattern="#,###" /></td>
										<td><fmt:formatNumber value="${row.defectQty}"
												pattern="#,###" /></td>
										<td>${row.workerName}</td>
										<td><c:choose>
												<c:when test="${row.status eq 'IN_PROGRESS'}">
													<span class="badge badge-info">생산중</span>
												</c:when>
												<c:when test="${row.status eq 'COMPLETED'}">
													<span class="badge badge-success">생산 완료</span>
												</c:when>
												<c:otherwise>
													<span class="badge badge-light">${empty row.status ? '미등록' : row.status}</span>
												</c:otherwise>
											</c:choose></td>
										<td><c:if test="${not empty row.startedAt}">
												<div>
													<fmt:formatDate value="${row.startedAt}"
														pattern="MM-dd HH:mm" />
												</div>
											</c:if> <c:if test="${not empty row.endedAt}">
												<small class="text-muted">~<fmt:formatDate
														value="${row.endedAt}" pattern="HH:mm" /></small>
											</c:if></td>
										<td>
											<div class="d-flex flex-column gap-1">
												<a class="btn btn-sm btn-outline-info"
													href="${cpath}/production/result/detail?resultId=${row.resultId}">
													상세 </a>
											</div>
										</td>
									</tr>
								</c:if>
							</c:forEach>

							<c:if test="${empty list}">
								<tr>
									<td colspan="12" class="text-center text-muted">조회 결과가
										없습니다.</td>
								</tr>
							</c:if>
						</tbody>
					</table>
				</div>

				<!-- 페이징 -->
				<div class="d-flex justify-content-center mt-4">
					<nav>
						<ul class="pagination justify-content-center mt-4">
							<c:if test="${pageMaker.cri.page > 1}">
								<li class="page-item"><a class="page-link"
									href="${cpath}/production/result/list?page=${pageMaker.cri.page - 1}&perPageNum=${pageMaker.cri.perPageNum}&keyword=${pageMaker.cri.keyword}&status=${pageMaker.cri.status}&startDate=${pageMaker.cri.startDate}&endDate=${pageMaker.cri.endDate}"
									style="color: #1C355E;">&laquo;</a></li>
							</c:if>

							<c:forEach var="p" begin="${pageMaker.startPage}"
								end="${pageMaker.endPage}">
								<li class="page-item ${p == pageMaker.cri.page ? 'active' : ''}">
									<a class="page-link"
									href="${cpath}/production/result/list?page=${p}&perPageNum=${pageMaker.cri.perPageNum}&keyword=${pageMaker.cri.keyword}&status=${pageMaker.cri.status}&startDate=${pageMaker.cri.startDate}&endDate=${pageMaker.cri.endDate}"
									style="${p == pageMaker.cri.page ? 'background-color:#1C355E;border-color:#1C355E;color:white;' : 'color:#1C355E;'}">
										${p} </a>
								</li>
							</c:forEach>

							<c:if test="${pageMaker.cri.page < pageMaker.endPage}">
								<li class="page-item"><a class="page-link"
									href="${cpath}/production/result/list?page=${pageMaker.cri.page + 1}&perPageNum=${pageMaker.cri.perPageNum}&keyword=${pageMaker.cri.keyword}&status=${pageMaker.cri.status}&startDate=${pageMaker.cri.startDate}&endDate=${pageMaker.cri.endDate}"
									style="color: #1C355E;">&raquo;</a></li>
							</c:if>
						</ul>
					</nav>
				</div>

			</div>
			<%@ include file="/WEB-INF/views/main/layout_footer.jsp"%>
		</div>
	</div>
</div>
