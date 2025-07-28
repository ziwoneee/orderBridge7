<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>
    <div class="main-panel">
      <div class="content-wrapper">
        <div class="row">
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">실시간 재고 조회</h3>
          </div>

          <!-- 검색 영역 -->
          <div class="col-12 mb-3">
            <form id="stockForm" action="${pageContext.request.contextPath}/product/stocklist" method="get" class="form-inline">
              <input type="hidden" name="mode" value="${param.mode}" />
              <input type="date" name="startDate" class="form-control mr-2" value="${param.startDate}">
              <span>~</span>
              <input type="date" name="endDate" class="form-control mx-2" value="${param.endDate}">
              <input type="text" name="keyword" class="form-control mx-2" placeholder="제품명, LOT 번호 검색" value="${param.keyword}">
              <button type="button" class="btn btn-success mr-1" onclick="viewStock()">현재고 조회</button>
              <button type="button" class="btn btn-primary" onclick="viewAll()">전체 내역 조회</button>
            </form>
          </div>

          <!-- 재고 테이블 -->
          <div class="col-12">
            <div class="table-responsive">
              <table id="stockTable" class="table table-bordered table-hover">
                <thead class="thead-light">
                  <tr>
                    <th>제품명</th>
                    <th>LOT 번호</th>
                    <th>현재고</th>
                    <th>안전재고</th>
                    <th>생산일자</th>
                    <th>유통기한</th>            
                    <th>재고상태</th>
                    <th>상세내역</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="item" items="${stockList}">
                    <tr>
                      <td>${item.productName}</td>
                      <td>${item.lotNo}</td>
                      <td>${item.stockQty}</td>
                      <td>${item.safeQty}</td>
                      <td><fmt:formatDate value="${item.regDate}" pattern="yyyy-MM-dd"/></td>
                      <td><fmt:formatDate value="${item.expireDate}" pattern="yyyy-MM-dd"/></td>
                      <td>
                        <c:choose>
                          <c:when test="${item.stockQty == 0}">
                            <span class="status-complete">완료</span>
                          </c:when>
                          <c:when test="${item.stockQty lt item.safeQty}">
                            <span class="status-low">부족</span>
                          </c:when>
                          <c:otherwise>
                            <span class="status-normal">정상</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td>
                        <button class="btn btn-outline-secondary btn-sm"
                                data-toggle="modal"
                                data-target="#detailModal"
                                data-product="${item.productName}"
                                data-lot="${item.lotNo}">
                          내역확인
                        </button>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>

            <!-- 다운로드 버튼 -->
            <div class="d-flex justify-content-end mt-3">
              <a href="#" class="btn btn-warning">+ 목록 다운로드</a>
            </div>
          </div>

          <!-- 페이징 -->
          <div class="col-12 d-flex justify-content-center mt-4">
            <nav>
              <ul class="pagination">
                <c:if test="${pageMaker.cri.page > 1}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
                  </li>
                </c:if>
                <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                  <li class="page-item ${p == cri.page ? 'active' : ''}">
                    <a class="page-link" href="?page=${p}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
                  </li>
                </c:forEach>
                <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.cri.page + 1}&perPageNum=${cri.perPageNum}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
                  </li>
                </c:if>
              </ul>
            </nav>
          </div>

        </div> <!-- row -->
        <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
      </div> <!-- content-wrapper -->
    </div> <!-- main-panel -->
  </div> <!-- container-fluid -->
</div> <!-- container-scroller -->

<!-- 모달 영역 -->
<div class="modal fade" id="detailModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">
          <span id="modalProductName"></span> (<span id="modalLotNo"></span>) 입출고 내역
        </h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <div class="modal-body">
        <table class="table table-sm table-bordered">
          <thead>
            <tr>
              <th>입력일</th>
              <th>구분</th>
              <th>수량</th>
              <th>비고</th>
            </tr>
          </thead>
          <tbody id="modalTransactionBody">
            <!-- Ajax로 동적 삽입 -->
          </tbody>
        </table>
        <div class="alert alert-info mt-3">제품의 실시간 입고/출고 내역을 확인할 수 있습니다.</div>
      </div>
    </div>
  </div>
</div>

<!-- JS 스크립트 -->
<script>
  $('#detailModal').on('show.bs.modal', function (event) {
    const button = $(event.relatedTarget);
    const product = button.data('product');
    const lot = button.data('lot');
    $('#modalProductName').text(product);
    $('#modalLotNo').text(lot);

    $.getJSON("/product/transaction", { product: product, lot: lot }, function(data) {
      let html = data.length === 0
        ? "<tr><td colspan='4'>내역이 없습니다.</td></tr>"
        : data.map(row => `
          <tr>
            <td>${row.reg_date}</td>
            <td>${row.type}</td>
            <td>${row.qty}</td>
            <td>${row.memo || ''}</td>
          </tr>`).join('');
      $('#modalTransactionBody').html(html);
    });
  });

  function viewStock() {
    document.querySelector('input[name="mode"]').value = 'stock';
    document.getElementById('stockForm').submit();
  }

  function viewAll() {
    document.querySelector('input[name="mode"]').value = 'all';
    document.getElementById('stockForm').submit();
  }
</script>
