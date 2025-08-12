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
        
        <!-- 최고관리자인 경우 -->
        <c:if test="${sessionScope.loginAdmin.roleId == 'SUPER'}">
          
          <!-- 페이지 제목 -->
          <h4 class="mb-4">관리자 계정 관리</h4>
          
          <!-- 검색 영역 -->
          <div class="mb-4">
            <form method="GET" action="${pageContext.request.contextPath}/admin/settings/accounts">
              <!-- 첫 번째 줄: 검색 필터들과 검색/초기화 버튼 -->
              <div class="row mb-3">
                <div class="col-md-2">
                  <select class="form-control" name="condition">
                    <option value="">전체</option>
                    <option value="SUPER" ${cri.condition == 'SUPER' ? 'selected' : ''}>최고관리자</option>
                    <option value="PROD" ${cri.condition == 'PROD' ? 'selected' : ''}>생산관리자</option>
                    <option value="SALES" ${cri.condition == 'SALES' ? 'selected' : ''}>영업관리자</option>
                    <option value="MATERIAL" ${cri.condition == 'MATERIAL' ? 'selected' : ''}>자재관리자</option>
                  </select>
                </div>
                <div class="col-md-2">
                  <select class="form-control" name="status">
                    <option value="">상태</option>
                    <option value="ACTIVE" ${cri.status == 'ACTIVE' ? 'selected' : ''}>활성</option>
                    <option value="INACTIVE" ${cri.status == 'INACTIVE' ? 'selected' : ''}>비활성</option>
                    <option value="LOCKED" ${cri.status == 'LOCKED' ? 'selected' : ''}>잠김</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <input type="text" class="form-control" name="keyword" placeholder="사번, 이름으로 검색" value="${cri.keyword}">
                </div>
                <div class="col-md-4">
                  <button type="submit" class="btn btn-primary me-2">
                    <i class="ti-search"></i> 검색
                  </button>
                  <a href="${pageContext.request.contextPath}/admin/settings/accounts" class="btn btn-outline-secondary">
                    <i class="ti-reload"></i> 초기화
                  </a>
                </div>
              </div>
              
              <!-- 두 번째 줄: 신규 등록 버튼만 오른쪽 끝에 -->
              <div class="row">
                <div class="col-12 d-flex justify-content-end">
                  <button type="button" class="btn btn-success" data-toggle="modal" data-target="#addAdminModal">
                    <i class="ti-plus"></i> 신규 등록
                  </button>
                </div>
              </div>
              
              <input type="hidden" name="sortColumn" id="sortColumn" value="${cri.sortColumn != null ? cri.sortColumn : 'admin_id'}">
              <input type="hidden" name="sortOrder" id="sortOrder" value="${cri.sortOrder != null ? cri.sortOrder : 'ASC'}">
              <input type="hidden" name="page" value="1">
            </form>
          </div>
          
          <!-- 테이블 -->
          <div class="table-responsive">
            <table class="table">
              <thead class="table-header-dark">
                <tr>
                  <th onclick="sortTable('admin_id')" style="cursor: pointer;">
                    사번 ${cri.sortColumn == 'admin_id' ? (cri.sortOrder == 'ASC' ? '↑' : '↓') : ''}
                  </th>
                  <th onclick="sortTable('name')" style="cursor: pointer;">
                    이름 ${cri.sortColumn == 'name' ? (cri.sortOrder == 'ASC' ? '↑' : '↓') : ''}
                  </th>
                  <th>소속/역할</th>
                  <th>연락처</th>
                  <th>상태</th>
                  <th>실패횟수</th>
                  <th onclick="sortTable('created_at')" style="cursor: pointer;">
                    등록일 ${cri.sortColumn == 'created_at' ? (cri.sortOrder == 'ASC' ? '↑' : '↓') : ''}
                  </th>
                  <th>상세</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${not empty adminList}">
                    <c:forEach var="admin" items="${adminList}">
                      <tr class="${(admin.status == 'INACTIVE' || admin.status == 'DELETED' || admin.status == 'LOCKED') ? 'inactive-row' : ''}">
                        <td>${admin.adminId}</td>
                        <td>${admin.name}</td>
                        <td>
                          <c:choose>
                            <c:when test="${admin.roleId == 'SUPER'}">
                              <span class="badge badge-danger">최고관리자</span>
                            </c:when>
                            <c:when test="${admin.roleId == 'PROD'}">
                              <span class="badge badge-success">생산관리</span>
                            </c:when>
                            <c:when test="${admin.roleId == 'SALES'}">
                              <span class="badge badge-info">영업관리</span>
                            </c:when>
                            <c:when test="${admin.roleId == 'MATERIAL'}">
                              <span class="badge badge-warning">자재관리</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge badge-secondary">${admin.roleId}</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>${admin.phone != null ? admin.phone : '-'}</td>
                        <td>
                          <c:choose>
                            <c:when test="${admin.status == 'ACTIVE'}">
                              <span class="badge badge-success">활성</span>
                            </c:when>
                            <c:when test="${admin.status == 'LOCKED'}">
                              <span class="badge badge-danger">잠김</span>
                            </c:when>
                            <c:when test="${admin.status == 'DELETED'}">
                              <span class="badge badge-dark">삭제됨</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge badge-secondary">비활성</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${admin.failCount >= 4}">
                              <span class="badge badge-danger">${admin.failCount}/5</span>
                            </c:when>
                            <c:when test="${admin.failCount >= 2}">
                              <span class="badge badge-warning">${admin.failCount}/5</span>
                            </c:when>
                            <c:when test="${admin.failCount > 0}">
                              <span class="badge badge-info">${admin.failCount}/5</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge badge-success">0/5</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${admin.createdAt != null}">
                              <fmt:formatDate value="${admin.createdAt}" pattern="yyyy-MM-dd"/>
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <button class="btn btn-sm btn-outline-info" onclick="viewAdminDetail('${admin.adminId}')">상세</button>
                        </td>
                        <td>
                          <c:if test="${admin.status != 'DELETED'}">
                            <button class="btn btn-sm btn-outline-warning" onclick="editAdmin('${admin.adminId}')">수정</button>
                          </c:if>
                        </td>
                      </tr>
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <tr>
                      <td colspan="9" class="text-center py-4">등록된 관리자가 없습니다.</td>
                    </tr>
                  </c:otherwise>
                </c:choose>
              </tbody>
            </table>
          </div>
          
          <!-- 페이징 -->
          <c:if test="${not empty adminList}">
            <nav class="mt-4">
              <ul class="pagination justify-content-center">
                <c:if test="${pageMaker.prev}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.startPage - 1}&keyword=${cri.keyword}&condition=${cri.condition}&status=${cri.status}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">이전</a>
                  </li>
                </c:if>
                
                <c:forEach var="pageNum" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                  <li class="page-item ${pageNum == cri.page ? 'active' : ''}">
                    <a class="page-link" href="?page=${pageNum}&keyword=${cri.keyword}&condition=${cri.condition}&status=${cri.status}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">${pageNum}</a>
                  </li>
                </c:forEach>
                
                <c:if test="${pageMaker.next}">
                  <li class="page-item">
                    <a class="page-link" href="?page=${pageMaker.endPage + 1}&keyword=${cri.keyword}&condition=${cri.condition}&status=${cri.status}&sortColumn=${cri.sortColumn}&sortOrder=${cri.sortOrder}">다음</a>
                  </li>
                </c:if>
              </ul>
            </nav>
          </c:if>
          
        </c:if>
        
        <!-- 일반 관리자인 경우 -->
        <c:if test="${sessionScope.loginAdmin.roleId != 'SUPER'}">
          
          <h4 class="mb-4">내 정보 관리</h4>
          
          <div class="card-section">
            <h5 class="section-title">기본 정보</h5>
            <form id="myInfoForm">
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label">사번</label>
                    <input type="text" class="form-control" value="${sessionScope.loginAdmin.adminId}" readonly style="background-color: #f8f9fa;">
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label">소속/역할</label>
                    <div class="form-control" style="background-color: #f8f9fa; border: 1px solid #ced4da;">
                      <c:choose>
                        <c:when test="${sessionScope.loginAdmin.roleId == 'PROD'}">
                          <span class="badge badge-success">생산관리자</span>
                        </c:when>
                        <c:when test="${sessionScope.loginAdmin.roleId == 'SALES'}">
                          <span class="badge badge-info">영업관리자</span>
                        </c:when>
                        <c:when test="${sessionScope.loginAdmin.roleId == 'MATERIAL'}">
                          <span class="badge badge-warning">자재관리자</span>
                        </c:when>
                      </c:choose>
                    </div>
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label required">이름</label>
                    <input type="text" class="form-control" id="myName" value="${sessionScope.loginAdmin.name}" required>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label">연락처</label>
                    <input type="tel" class="form-control" id="myPhone" 
                           value="${sessionScope.loginAdmin.phone != null ? sessionScope.loginAdmin.phone : ''}" 
                           placeholder="010-0000-0000" maxlength="13" oninput="formatPhoneNumber(this)">
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label">새 비밀번호</label>
                    <input type="password" class="form-control" id="myPassword">
                    <small class="form-text text-muted">변경하지 않으려면 비워두세요.</small>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label">비밀번호 확인</label>
                    <input type="password" class="form-control" id="myPasswordConfirm">
                  </div>
                </div>
              </div>
              <div class="mt-3">
                <button type="button" class="btn custom-navy" onclick="updateMyInfo()">정보 수정</button>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-secondary ml-2">목록</a>
              </div>
            </form>
          </div>
          
        </c:if>
        
      </div>
      <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
    </div>
  </div>   
