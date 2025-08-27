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
            
            <!-- 페이지 헤더 -->
            <div class="col-12">
              <div class="page-header">
                <h3 class="page-title">생산라인 관리</h3>
              </div>
            </div>

           <!-- 상태 요약 카드 -->
<div class="col-12 mb-3">
  <div class="row">
    <div class="col-md-4 mb-3 mb-md-0">
      <div class="card text-white status-card status-info">
        <div class="card-body status-row py-4">
          <div class="stat-label">전체 라인</div>
          <div class="stat-value">${totalCount}</div>
        </div>
      </div>
    </div>

    <div class="col-md-4 mb-3 mb-md-0">
      <div class="card text-white status-card status-success">
        <div class="card-body status-row py-4">
          <div class="stat-label">활성 라인</div>
          <div class="stat-value">${activeCount}</div>
        </div>
      </div>
    </div>

    <div class="col-md-4">
      <div class="card text-white status-card status-secondary">
        <div class="card-body status-row py-4">
          <div class="stat-label">비활성 라인</div>
          <div class="stat-value">${inactiveCount}</div>
        </div>
      </div>
    </div>
  </div>
</div>

            <!-- 라인 목록 테이블 -->
<div class="col-12">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">생산라인 목록</h4>
      
      <div class="table-responsive">
        <table class="table table-bordered">
          <thead class="table-header-dark">
            <tr>
              <th>라인ID</th>
              <th>라인명</th>
              <th>상태</th>
              <th>현재 작업</th>
              <th>상세</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="line" items="${lineList}">
              <tr <c:if test="${line.status eq 'INACTIVE'}">class="inactive-row"</c:if>>
                <td>${line.lineId}</td>
                <td>${line.lineName}</td>
                <td>
                  <c:choose>
                    <c:when test="${line.status eq 'ACTIVE'}">
                      <span class="badge badge-success">활성</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge badge-secondary">비활성</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${line.currentWorkOrder != null}">
                      <span class="text-warning font-weight-bold">
                        생산중
                      </span>
                    </c:when>
                    <c:when test="${line.status eq 'ACTIVE'}">
                      <span class="text-muted">대기중</span>
                    </c:when>
                    <c:otherwise>
                      <span class="text-muted">-</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <button type="button" 
                          class="btn btn-sm btn-outline-info"
                          onclick="showLineDetail('${line.lineId}')">
                    상세
                  </button>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty lineList}">
              <tr>
                <td colspan="5" class="text-center text-muted">
                  등록된 생산라인이 없습니다.
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
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

<!-- 라인 상세 모달 -->
<div class="modal fade" id="lineDetailModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header" style="background-color: #1c355e;">
        <h5 class="modal-title text-white">생산라인 상세</h5>
        <button type="button" class="close text-white" data-dismiss="modal">
          <span>&times;</span>
        </button>
      </div>
      <div class="modal-body" id="lineDetailContent">
        <!-- 상세 내용이 여기에 로드됩니다 -->
      </div>
    </div>
  </div>
</div>

<style>
/* 카드 공통 */
.status-card {
  border: 0;
  border-radius: 16px;
  box-shadow: 0 4px 14px rgba(0,0,0,.08);
}

/* ←라벨 / →숫자 배치 */
.status-row{
  display:flex;
  align-items:center;
  justify-content:space-between;
  min-height:96px;
}

/* 라벨 왼쪽 */
.stat-label{
  font-size:.95rem;
  font-weight:600;
  opacity:.95;
  margin:0;
}

/* 숫자 오른쪽 크게 */
.stat-value{
  line-height:1;
  font-weight:800;
  font-size:clamp(2.2rem, 3.6vw, 3.2rem);
  letter-spacing:-0.5px;
  text-align:right;
  min-width: 3ch; /* 숫자 폭 확보(깜빡임 방지) */
}

/* 배경색 */
.status-info{background:#1f6fb2;}
.status-success{background:#2e8b57;}
.status-secondary{background:#7a7f87;}

/* 테이블 헤더 남색 유지 */
.table-header-dark th{
  background:#1c355e;
  color:#fff;
  border-color:#1c355e;
}
</style>

<script>
(function(){
  var ctx = window.CONTEXT_PATH || '${pageContext.request.contextPath}';

  // 상세 모달 로더
  window.showLineDetail = function(lineId) {
    $.ajax({
      url: ctx + '/line/detail',
      method: 'GET',
      data: { lineId: lineId },
      success: function(response) {
        $('#lineDetailContent').html(response);
        $('#lineDetailModal').modal('show');
        // 방금 주입된 partial 안의 툴팁 다시 초기화
        $('#lineDetailModal [data-toggle="tooltip"]').tooltip();
      },
      error: function() {
        alert('상세 정보를 불러오는데 실패했습니다.');
      }
    });
  };

  // 위임 바인딩: 모달 내부 버튼(.js-toggle-line) 클릭 처리
  $(document).on('click', '.js-toggle-line', function(e){
    var btn = this;
    if (btn.disabled) return;

    var lineId = btn.dataset.lineId;
    var next   = btn.dataset.next; // ACTIVE or INACTIVE
    var msg    = '라인을 ' + (next === 'ACTIVE' ? '활성화' : '비활성화') + '하시겠습니까?';
    if(!confirm(msg)) return;

    btn.disabled = true; // 중복 클릭 방지
    $.post(ctx + '/line/updateStatus', { lineId: lineId, status: next })
      .done(function(res){
        if(res && res.success){
          alert('상태가 변경되었습니다.');
          location.reload();
        }else{
          alert('상태 변경 실패: ' + (res && res.message ? res.message : '알 수 없음'));
        }
      })
      .fail(function(){
        alert('시스템 오류가 발생했습니다.');
      })
      .always(function(){
        btn.disabled = false;
      });
  });
})();
</script>
