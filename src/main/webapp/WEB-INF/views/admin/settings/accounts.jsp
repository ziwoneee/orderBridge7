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
          
          <!-- 최고관리자인 경우 - 전체 관리자 계정 관리 -->
          <c:if test="${sessionScope.loginAdmin.roleId == 'SUPER'}">
            
            <!-- 페이지 제목 -->
            <div class="col-md-12 grid-margin">
              <div class="row">
                <div class="col-12 col-xl-8 mb-4 mb-xl-0">
                  <h3 class="font-weight-bold">관리자 계정 관리</h3>
                  <h6 class="font-weight-normal mb-0">시스템 관리자 계정을 등록, 수정, 삭제할 수 있습니다.</h6>
                </div>
              </div>
            </div>
            
            <!-- 검색 및 필터 카드 -->
            <div class="col-md-12 grid-margin stretch-card">
              <div class="card">
                <div class="card-body">
                  <div class="row">
                    <div class="col-md-3">
                      <label class="form-label">검색</label>
                      <input type="text" class="form-control" id="searchInput" placeholder="사번, 이름으로 검색">
                    </div>
                    <div class="col-md-3">
                      <label class="form-label">소속/역할</label>
                      <select class="form-control" id="roleFilter">
                        <option value="">전체</option>
                        <option value="SUPER">최고관리자</option>
                        <option value="PROD">생산관리자</option>
                        <option value="SALES">영업관리자</option>
                        <option value="MATERIAL">자재관리자</option>
                      </select>
                    </div>
                    <div class="col-md-3">
                      <label class="form-label">상태</label>
                      <select class="form-control" id="statusFilter">
                        <option value="">전체</option>
                        <option value="ACTIVE">활성</option>
                        <option value="INACTIVE">비활성</option>
                      </select>
                    </div>
                    <div class="col-md-3 d-flex align-items-end">
                      <div class="form-group">
                        <button type="button" class="btn btn-primary me-2" onclick="searchAdmins()" style="background-color: #1C355E; border-color: #1C355E;">
                          <i class="ti-search"></i> 검색
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/settings/accounts" class="btn btn-light">
                          <i class="ti-reload"></i> 초기화
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 신규등록 버튼 -->
            <div class="col-md-12 mb-3">
              <button type="button" class="btn btn-success mb-2" data-toggle="modal" data-target="#addAdminModal">
                <i class="ti-plus"></i> 신규등록
              </button>
            </div>
            
            <!-- 관리자 목록 테이블 -->
            <div class="col-md-12 grid-margin stretch-card">
              <div class="card">
                <div class="card-body">
                  <h4 class="card-title">관리자 목록</h4>
                  <div class="table-responsive">
                    <table class="table table-striped">
                      <thead class="table-header-dark">
                        <tr>
                          <th>사번</th>
                          <th>이름</th>
                          <th>소속/역할</th>
                          <th>연락처</th>
                          <th>상태</th>
                          <th>입사일</th>
                          <th>최종수정일</th>
                          <th>상세</th>
                          <th>관리</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach var="admin" items="${adminList}">
                          <tr class="${admin.status == 'INACTIVE' ? 'inactive-row' : ''}">
                            <td>
                              <span class="badge badge-dark">${admin.adminId}</span>
                            </td>
                            <td><strong>${admin.name}</strong></td>
                            <td>
                              <div>
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
                              </div>
                            </td>
                            <td>${admin.phone}</td>
                            <td>
                              <c:choose>
                                <c:when test="${admin.status == 'ACTIVE'}">
                                  <span class="badge badge-success">활성</span>
                                </c:when>
                                <c:otherwise>
                                  <span class="badge badge-secondary">비활성</span>
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
                              <c:choose>
                                <c:when test="${admin.updatedAt != null}">
                                  <fmt:formatDate value="${admin.updatedAt}" pattern="yyyy-MM-dd"/>
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                              </c:choose>
                            </td>
                            <td>
                              <button class="btn btn-sm btn-outline-info" onclick="viewAdminDetail('${admin.adminId}')">
                                상세
                              </button>
                            </td>
                            <td>
                              <button class="btn btn-sm btn-outline-warning" onclick="editAdmin('${admin.adminId}')">
                                수정
                              </button>
                            </td>
                          </tr>
                        </c:forEach>
                        <c:if test="${empty adminList}">
                          <tr>
                            <td colspan="9" class="text-center py-4">등록된 관리자가 없습니다.</td>
                          </tr>
                        </c:if>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
            
          </c:if>
          
          <!-- 일반 관리자인 경우 - 본인 정보 수정만 -->
          <c:if test="${sessionScope.loginAdmin.roleId != 'SUPER'}">
            
            <!-- 페이지 제목 -->
            <div class="col-md-12 grid-margin">
              <div class="row">
                <div class="col-12 col-xl-8 mb-4 mb-xl-0">
                  <h3 class="font-weight-bold">내 정보 관리</h3>
                  <h6 class="font-weight-normal mb-0">본인의 계정 정보를 확인하고 수정할 수 있습니다.</h6>
                </div>
              </div>
            </div>
            
            <!-- 내 정보 카드 -->
            <div class="col-md-12 grid-margin stretch-card">
              <div class="card">
                <div class="card-body">
                  <h4 class="card-title">기본 정보</h4>
                  <form id="myInfoForm">
                    <div class="card-section">
                      <div class="row">
                        <div class="col-md-6">
                          <div class="form-group">
                            <label class="form-label">사번</label>
                            <input type="text" class="form-control" value="${sessionScope.loginAdmin.adminId}" readonly style="background-color: #f8f9fa;">
                            <small class="form-text text-muted">사번은 변경할 수 없습니다.</small>
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
                            <input type="tel" class="form-control" id="myPhone" value="${sessionScope.loginAdmin.phone}" placeholder="010-0000-0000">
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
                      <div class="row">
                        <div class="col-md-12">
                          <button type="button" class="btn btn-primary" onclick="updateMyInfo()">
                            <i class="ti-check"></i> 정보 수정
                          </button>
                        </div>
                      </div>
                    </div>
                  </form>
                </div>
              </div>
            </div>
            
          </c:if>
          
        </div>
        
        <!-- 페이징 처리 시작 -->
        <c:if test="${sessionScope.loginAdmin.roleId == 'SUPER' && not empty adminList}">
          <div class="row">
            <div class="col-12">
              <nav aria-label="Page navigation">
                <ul class="pagination justify-content-center">
                  <!-- 페이징 로직은 컨트롤러에서 처리 -->
                </ul>
              </nav>
            </div>
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

