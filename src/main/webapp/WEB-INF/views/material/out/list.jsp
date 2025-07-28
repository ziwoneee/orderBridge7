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
          
          	<!-- 제목 -->
			<div class="col-12 mb-4">
			  <h3 class="font-weight-bold">자재 출고 관리</h3>
			</div>
			
			<!-- 검색 필터 -->
			<form method="get" class="row align-items-end mb-4">

			  <!-- 1행 -->
			  <div class="col-md-3">
			    <label for="status" class="form-label">출고상태</label>
			    <select name="status" id="status" class="form-control">
			      <option value="" ${empty param.status ? 'selected' : ''}>전체</option>
			      <option value="미출고" ${param.status eq '미출고' ? 'selected' : ''}>미출고</option>
			      <option value="출고완료" ${param.status eq '출고완료' ? 'selected' : ''}>출고완료</option>
			    </select>
			  </div>
			
			  <div class="col-md-5">
			    <label for="keyword" class="form-label">출고관리번호</label>
			    <input type="text" id="keyword" name="keyword" value="${param.keyword}"
			           class="form-control"
			           placeholder="예: OUT-RM-20250725-001" />
			  </div>
			
			  <!-- 2행 -->
			  <div class="col-md-3 mt-3">
			    <label for="startDate" class="form-label">출고일자 (시작)</label>
			    <input type="date" id="startDate" name="startDate" value="${param.startDate}" class="form-control" />
			  </div>
			
			  <div class="col-md-3 mt-3">
			    <label for="endDate" class="form-label">출고일자 (종료)</label>
			    <input type="date" id="endDate" name="endDate" value="${param.endDate}" class="form-control" />
			  </div>
			
			  <div class="col-md-2 mt-3">
			    <label class="form-label invisible">조회</label>
			    <button type="submit" class="btn btn-primary w-100">조회</button>
			  </div>
			
			</form>










		
		    <!-- 출고 리스트 테이블 -->
		    <div id="table_content" class="table-responsive">
		      <table class="table table-bordered text-center">
		        <thead>
		          <tr>
		            <th>출고관리번호</th>
		            <th>출고일자</th>
		            <th>출고상태</th>
		            <th>품명</th>
		            <th>출고수량</th>
		            <th>재고상태</th>
		            <th>작업지시번호</th>
		            <th>납기일자</th>
		            <th>담당자</th>
		            <th>상세</th>
		            <th>출고처리</th>
		          </tr>
		        </thead>
		        <tbody>
		         <c:forEach var="item" items="${outList}">
				  <tr>
				    <td>${item.outboundId}</td>
				    <td>
				      <c:if test="${item.outboundDate != null}">
				        <fmt:formatDate value="${item.outboundDate}" pattern="yyyy-MM-dd" />
				      </c:if>
				    </td>
				    <td>
				      <c:choose>
				        <c:when test="${item.status eq '출고완료'}">
				          <span class="badge badge-success">출고완료</span>
				        </c:when>
				        <c:otherwise>
				          <span class="badge badge-danger">미출고</span>
				        </c:otherwise>
				      </c:choose>
				    </td>
				    <td>${item.materialName}</td>
				    <td>${item.requiredQty}</td>
				    <td>${item.stockStatus} (재고 ${item.stockQty})</td>
				    <td>${item.workOrderNo}</td>
				    <td><fmt:formatDate value="${item.dueDate}" pattern="yyyy-MM-dd" /></td>
				    <td>${item.handledBy}</td>
				    <td><button class="btn btn-sm btn-outline-secondary" onclick="loadOutboundDetail('${item.outboundId}')">상세</button></td>
				    <td>
				      <c:if test="${item.status ne '완료'}">
				        <button class="btn btn-sm btn-outline-primary">출고처리</button>
				      </c:if>
				    </td>
				  </tr>
				</c:forEach>

		        </tbody>
		      </table>
		    </div>
		    
		    <!-- 출고관리 상세 모달 -->
			<div class="modal fade" id="outboundDetailModal" tabindex="-1" role="dialog" aria-labelledby="modalTitle" aria-hidden="true">
			  <div class="modal-dialog modal-lg" role="document">
			    <div class="modal-content">
			
			      <div class="modal-header">
			        <h5 class="modal-title">출고관리 상세</h5>
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
			          <span aria-hidden="true">&times;</span>
			        </button>
			      </div>
			
			      <div class="modal-body">
			        <!-- 기본 정보 -->
			        <table class="table table-bordered">
			          <tbody>
			            <tr>
			              <th>작업지시번호</th>
			              <td id="workOrderNo"></td>
			              <th>납기일</th>
			              <td id="dueDate"></td>
			            </tr>
			            <tr>
			              <th>작업지시일자</th>
			              <td id="workOrderDate"></td>
			            </tr>
			            <tr>
			              <th>출고관리번호</th>
			              <td id="outboundId"></td>
			              <th>출고진행현황</th>
			              <td id="status"></td>
			            </tr>
			            <tr>
			              <th>출고일자</th>
			              <td id="outboundDate"></td>
			              <th>출고담당자</th>
			              <td id="handledBy"></td>
			            </tr>
			            <tr>
			              <th>품명</th>
			              <td colspan="3" id="materialName"></td>
			            </tr>
			          </tbody>
			        </table>
			
			        <!-- 자재 재고 정보 -->
			        <h6 class="mt-4">자재 재고 정보</h6>
			        <table class="table table-bordered text-center">
			          <thead>
			            <tr>
			              <th>품목코드</th>
			              <th>품명</th>
			              <th>필요수량</th>
			              <th>재고수량</th>
			              <th>재고상태</th>
			            </tr>
			          </thead>
			          <tbody id="stockInfo">
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
          
			<!-- 페이징 처리 시작 -->
			<div class="d-flex justify-content-center mt-4">
			 <nav>
			  <ul class="pagination justify-content-center mt-4">
			
			    <!-- 이전 버튼 -->
			    <c:if test="${pageMaker.cri.page>1}">
			      <li class="page-item">
			        <a class="page-link"href="?page=${pageMaker.startPage - 1}&perPageNum=${cri.perPageNum}&condition=${cri.condition}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&laquo;</a>
			      </li>
			    </c:if>
			    
			    <!-- 페이지 번호 출력 -->
			    <c:forEach var="p" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
			      <li class="page-item ${p == cri.page ? 'active' : ''}">
			        <a class="page-link"href="?page=${p}&perPageNum=${cri.perPageNum}&condition=${cri.condition}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${p}</a>
			      </li>
			    </c:forEach>
			    
			    <!-- 다음 버튼 -->
			    <c:if test="${pageMaker.cri.page<pageMaker.endPage}">
			      <li class="page-item">
			        <a class="page-link"href="?page=${pageMaker.endPage + 1}&perPageNum=${cri.perPageNum}&condition=${cri.condition}&keyword=${cri.keyword}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">&raquo;</a>
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