</div>

<!-- 관리자 추가 모달 -->
<c:if test="${sessionScope.loginAdmin.roleId == 'SUPER'}">
  <div class="modal fade" id="addAdminModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header bg-primary">
          <h5 class="modal-title text-white">관리자 추가</h5>
          <button type="button" class="close text-white" data-dismiss="modal">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form id="addAdminForm">
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">관리자 ID (사번)</label>
                  <div class="input-group">
                    <div class="input-group-prepend">
                      <select class="form-control" id="adminIdPrefix" style="max-width: 120px;">
                        <option value="A">A (최고관리자)</option>
                        <option value="P">P (생산)</option>
                        <option value="S">S (영업)</option>
                        <option value="M">M (자재)</option>
                      </select>
                    </div>
                    <input type="text" class="form-control" id="adminIdNumber" placeholder="0001" maxlength="4">
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">이름</label>
                  <input type="text" class="form-control" id="adminName" required>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">비밀번호</label>
                  <input type="password" class="form-control" id="adminPassword" required>
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">비밀번호 확인</label>
                  <input type="password" class="form-control" id="adminPasswordConfirm" required>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label">전화번호</label>
                  <input type="tel" class="form-control" id="adminPhone" placeholder="010-0000-0000" maxlength="13" oninput="formatPhoneNumber(this)">
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">역할</label>
                  <select class="form-control" id="adminRole" required>
                    <option value="">선택해주세요</option>
                    <option value="SUPER">최고관리자</option>
                    <option value="PROD">생산관리자</option>
                    <option value="SALES">영업관리자</option>
                    <option value="MATERIAL">자재관리자</option>
                  </select>
                </div>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
          <button type="button" class="btn custom-navy" onclick="addAdmin()">등록</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 관리자 수정 모달 -->
  <div class="modal fade" id="editAdminModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header bg-primary">
          <h5 class="modal-title text-white">관리자 수정</h5>
          <button type="button" class="close text-white" data-dismiss="modal">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form id="editAdminForm">
            <input type="hidden" id="editAdminId">
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label">관리자 ID (사번)</label>
                  <input type="text" class="form-control" id="editAdminIdDisplay" readonly style="background-color: #f8f9fa;">
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">이름</label>
                  <input type="text" class="form-control" id="editAdminName" required>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">
                <div class="form-group">
                  <label class="form-label">새 비밀번호</label>
                  <input type="password" class="form-control" id="editAdminPassword">
                  <small class="form-text text-muted">변경하지 않으려면 비워두세요.</small>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label">전화번호</label>
                  <input type="tel" class="form-control" id="editAdminPhone" placeholder="010-0000-0000" maxlength="13" oninput="formatPhoneNumber(this)">
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">역할</label>
                  <select class="form-control" id="editAdminRole" required>
                    <option value="SUPER">최고관리자</option>
                    <option value="PROD">생산관리자</option>
                    <option value="SALES">영업관리자</option>
                    <option value="MATERIAL">자재관리자</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label class="form-label required">상태</label>
                  <select class="form-control" id="editAdminStatus" required>
                    <option value="ACTIVE">활성</option>
                    <option value="INACTIVE">비활성</option>
                  </select>
                </div>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
          <button type="button" class="btn custom-navy" onclick="updateAdmin()">수정</button>
          <button type="button" class="btn btn-danger" onclick="deleteAdminFromModal()">삭제</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 관리자 상세 모달 -->
  <div class="modal fade" id="adminDetailModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header bg-primary">
          <h5 class="modal-title text-white">관리자 상세정보</h5>
          <button type="button" class="close text-white" data-dismiss="modal">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="table-responsive">
            <table class="table table-bordered">
              <tbody id="adminDetailBody"></tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">닫기</button>
          <button type="button" class="btn custom-navy" onclick="editAdminFromDetail()">수정</button>
        </div>
      </div>
    </div>
  </div>
