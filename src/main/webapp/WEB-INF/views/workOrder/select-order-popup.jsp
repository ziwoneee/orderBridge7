<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>

<head>
<title>확정 수주 선택</title>
<meta name="viewport" content="width=device-width, initial-scale=1.1">


<style>
/* 선택된 행 강조 */
.order-row.selected {
    background-color: #e8f1ff !important;
}

/* 선택 정보 표시 영역 */
#selectionInfo {
    position: sticky;
    top: 0;
    z-index: 100;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    margin-bottom: 20px;
}

/* 전체 선택 체크박스 스타일 */
#selectAll {
    cursor: pointer;
    width: 18px;
    height: 18px;
}

/* 체크박스 정렬 */
.order-checkbox, #selectAll {
    margin: 0 auto;
    display: block;
}

/* 테이블 헤더 고정 (선택사항) */
.table-container {
    max-height: 450px;
    overflow-y: auto;
}

.table thead th {
    position: sticky;
    top: 0;
    background-color: #f8f9fa;
    z-index: 10;
}

/* 비활성화된 버튼 스타일 */
#mergeSelectBtn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}
</style>

</head>

<body>
	<!-- 제목 -->
	<div class="page-title">
		<i class="fas fa-clipboard-list"></i> 확정 수주 선택
	</div>

	<!-- 검색 영역 -->
	<div class="search-container">
		<form method="get" action="/workorder/select-order">
			<div class="search-form-wrapper">
				<input type="text" name="keyword" class="form-control search-input"
					placeholder="수주번호, 제품명, 거래처명 검색" value="${cri.keyword}">
				<button type="submit" class="btn search-btn">
					<i class="fas fa-search"></i> 검색
				</button>
			</div>
		</form>
	</div>
	
	<!-- 선택 정보 표시 영역 (동적 생성되는 곳) -->
	<!-- JS에서 자동으로 추가됨 -->
	
	<!-- 병합 버튼 -->
	<div class="btn-align-right mb-3">
		<button id="mergeSelectBtn" class="btn btn-primary" 
				style="background-color: #1C355E; border-color: #1C355E;" disabled>
			작업지시 등록
		</button>
	</div>

	<!-- 수주 목록 테이블 -->
	<div class="table-container">
		<div class="table-responsive">
			<table class="table table-hover">
				<thead>
					<tr>
						<th width="50">
							<input type="checkbox" id="selectAll" title="전체 선택">
						</th>
						<th>수주번호</th>
						<th>거래처</th>
						<th>제품명</th>
						<th>수주일</th>
						<th>납기일</th>
						<th class="text-right">수주수량</th>
						<th class="text-right">가용수량</th>
						<th class="text-right">생산필요</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="order" items="${orderList}">
						<tr class="order-row">
							<!-- 체크박스 -->
							<c:set var="formattedDueDate">
							  <fmt:formatDate value="${order.dueDate}" pattern="yyyy-MM-dd" />
							</c:set>
							<td class="text-center">
								<input type="checkbox" class="order-checkbox"
									data-cl-order-id="${order.clOrderId}"
									data-product-id="${order.productId}"
									data-product-name="${order.productName}"
									data-client-name="${order.clientName}"
									data-order-qty="${order.orderQty}"
									data-due-date="${formattedDueDate}">
							</td>
							
							<!-- 수주번호 -->
							<td class="font-weight-medium">${order.clOrderId}</td>
							
							<!-- 거래처 -->
							<td>
								<c:choose>
									<c:when test="${not empty order.clientName}">
										${order.clientName}
									</c:when>
									<c:otherwise>
										<span class="text-muted">-</span>
									</c:otherwise>
								</c:choose>
							</td>
							
							<!-- 제품명 -->
							<td>
								<span class="badge badge-info">${order.productName}</span>
							</td>
							
							<!-- 수주일 -->
							<td>
								<fmt:formatDate value="${order.clOrderDate}" pattern="yyyy-MM-dd" />
							</td>
							
							<!-- 납기일 -->
							<td>
								<c:choose>
									<c:when test="${not empty order.dueDate}">
										<fmt:formatDate value="${order.dueDate}" pattern="yyyy-MM-dd" />
									</c:when>
									<c:otherwise>
										<span class="text-muted">-</span>
									</c:otherwise>
								</c:choose>
							</td>
							
							<!-- 수주 수량 -->
							<td class="text-right">
								<fmt:formatNumber value="${order.orderQty}" pattern="#,##0" />
							</td>
							
							<!-- 가용 수량 -->
							<td class="text-right">
								<c:choose>
									<c:when test="${order.availableQty > 0}">
										<span class="text-success">
											<fmt:formatNumber value="${order.availableQty}" pattern="#,##0" />
										</span>
									</c:when>
									<c:otherwise>
										<span class="text-muted">0</span>
									</c:otherwise>
								</c:choose>
							</td>
							
							<!-- 생산 필요 수량 -->
							<td class="text-right">
								<c:choose>
									<c:when test="${order.requiredQty > 0}">
										<span class="text-danger font-weight-bold">
											<fmt:formatNumber value="${order.requiredQty}" pattern="#,##0" />
										</span>
									</c:when>
									<c:otherwise>
										<span class="text-muted">0</span>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</c:forEach>

					<!-- 데이터 없을 때 메시지 -->
					<c:if test="${empty orderList}">
						<tr>
							<td colspan="9" class="text-center py-5">
								<div class="empty-message">
									<i class="fas fa-inbox fa-3x mb-3 text-muted"></i>
									<p class="text-muted mb-0">확정된 수주가 없습니다.</p>
								</div>
							</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>

	<!-- 페이징 영역 -->
	<c:if test="${totalCount > 0}">
		<div class="pagination-container">
			<nav aria-label="페이지 네비게이션">
				<ul class="pagination justify-content-center">
					<!-- 이전 버튼 -->
					<c:if test="${cri.page > 1}">
						<li class="page-item">
							<a class="page-link" href="?page=${cri.page - 1}&keyword=${cri.keyword}">
								<i class="fas fa-chevron-left"></i>
							</a>
						</li>
					</c:if>

					<!-- 페이지 번호 -->
					<c:set var="startPage" value="${cri.page - 2 > 0 ? cri.page - 2 : 1}" />
					<c:set var="endPage" value="${startPage + 4 > totalPages ? totalPages : startPage + 4}" />
					
					<!-- 시작 페이지 재조정 -->
					<c:if test="${endPage - startPage < 4 && startPage > 1}">
						<c:set var="startPage" value="${endPage - 4 > 0 ? endPage - 4 : 1}" />
					</c:if>

					<c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
						<li class="page-item ${cri.page == pageNum ? 'active' : ''}">
							<a class="page-link" href="?page=${pageNum}&keyword=${cri.keyword}">
								${pageNum}
							</a>
						</li>
					</c:forEach>

					<!-- 다음 버튼 -->
					<c:if test="${cri.page < totalPages}">
						<li class="page-item">
							<a class="page-link" href="?page=${cri.page + 1}&keyword=${cri.keyword}">
								<i class="fas fa-chevron-right"></i>
							</a>
						</li>
					</c:if>
				</ul>
			</nav>

			<!-- 총 개수 -->
			<div class="text-center mt-2 text-muted">
				<small>총 ${totalCount}건 (${cri.page}/${totalPages} 페이지)</small>
			</div>
		</div>
	</c:if>
	
	<!-- jQuery -->
	<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
	
	<!-- select-order JS -->
	<script src="${pageContext.request.contextPath}/resources/js/select-order.js"></script>
	
	<!-- Bootstrap JS -->
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.6.0/js/bootstrap.bundle.min.js"></script>

	<script>
	// 추가 UI 효과 (선택사항)
	$(document).ready(function() {
		// 체크박스 변경 시 행 강조
		$(document).on('change', '.order-checkbox', function() {
			if ($(this).prop('checked')) {
				$(this).closest('tr').addClass('selected');
			} else {
				$(this).closest('tr').removeClass('selected');
			}
		});
		
		// 검색 입력창 포커스
		$('.search-input').focus();
		
		// 엔터키 검색 방지 (form에서 처리)
		$('.search-input').on('keydown', function(e) {
			if (e.keyCode === 13) {
				$(this).closest('form').submit();
			}
		});
	});
	</script>

</body>
</html>