<!-- 최고관리자 전용 모달들 -->
<c:if test="${sessionScope.loginAdmin.roleId == 'SUPER'}">

  <!-- 관리자 추가 모달 -->
  <div class="modal fade" id="addAdminModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header" style="background-color: #1c355e;">
          <h5 class="modal-title text-white">
            <i class="ti-user mr-2"></i>관리자 추가
          </h5>
          <button type="button" class="close text-white" data-dismiss="modal">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form id="addAdminForm">
            <div class="card-section">
              <h5 class="section-title">기본 정보</h5>
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label class="form-label required">관리자 ID (사번)</label>
                    <div class="input-group">
                      <div class="input-group-prepend">
                        <select class="form-control" id="adminIdPrefix" style="width: 120px;">
                          <option value="A">A (최고관리자)</option>
                          <option value="P">P (생산)</option>
                          <option value="S">S (영업)</option>
                          <option value="M">M (자재)</option>
                        </select>
                      </div>
                      <input type="text" class="form-control" id="adminIdNumber" placeholder="0001" maxlength="4" pattern="[0-9]{4}">
                    </div>
                    <small class="form-text text-muted">
                      형식: A0001, P0001, S0001, M0001
                    </small>
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
                    <input type="tel" class="form-control" id="adminPhone" placeholder="010-0000-0000">
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
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" onclick="addAdmin()">등록</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 관리자 수정 모달 -->
  <div class="modal fade" id="editAdminModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header" style="background-color: #1c355e;">
          <h5 class="modal-title text-white">
            <i class="ti-pencil mr-2"></i>관리자 수정
          </h5>
          <button type="button" class="close text-white" data-dismiss="modal">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form id="editAdminForm">
            <input type="hidden" id="editAdminId">
            <div class="card-section">
              <h5 class="section-title">기본 정보</h5>
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
                    <input type="tel" class="form-control" id="editAdminPhone" placeholder="010-0000-0000">
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
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" onclick="updateAdmin()">수정</button>
          <button type="button" class="btn btn-danger btn-block" onclick="deleteAdminFromModal()">삭제</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 관리자 상세 모달 -->
  <div class="modal fade" id="adminDetailModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header" style="background-color: #1c355e;">
          <h5 class="modal-title text-white">
            <i class="ti-eye mr-2"></i>관리자 상세정보
          </h5>
          <button type="button" class="close text-white" data-dismiss="modal">
            <span>&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="table-responsive">
            <table class="table table-bordered">
              <thead>
                <tr>
                  <th class="bg-light">항목</th>
                  <th class="bg-light">내용</th>
                </tr>
              </thead>
              <tbody id="adminDetailBody">
                <!-- 상세 정보가 여기에 들어감 -->
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">목록</button>
          <button type="button" class="btn btn-info" onclick="editAdminFromDetail()">수정</button>
        </div>
      </div>
    </div>
  </div>

</c:if>

<!-- JavaScript 변수 설정 -->
<script>
  // JavaScript에서 사용할 변수들 설정
  window.isSuperAdmin = ${sessionScope.loginAdmin.roleId == 'SUPER'};
  window.currentAdminId = '${sessionScope.loginAdmin.adminId}';
</script>

<!-- JavaScript 파일 포함 -->
<script src="${pageContext.request.contextPath}/resources/js/accounts.js"></script>