</c:if>

<script>
  if (!document.querySelector('meta[name="contextPath"]')) {
    const meta = document.createElement('meta');
    meta.name = 'contextPath';
    meta.content = '${pageContext.request.contextPath}';
    document.head.appendChild(meta);
  }
  
  window.isSuperAdmin = ${sessionScope.loginAdmin.roleId == 'SUPER'};
  window.currentAdminId = '${sessionScope.loginAdmin.adminId}';
  
  function formatPhoneNumber(input) {
    let value = input.value.replace(/[^0-9]/g, '');
    if (value.length >= 3 && value.length <= 7) {
      value = value.replace(/(\d{3})(\d+)/, '$1-$2');
    } else if (value.length >= 8) {
      value = value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
    }
    input.value = value;
  }
  
  function sortTable(column) {
    const currentSortColumn = document.getElementById('sortColumn').value;
    const currentSortOrder = document.getElementById('sortOrder').value;
    
    let newSortOrder = 'ASC';
    if (currentSortColumn === column && currentSortOrder === 'ASC') {
      newSortOrder = 'DESC';
    }
    
    document.getElementById('sortColumn').value = column;
    document.getElementById('sortOrder').value = newSortOrder;
    document.querySelector('form').submit();
  }
  
  // 페이지 로드 후 비활성 행들을 맨 아래로 이동
  document.addEventListener('DOMContentLoaded', function() {
    const tbody = document.querySelector('tbody');
    const inactiveRows = document.querySelectorAll('.inactive-row');
    
    // 비활성 행들을 맨 아래로 이동
    inactiveRows.forEach(function(row) {
      tbody.appendChild(row);
    });
  });
</script>

<script src="${pageContext.request.contextPath}/resources/js/accounts.js"></script>