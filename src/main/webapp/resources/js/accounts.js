// accounts.js - 관리자 계정 관리 JavaScript

// 전역 변수
const contextPath = document.querySelector('meta[name="contextPath"]') ? 
  document.querySelector('meta[name="contextPath"]').getAttribute('content') : '';

// 최고관리자 권한 체크 (JSP에서 설정)
const isSuperAdmin = typeof window.isSuperAdmin !== 'undefined' ? window.isSuperAdmin : false;

// 검색 기능
function searchAdmins() {
  const searchValue = document.getElementById('searchInput').value;
  const roleFilter = document.getElementById('roleFilter').value;
  const statusFilter = document.getElementById('statusFilter').value;
  
  const form = document.createElement('form');
  form.method = 'GET';
  form.action = contextPath + '/admin/settings/accounts';
  
  if(searchValue) {
    const searchInput = document.createElement('input');
    searchInput.type = 'hidden';
    searchInput.name = 'search';
    searchInput.value = searchValue;
    form.appendChild(searchInput);
  }
  
  if(roleFilter) {
    const roleInput = document.createElement('input');
    roleInput.type = 'hidden';
    roleInput.name = 'role';
    roleInput.value = roleFilter;
    form.appendChild(roleInput);
  }
  
  if(statusFilter) {
    const statusInput = document.createElement('input');
    statusInput.type = 'hidden';
    statusInput.name = 'status';
    statusInput.value = statusFilter;
    form.appendChild(statusInput);
  }
  
  document.body.appendChild(form);
  form.submit();
}

// 관리자 추가
function addAdmin() {
  const form = document.getElementById('addAdminForm');
  const password = document.getElementById('adminPassword').value;
  const passwordConfirm = document.getElementById('adminPasswordConfirm').value;
  const prefix = document.getElementById('adminIdPrefix').value;
  const number = document.getElementById('adminIdNumber').value;
  
  if (password !== passwordConfirm) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }
  
  if (!number || number.length !== 4 || !/^\d{4}$/.test(number)) {
    alert('사번은 4자리 숫자로 입력해주세요. (예: 0001)');
    return;
  }
  
  const adminId = prefix + number;
  
  const adminData = {
    adminId: adminId,
    name: document.getElementById('adminName').value,
    password: password,
    phone: document.getElementById('adminPhone').value,
    roleId: document.getElementById('adminRole').value
  };
  
  $.ajax({
    url: contextPath + '/admin/settings/accounts',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(adminData),
    success: function(response) {
      if(response.success) {
        alert('관리자가 성공적으로 등록되었습니다.\n사번: ' + adminId);
        $('#addAdminModal').modal('hide');
        form.reset();
        location.reload();
      } else {
        alert('등록 실패: ' + response.message);
      }
    },
    error: function() {
      alert('등록 중 오류가 발생했습니다.');
    }
  });
}

// 관리자 수정
function editAdmin(adminId) {
  $.ajax({
    url: contextPath + '/admin/settings/accounts/' + adminId,
    type: 'GET',
    success: function(admin) {
      document.getElementById('editAdminId').value = admin.adminId;
      document.getElementById('editAdminIdDisplay').value = admin.adminId;
      document.getElementById('editAdminName').value = admin.name;
      document.getElementById('editAdminPhone').value = admin.phone || '';
      document.getElementById('editAdminRole').value = admin.roleId;
      document.getElementById('editAdminStatus').value = admin.status;
      
      $('#editAdminModal').modal('show');
    },
    error: function() {
      alert('관리자 정보를 가져오는데 실패했습니다.');
    }
  });
}

// 관리자 정보 업데이트
function updateAdmin() {
  const adminData = {
    adminId: document.getElementById('editAdminId').value,
    name: document.getElementById('editAdminName').value,
    password: document.getElementById('editAdminPassword').value,
    phone: document.getElementById('editAdminPhone').value,
    roleId: document.getElementById('editAdminRole').value,
    status: document.getElementById('editAdminStatus').value
  };
  
  $.ajax({
    url: contextPath + '/admin/settings/accounts/' + adminData.adminId,
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify(adminData),
    success: function(response) {
      if(response.success) {
        alert('관리자 정보가 성공적으로 수정되었습니다.');
        $('#editAdminModal').modal('hide');
        location.reload();
      } else {
        alert('수정 실패: ' + response.message);
      }
    },
    error: function() {
      alert('수정 중 오류가 발생했습니다.');
    }
  });
}

