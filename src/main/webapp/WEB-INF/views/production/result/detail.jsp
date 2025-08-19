<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>

  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">

        <!-- 제목 -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 class="mb-0">생산 실적 상세</h3>
          <div>
            <a href="${cpath}/production/result/list" class="btn btn-outline-secondary">
              <i class="ti-arrow-left"></i> 목록으로
            </a>
          </div>
        </div>

        <!-- 기본 정보 섹션 -->
        <div class="card-section">
          <h5 class="section-title">기본 정보</h5>

          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">실적번호</label>
                <input type="text" class="form-control" value="${result.resultId}" readonly>
              </div>
            </div>

            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">작업지시번호</label>
                <input type="text" class="form-control" value="${result.orderId}" readonly>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">제품명</label>
                <input type="text" class="form-control" value="${result.productName}" readonly>
              </div>
            </div>

            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">라인명</label>
                <input type="text" class="form-control" value="${result.lineName}" readonly>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">작업자명</label>
                <input type="text" class="form-control" value="${result.workerName}" readonly>
              </div>
            </div>

            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">상태</label>
                <div class="form-control" style="background-color:#f8f9fa;">
                  <c:choose>
                    <c:when test="${result.status eq 'IN_PROGRESS'}">
                      <span class="badge badge-info">생산중</span>
                    </c:when>
                    <c:when test="${result.status eq 'COMPLETED'}">
                      <span class="badge badge-success">생산 완료</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge badge-secondary">${result.status}</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </div>
        </div>

      <!-- 수량 정보 섹션 -->
<div class="card-section">
  <h5 class="section-title">수량 정보</h5>
  
  <div class="row">
    <!-- 계획수량 -->
    <div class="col-md-3">
      <div class="form-group">
        <label class="form-label">계획수량</label>
        <input type="text" class="form-control"
               value="<fmt:formatNumber value='${result.orderQty}' pattern='#,###'/>" readonly>
      </div>
    </div>

    <!-- 🔥 이번 생산수량 (메인) -->
    <div class="col-md-3">
      <div class="form-group">
        <label class="form-label" style="color:#007bff;">생산수량</label>
        <input type="text" class="form-control"
               value="<fmt:formatNumber value='${result.actualQty}' pattern='#,###'/>" readonly
               style="background-color:#e3f2fd;font-weight:bold;color:#1976d2;">
      </div>
    </div>

    <!-- 불량수량 -->
    <div class="col-md-3">
      <div class="form-group">
        <label class="form-label">불량수량</label>
        <input type="text" class="form-control"
               value="<fmt:formatNumber value='${result.defectQty}' pattern='#,###'/>" readonly>
      </div>
    </div>

    <!-- 🆕 등록시점 진행률 -->
    <div class="col-md-3">
      <div class="form-group">
        <label class="form-label" style="color:#28a745;">등록시점 진행률</label>
        <input type="text" class="form-control"
               value="<c:choose>
                        <c:when test='${not empty result.progressRate}'>
                          <fmt:formatNumber value='${result.progressRate}' pattern='#0.0'/>%
                        </c:when>
                        <c:otherwise>-</c:otherwise>
                      </c:choose>" readonly
               style="background-color:#d4edda;font-weight:bold;color:#155724;">
      </div>
    </div>
  </div>
</div>

        <!-- 시간 정보 섹션 -->
        <div class="card-section">
          <h5 class="section-title">작업 시간</h5>

          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">작업시작시간</label>
                <input type="text" class="form-control"
                       value="<c:choose>
                                <c:when test='${not empty result.startedAt}'><fmt:formatDate value='${result.startedAt}' pattern='yyyy-MM-dd HH:mm' /></c:when>
                                <c:otherwise>-</c:otherwise>
                              </c:choose>" readonly>
              </div>
            </div>

            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">작업종료시간</label>
                <input type="text" class="form-control"
                       value="<c:choose>
                                <c:when test='${not empty result.endedAt}'><fmt:formatDate value='${result.endedAt}' pattern='yyyy-MM-dd HH:mm' /></c:when>
                                <c:otherwise>-</c:otherwise>
                              </c:choose>" readonly>
              </div>
            </div>
          </div>
        </div>

        <!-- LOT 정보 섹션 -->
        <div class="card-section">
          <h5 class="section-title">LOT 정보</h5>

          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">LOT번호</label>
                <input type="text" class="form-control" value="${result.lotNo}" readonly>
              </div>
            </div>

            <div class="col-md-6">
              <div class="form-group">
                <label class="form-label">등록일시</label>
                <input type="text" class="form-control"
                       value="<c:choose>
                                <c:when test='${not empty result.createdAt}'><fmt:formatDate value='${result.createdAt}' pattern='yyyy-MM-dd HH:mm:ss' /></c:when>
                                <c:otherwise>-</c:otherwise>
                              </c:choose>" readonly>
              </div>
            </div>
          </div>
        </div>

        <!-- 불량 기록 섹션 (있는 경우) -->
        <c:if test="${not empty defectList}">
          <div class="card-section">
            <h5 class="section-title">불량 기록</h5>

            <div class="table-responsive">
              <table class="table table-bordered">
                <thead class="table-header-dark">
                  <tr>
                    <th>불량유형</th>
                    <th>불량수량</th>
                    <th>비고</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="defect" items="${defectList}">
                    <tr>
                      <td>${defect.defectType}</td>
                      <td><fmt:formatNumber value="${defect.quantity}" pattern="#,###" /></td>
                      <td>${defect.note}</td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div>
        </c:if>

      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
    <!-- 본문 끝 -->
  </div>
</div>