<!-- 출고 처리 JS 함수 -->
<script>
function processOut(outboundId) {
  if (confirm("출고처리 하시겠습니까?")) {
    location.href = '/material/out/process?outboundId=' + outboundId;
  }
}

function viewDetail(outboundId) {
  // 상세 모달 띄우기 Ajax 또는 location.href 사용
  location.href = '/material/out/detail?outboundId=' + outboundId;
}
</script>

<!-- 자재 상세 모달 -->
<script>
function openOutboundModal(data) {
  // 기본 정보 채우기
  $('#workOrderNo').text(data.workOrderNo);
  $('#dueDate').text(formatDate(data.dueDate));
  $('#workOrderDate').text(formatDate(data.workOrderDate));
  $('#outboundId').text(data.outboundId);
  $('#status').text(data.status);
  $('#outboundDate').text(formatDate(data.outboundDate));
  $('#handledBy').text(data.handledBy);
  $('#materialName').text(data.materialName);

  // 자재 재고 정보 테이블 초기화
  $('#stockInfo').empty();

  data.materialList.forEach(function(item) {
    const stockStatus = item.stockQty >= item.requiredQty
      ? '<span class="badge badge-success">정상</span>'
      : '<span class="badge badge-danger">부족</span>';

    $('#stockInfo').append(`
      <tr>
        <td>${item.materialId}</td>
        <td>${item.materialName}</td>
        <td>${item.requiredQty}</td>
        <td>${item.stockQty}</td>
        <td>${stockStatus}</td>
      </tr>
    `);
  });

  // 모달 열기
  $('#outboundDetailModal').modal('show');
}

function loadOutboundDetail(outboundId) {
	  $.ajax({
	    url: '/material/outbound/detail',
	    method: 'GET',
	    data: { outboundId: outboundId },
	    success: function(response) {
	      console.log("Ajax 응답 데이터:", response);	
	      openOutboundModal(response); // ✅ 기존에 정의한 함수
	    },
	    error: function() {
	      alert('상세 정보를 불러오는 데 실패했습니다.');
	    }
	  });
	}
</script>

<script>
// 날짜를 yyyy-MM-dd 형식으로 변환하는 함수
function formatDate(timestamp) {
  if (!timestamp) return '--';

  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return '--'; 

  const yyyy = date.getFullYear();
  const mm = ('0' + (date.getMonth() + 1)).slice(-2);
  const dd = ('0' + date.getDate()).slice(-2);
  return `${yyyy}-${mm}-${dd}`;
}
</script>
