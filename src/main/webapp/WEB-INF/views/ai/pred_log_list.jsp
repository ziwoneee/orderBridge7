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
			      <h3 class="font-weight-bold">예측 결과 로그</h3>
			    </div>
			  </div>
			</div>
			
			<!-- 검색 영역 -->
			<div class="col-12 mb-3">
			  <form class="forms-sample" method="get" id="searchForm">
			    <div class="row align-items-end">
			      <div class="col-md-3 form-group">
			        <label class="form-label text-muted small">키워드 검색</label>
			        <input type="text" name="q" value="${q}" class="form-control" placeholder="request_type / 내용 검색"/>
			      </div>
			      <div class="col-md-2 form-group">
			        <label class="form-label text-muted small">시작일</label>
			        <input type="date" name="from" value="<fmt:formatDate value='${from}' pattern='yyyy-MM-dd'/>" class="form-control"/>
			      </div>
			      <div class="col-md-auto form-group text-center px-2">
			        <label class="form-label text-muted small">&nbsp;</label>
			        <div class="mt-2">~</div>
			      </div>
			      <div class="col-md-2 form-group">
			        <label class="form-label text-muted small">종료일</label>
			        <input type="date" name="to" value="<fmt:formatDate value='${to}' pattern='yyyy-MM-dd'/>" class="form-control"/>
			      </div>
			      
			      <div class="col-md-2 form-group">
			        <label class="form-label text-muted small">페이지당</label>
			        <select name="perPageNum" class="form-control"
			                onchange="this.form.submit();"> <!-- ✅ 간단하게 변경 -->
			          <c:set var="sizes" value="${fn:split('10,25,50,100,200', ',')}"/>
			          <c:forEach var="n" items="${sizes}">
			            <option value="${n}" <c:if test="${cri.perPageNum == n}">selected</c:if>>${n}</option>
			          </c:forEach>
			        </select>
			      </div>
			      
			      <div class="col-md-3 form-group">
			        <button type="submit" class="btn btn-primary me-2">
			          <i class="ti-search"></i> 검색
			        </button>
			        <a href="${pageContext.request.contextPath}/ai/pred-logs" class="btn btn-light">
			          <i class="ti-reload"></i> 초기화
			        </a>
			      </div>
			    </div>
			  </form>
			</div>
			
			<!-- 예측 결과 로그 목록 -->
			<div class="col-12">
			  <div class="table-responsive">
			    <table class="table table-hover">
			      <thead style="background-color: #1C355E; color: white;">
			        <tr>
			          <th>시각</th>
			          <th>요청자</th>
			          <th>유형</th>
			          <th>상세</th>
			        </tr>
			      </thead>
			      <tbody>
			        <c:forEach var="x" items="${logs}">
			          <tr>
			            <td><fmt:formatDate value="${x.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
			            <td>${x.requestedBy}</td>
			            <td>
			              <span class="badge badge-info">${x.requestType}</span>
			            </td>
			            <td>
			              <details>
			                <summary class="btn btn-sm btn-outline-info" style="cursor: pointer;">JSON 보기</summary>
			                <div class="mt-2">
			                  <div class="row">
			                    <div class="col-md-6">
			                      <h6 class="text-muted">입력 데이터</h6>
			                      <pre style="white-space:pre-wrap; background-color: #f8f9fa; padding: 10px; border-radius: 4px; font-size: 0.85em; max-height: 200px; overflow-y: auto;">${x.inputDataJson}</pre>
			                    </div>
			                    <div class="col-md-6">
			                      <h6 class="text-muted">예측 결과</h6>
			                      <pre style="white-space:pre-wrap; background-color: #f8f9fa; padding: 10px; border-radius: 4px; font-size: 0.85em; max-height: 200px; overflow-y: auto;">${x.predictionResultJson}</pre>
			                    </div>
			                  </div>
			                </div>
			              </details>
			            </td>
			          </tr>
			        </c:forEach>
			        <c:if test="${empty logs}">
			          <tr>
			            <td colspan="4" class="text-center py-4">
			              <div class="text-muted">
			                <i class="ti-info-alt" style="font-size: 24px;"></i>
			                <p class="mt-2">조회된 로그가 없습니다.</p>
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
          <c:if test="${not empty pageMaker}">
		    <div class="d-flex justify-content-center mt-4">
		      <nav>
		        <ul class="pagination justify-content-center">
		          
		          <c:if test="${pageMaker.cri.page > 1}">
		            <li class="page-item">
		              <a class="page-link" 
		                 href="?page=${pageMaker.startPage - 1}&perPageNum=${pageMaker.cri.perPageNum}&q=${param.q}&from=${param.from}&to=${param.to}">
		                &laquo;
		              </a>
		            </li>
		          </c:if>
		          
		          <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
		            <li class="page-item ${p == pageMaker.cri.page ? 'active' : ''}">
		              <a class="page-link" 
		                 href="?page=${p}&perPageNum=${pageMaker.cri.perPageNum}&q=${param.q}&from=${param.from}&to=${param.to}">
		                ${p}
		              </a>
		            </li>
		          </c:forEach>
		          
		          <c:if test="${pageMaker.cri.page < pageMaker.endPage}">
		            <li class="page-item">
		              <a class="page-link" 
		                 href="?page=${pageMaker.cri.page + 1}&perPageNum=${pageMaker.cri.perPageNum}&q=${param.q}&from=${param.from}&to=${param.to}">
		                &raquo;
		              </a>
		            </li>
		          </c:if>
		          
		        </ul>
		      </nav>
		    </div>
		  </c:if>
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