// 관리자 상세보기
function viewAdminDetail(adminId) {
  $.ajax({
    url: contextPath + '/admin/settings/accounts/' + adminId,
    type: 'GET',
    success: function(admin) {
      let detailHtml = '';
      detailHtml += '<tr><td><strong>사번</strong></td><td><span class="badge badge-dark">' + admin.adminId + '</span></td></tr>';
      detailHtml += '<tr><td><strong>이름</strong></td><td>' + admin.name + '</td></tr>';
      detailHtml += '<tr><td><strong>소속/역할</strong></td><td>' + getRoleNameDetail(admin.roleId) + '</td></tr>';
      detailHtml += '<tr><td><strong>연락처</strong></td><td>' + (admin.phone || '-') + '</td></tr>';
      detailHtml += '<tr><td><strong>상태</strong></td><td>' + getStatusBadge(admin.status) + '</td></tr>';
      detailHtml += '<tr><td><strong>입사일</strong></td><td>' + formatDate(admin.createdAt) + '</td></tr>';
      detailHtml += '<tr><td><strong>최종수정일</strong></td><td>' + formatDate(admin.updatedAt) + '</td></tr>';
      
      $('#adminDetailBody').html(detailHtml);
      $('#adminDetailModal').data('admin-id', adminId);
      $('#adminDetailModal').modal('show');
    },
    error: function() {
      alert('관리자 정보를 가져오는데 실패했습니다.');
    }
  });
}

// 상세 모달에서 수정 모달로 이동
function editAdminFromDetail() {
  const adminId = $('#adminDetailModal').data('admin-id');
  $('#adminDetailModal').modal('hide');
  setTimeout(function() {
    editAdmin(adminId);
  }, 300);
}

// 수정 모달에서 삭제
function deleteAdminFromModal() {
  const adminId = document.getElementById('editAdminId').value;
  if (confirm('정말로 이 관리자를 삭제하시겠습니까?')) {
    $.ajax({
      url: contextPath + '/admin/settings/accounts/' + adminId,
      type: 'DELETE',
      success: function(response) {
        if(response.success) {
          alert('관리자가 성공적으로 삭제되었습니다.');
          $('#editAdminModal').modal('hide');
          location.reload();
        } else {
          alert('삭제 실패: ' + response.message);
        }
      },
      error: function() {
        alert('삭제 중 오류가 발생했습니다.');
      }
    });
  }
}

// 헬퍼 함수들
function getRoleNameDetail(roleId) {
  const roleMap = {
    'SUPER': '<span class="badge badge-danger">최고관리자</span>',
    'PROD': '<span class="badge badge-success">생산관리자</span>',
    'SALES': '<span class="badge badge-info">영업관리자</span>',
    'MATERIAL': '<span class="badge badge-warning">자재관리자</span>'
  };
  return roleMap[roleId] || roleId;
}

function getStatusBadge(status) {
  if (status === 'ACTIVE') {
    return '<span class="badge badge-success">활성</span>';
  } else {
    return '<span class="badge badge-secondary">비활성</span>';
  }
}

function formatDate(dateString) {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('ko-KR');
}

// 일반 관리자 - 내 정보 수정
function updateMyInfo() {
  const password = document.getElementById('myPassword').value;
  const passwordConfirm = document.getElementById('myPasswordConfirm').value;
  
  if (password && password !== passwordConfirm) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }
  
  const myData = {
    adminId: window.currentAdminId,
    name: document.getElementById('myName').value,
    phone: document.getElementById('myPhone').value
  };
  
  if (password) {
    myData.password = password;
  }
  
  $.ajax({
    url: contextPath + '/admin/settings/accounts/my-info',
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify(myData),
    success: function(response) {
      if(response.success) {
        alert('정보가 성공적으로 수정되었습니다.');
        document.getElementById('myPassword').value = '';
        document.getElementById('myPasswordConfirm').value = '';
        location.reload();
      } else {
        alert('수정 실패: ' + response.message);
      }
    },
    error: function() {
      alert('수정 중 오류가 발생했습니다.');
    }
  });
}

// 엔터 키로 검색
$(document).ready(function() {
  $('#searchInput').on('keypress', function(e) {
    if (e.which === 13) {
      searchAdmins();
    }
  });
});

// 전역 함수로 등록 (JSP에서 호출할 수 있도록)
window.searchAdmins = searchAdmins;
window.addAdmin = addAdmin;
window.editAdmin = editAdmin;
window.updateAdmin = updateAdmin;
window.viewAdminDetail = viewAdminDetail;
window.editAdminFromDetail = editAdminFromDetail;
window.deleteAdminFromModal = deleteAdminFromModal;
window.updateMyInfo = updateMyInfo;