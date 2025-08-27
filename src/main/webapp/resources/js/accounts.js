// accounts.js - 관리자 계정 관리 JavaScript (ES5 호환 버전)

// 전역 변수 및 초기화
var contextPath = '';
var isSuperAdmin = false;
var currentAdminId = '';

// DOM이 로드된 후 초기화
document.addEventListener('DOMContentLoaded', function () {
  // contextPath 설정
  var metaContextPath = document.querySelector('meta[name="contextPath"]');
  if (metaContextPath) {
    contextPath = metaContextPath.getAttribute('content');
  }

  // 전역 변수 초기화 (JSP에서 설정된 값 사용)
  if (typeof window.isSuperAdmin !== 'undefined') {
    isSuperAdmin = window.isSuperAdmin;
  }
  if (typeof window.currentAdminId !== 'undefined') {
    currentAdminId = window.currentAdminId;
  }

  // 이벤트 리스너 등록
  initializeEventListeners();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
  // 검색 입력창 엔터 키 이벤트
  var searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('keypress', function (e) {
      if (e.key === 'Enter') {
        searchAdmins();
      }
    });
  }

  // 관리자 ID 접두사 변경 시 역할 자동 설정
  var adminIdPrefix = document.getElementById('adminIdPrefix');
  var adminRole = document.getElementById('adminRole');
  if (adminIdPrefix && adminRole) {
    adminIdPrefix.addEventListener('change', function () {
      var prefix = this.value;
      var roleMap = {
        'A': 'SUPER',
        'P': 'PROD',
        'S': 'SALES',
        'M': 'MATERIAL'
      };
      if (roleMap[prefix]) {
        adminRole.value = roleMap[prefix];
      }
    });
  }
}

// 전화번호 자동 포맷팅 함수 (JSP에서도 사용)
function formatPhoneNumber(input) {
  var value = input.value.replace(/[^0-9]/g, '');
  if (value.length >= 3 && value.length <= 7) {
    value = value.replace(/(\d{3})(\d+)/, '$1-$2');
  } else if (value.length >= 8) {
    value = value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
  }
  input.value = value;
}

// 전화번호 유효성 검증
function validatePhoneNumber(phone) {
  if (!phone || phone.trim() === '') {
    return true; // 빈 전화번호는 유효 (선택사항)
  }
  var phoneRegex = /^010-\d{4}-\d{4}$/;
  return phoneRegex.test(phone);
}

// 전화번호 중복 확인 (Promise 반환)
function checkPhoneDuplicate(phone, currentId) {
  return new Promise(function (resolve, reject) {
    if (!phone || phone.trim() === '') {
      resolve(false); // 빈 전화번호는 중복 아님
      return;
    }

    if (typeof $ === 'undefined') {
      reject('jQuery가 로드되지 않았습니다.');
      return;
    }

    $.ajax({
      url: contextPath + '/admin/settings/accounts/check-phone',
      type: 'GET',
      data: {
        phone: phone,
        currentAdminId: currentId || null
      },
      success: function (response) {
        resolve(response && response.isDuplicate ? true : false);
      },
      error: function (xhr, status, error) {
        console.error('전화번호 중복 확인 오류:', error);
        resolve(false); // 오류 시에는 중복이 아닌 것으로 처리하여 진행
      }
    });
  });
}

// 검색 기능 (URLSearchParams 미사용)
function searchAdmins() {
  var searchInput = document.getElementById('searchInput');
  var roleFilter = document.getElementById('roleFilter');
  var statusFilter = document.getElementById('statusFilter');

  if (!searchInput || !roleFilter || !statusFilter) {
    console.error('검색 요소를 찾을 수 없습니다.');
    return;
  }

  var searchValue = searchInput.value.trim();
  var roleValue = roleFilter.value;
  var statusValue = statusFilter.value;

  var params = [];
  if (searchValue) params.push('search=' + encodeURIComponent(searchValue));
  if (roleValue) params.push('role=' + encodeURIComponent(roleValue));
  if (statusValue) params.push('status=' + encodeURIComponent(statusValue));

  var url = contextPath + '/admin/settings/accounts' + (params.length ? '?' + params.join('&') : '');
  window.location.href = url;
}

// 관리자 추가 (async/await 제거)
function addAdmin() {
  try {
    // 필수 요소들 가져오기
    var elements = {
      password: document.getElementById('adminPassword'),
      passwordConfirm: document.getElementById('adminPasswordConfirm'),
      prefix: document.getElementById('adminIdPrefix'),
      number: document.getElementById('adminIdNumber'),
      name: document.getElementById('adminName'),
      phone: document.getElementById('adminPhone'),
      role: document.getElementById('adminRole')
    };

    // 요소 존재 확인
    for (var k in elements) {
      if (Object.prototype.hasOwnProperty.call(elements, k)) {
        if (!elements[k]) {
          alert(k + ' 요소를 찾을 수 없습니다.');
          return;
        }
      }
    }

    var password = elements.password.value;
    var passwordConfirm = elements.passwordConfirm.value;
    var prefix = elements.prefix.value;
    var number = elements.number.value;
    var name = elements.name.value.trim();
    var phone = elements.phone.value.trim();
    var role = elements.role.value;

    // 유효성 검증
    if (!name) { alert('이름을 입력해주세요.'); elements.name.focus(); return; }
    if (!password) { alert('비밀번호를 입력해주세요.'); elements.password.focus(); return; }
    if (password !== passwordConfirm) { alert('비밀번호가 일치하지 않습니다.'); elements.passwordConfirm.focus(); return; }
    if (!number || number.length !== 4 || !/^\d{4}$/.test(number)) { alert('사번은 4자리 숫자로 입력해주세요. (예: 0001)'); elements.number.focus(); return; }
    if (!role) { alert('역할을 선택해주세요.'); elements.role.focus(); return; }
    if (phone && !validatePhoneNumber(phone)) { alert('전화번호는 010-0000-0000 형식으로 입력해주세요.'); elements.phone.focus(); return; }

    var proceed = function () {
      var adminId = prefix + number;
      var adminData = {
        adminId: adminId,
        name: name,
        password: password,
        phone: phone || null,
        roleId: role
      };

      $.ajax({
        url: contextPath + '/admin/settings/accounts',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(adminData),
        success: function (response) {
          if (response && response.success) {
            alert('관리자가 성공적으로 등록되었습니다.\n사번: ' + adminId);
            $('#addAdminModal').modal('hide');
            var f = document.getElementById('addAdminForm');
            if (f && typeof f.reset === 'function') { f.reset(); }
            location.reload();
          } else {
            alert('등록 실패: ' + (response && response.message ? response.message : '알 수 없는 오류'));
          }
        },
        error: function (xhr, status, error) {
          console.error('관리자 등록 오류:', error);
          alert('등록 중 오류가 발생했습니다.');
        }
      });
    };

    // 전화번호 중복 확인 후 진행
    if (phone) {
      checkPhoneDuplicate(phone).then(function (isDup) {
        if (isDup) {
          alert('이미 사용 중인 전화번호입니다.');
          elements.phone.focus();
          return;
        }
        proceed();
      }).catch(function (err) {
        console.error('전화번호 중복 확인 오류:', err);
        proceed(); // 오류 시에도 진행
      });
    } else {
      proceed();
    }

  } catch (error) {
    console.error('addAdmin 오류:', error);
    alert('등록 중 오류가 발생했습니다.');
  }
}

// 관리자 수정 모달 열기
function editAdmin(adminId) {
  if (!adminId) {
    alert('유효하지 않은 관리자 ID입니다.');
    return;
  }

  $.ajax({
    url: contextPath + '/admin/settings/accounts/' + encodeURIComponent(adminId),
    type: 'GET',
    success: function (admin) {
      if (!admin) {
        alert('관리자 정보를 찾을 수 없습니다.');
        return;
      }

      // 모달 필드 채우기
      var elements = {
        editAdminId: document.getElementById('editAdminId'),
        editAdminIdDisplay: document.getElementById('editAdminIdDisplay'),
        editAdminName: document.getElementById('editAdminName'),
        editAdminPhone: document.getElementById('editAdminPhone'),
        editAdminRole: document.getElementById('editAdminRole'),
        editAdminStatus: document.getElementById('editAdminStatus')
      };

      if (elements.editAdminId) elements.editAdminId.value = admin.adminId || '';
      if (elements.editAdminIdDisplay) elements.editAdminIdDisplay.value = admin.adminId || '';
      if (elements.editAdminName) elements.editAdminName.value = admin.name || '';
      if (elements.editAdminPhone) elements.editAdminPhone.value = admin.phone || '';
      if (elements.editAdminRole) elements.editAdminRole.value = admin.roleId || '';
      if (elements.editAdminStatus) elements.editAdminStatus.value = admin.status || 'ACTIVE';

      // 잠김 상태면 잠금해제 버튼 표시
      var unlockBtn = document.getElementById('unlockBtn');
      if (unlockBtn) {
        if (admin.status === 'LOCKED') {
          unlockBtn.style.display = 'inline-block';
        } else {
          unlockBtn.style.display = 'none';
        }
      }

      $('#editAdminModal').modal('show');
    },
    error: function (xhr, status, error) {
      console.error('관리자 정보 조회 오류:', error);
      alert('관리자 정보를 가져오는데 실패했습니다.');
    }
  });
}

// 관리자 정보 업데이트 (async/await 제거)
function updateAdmin() {
  try {
    var elements = {
      adminId: document.getElementById('editAdminId'),
      name: document.getElementById('editAdminName'),
      password: document.getElementById('editAdminPassword'),
      phone: document.getElementById('editAdminPhone'),
      role: document.getElementById('editAdminRole'),
      status: document.getElementById('editAdminStatus')
    };

    for (var k in elements) {
      if (Object.prototype.hasOwnProperty.call(elements, k)) {
        if (!elements[k]) { alert(k + ' 요소를 찾을 수 없습니다.'); return; }
      }
    }

    var adminId = elements.adminId.value;
    var name = elements.name.value.trim();
    var password = elements.password.value;
    var phone = elements.phone.value.trim();
    var role = elements.role.value;
    var status = elements.status.value;

    if (!adminId) { alert('관리자 ID가 없습니다.'); return; }
    if (!name) { alert('이름을 입력해주세요.'); elements.name.focus(); return; }
    if (phone && !validatePhoneNumber(phone)) { alert('전화번호는 010-0000-0000 형식으로 입력해주세요.'); elements.phone.focus(); return; }

    var afterDupCheck = function () {
      var adminData = {
        adminId: adminId,
        name: name,
        phone: phone || null,
        roleId: role,
        status: status
      };
      if (password && password.trim() !== '') { adminData.password = password; }

      $.ajax({
        url: contextPath + '/admin/settings/accounts/' + encodeURIComponent(adminId),
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(adminData),
        success: function (response) {
          if (response && response.success) {
            alert('관리자 정보가 성공적으로 수정되었습니다.');
            $('#editAdminModal').modal('hide');
            location.reload();
          } else {
            alert('수정 실패: ' + (response && response.message ? response.message : '알 수 없는 오류'));
          }
        },
        error: function (xhr, status, error) {
          console.error('관리자 수정 오류:', error);
          alert('수정 중 오류가 발생했습니다.');
        }
      });
    };

    if (phone) {
      checkPhoneDuplicate(phone, adminId).then(function (isDup) {
        if (isDup) { alert('이미 사용 중인 전화번호입니다.'); elements.phone.focus(); return; }
        afterDupCheck();
      }).catch(function (err) {
        console.error('전화번호 중복 확인 오류:', err);
        afterDupCheck(); // 오류 시에도 진행
      });
    } else {
      afterDupCheck();
    }

  } catch (error) {
    console.error('updateAdmin 오류:', error);
    alert('수정 중 오류가 발생했습니다.');
  }
}

// 수정 모달에서 잠금 해제
function unlockAccountFromModal() {
  var adminIdElement = document.getElementById('editAdminId');
  if (!adminIdElement) {
    alert('관리자 ID를 찾을 수 없습니다.');
    return;
  }

  var adminId = adminIdElement.value;
  if (!adminId) {
    alert('유효하지 않은 관리자 ID입니다.');
    return;
  }

  if (confirm('계정 잠금을 해제하시겠습니까?')) {
    $.ajax({
      url: contextPath + '/admin/settings/accounts/' + encodeURIComponent(adminId) + '/unlock',
      type: 'PUT',
      success: function (response) {
        if (response && response.success) {
          alert('계정 잠금이 해제되었습니다.');
          $('#editAdminModal').modal('hide');
          location.reload();
        } else {
          alert('해제 실패: ' + (response && response.message ? response.message : '알 수 없는 오류'));
        }
      },
      error: function (xhr, status, error) {
        console.error('잠금 해제 오류:', error);
        alert('잠금 해제 중 오류가 발생했습니다.');
      }
    });
  }
}

// 관리자 상세보기
function viewAdminDetail(adminId) {
  if (!adminId) {
    alert('유효하지 않은 관리자 ID입니다.');
    return;
  }

  $.ajax({
    url: contextPath + '/admin/settings/accounts/' + encodeURIComponent(adminId),
    type: 'GET',
    success: function (admin) {
      if (!admin) {
        alert('관리자 정보를 찾을 수 없습니다.');
        return;
      }

      var detailHtml = '';
      detailHtml += '<tr><td><strong>사번</strong></td><td>' + (admin.adminId || '-') + '</td></tr>';
      detailHtml += '<tr><td><strong>이름</strong></td><td>' + (admin.name || '-') + '</td></tr>';
      detailHtml += '<tr><td><strong>소속/역할</strong></td><td>' + getRoleNameDetail(admin.roleId) + '</td></tr>';
      detailHtml += '<tr><td><strong>연락처</strong></td><td>' + (admin.phone || '-') + '</td></tr>';
      detailHtml += '<tr><td><strong>상태</strong></td><td>' + getStatusBadge(admin.status) + '</td></tr>';
      detailHtml += '<tr><td><strong>실패횟수</strong></td><td>' + (admin.failCount || 0) + '/5</td></tr>';
      detailHtml += '<tr><td><strong>등록일</strong></td><td>' + formatDate(admin.createdAt) + '</td></tr>';
      detailHtml += '<tr><td><strong>최종수정일</strong></td><td>' + formatDate(admin.updatedAt) + '</td></tr>';

      var detailBody = document.getElementById('adminDetailBody');
      if (detailBody) {
        detailBody.innerHTML = detailHtml;
      }

      $('#adminDetailModal').data('admin-id', adminId);
      $('#adminDetailModal').modal('show');
    },
    error: function (xhr, status, error) {
      console.error('관리자 상세 조회 오류:', error);
      alert('관리자 정보를 가져오는데 실패했습니다.');
    }
  });
}

// 상세 모달에서 수정 모달로 이동
function editAdminFromDetail() {
  var adminId = $('#adminDetailModal').data('admin-id');
  if (!adminId) {
    alert('관리자 ID를 찾을 수 없습니다.');
    return;
  }

  $('#adminDetailModal').modal('hide');
  setTimeout(function () {
    editAdmin(adminId);
  }, 300);
}

// 수정 모달에서 삭제 (소프트 삭제)
function deleteAdminFromModal() {
  var adminIdElement = document.getElementById('editAdminId');
  if (!adminIdElement) {
    alert('관리자 ID를 찾을 수 없습니다.');
    return;
  }

  var adminId = adminIdElement.value;
  if (!adminId) {
    alert('유효하지 않은 관리자 ID입니다.');
    return;
  }

  if (adminId === currentAdminId) {
    alert('본인 계정은 삭제할 수 없습니다.');
    return;
  }

  if (confirm('정말로 이 관리자를 삭제하시겠습니까?\n삭제된 계정은 복구할 수 있습니다.')) {
    $.ajax({
      url: contextPath + '/admin/settings/accounts/' + encodeURIComponent(adminId) + '/soft-delete',
      type: 'PUT',
      success: function (response) {
        if (response && response.success) {
          alert('관리자가 성공적으로 삭제되었습니다.');
          $('#editAdminModal').modal('hide');
          location.reload();
        } else {
          alert('삭제 실패: ' + (response && response.message ? response.message : '알 수 없는 오류'));
        }
      },
      error: function (xhr, status, error) {
        console.error('관리자 삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
      }
    });
  }
}

// 일반 관리자 - 내 정보 수정 (본인 제외 중복 확인)
function updateMyInfo() {
  try {
    var elements = {
      name: document.getElementById('myName'),
      password: document.getElementById('myPassword'),
      passwordConfirm: document.getElementById('myPasswordConfirm'),
      phone: document.getElementById('myPhone')
    };

    for (var k in elements) {
      if (Object.prototype.hasOwnProperty.call(elements, k)) {
        if (!elements[k]) { alert(k + ' 요소를 찾을 수 없습니다.'); return; }
      }
    }

    var name = elements.name.value.trim();
    var password = elements.password.value;
    var passwordConfirm = elements.passwordConfirm.value;
    var phone = elements.phone.value.trim();

    if (!name) { alert('이름을 입력해주세요.'); elements.name.focus(); return; }
    if (password && password !== passwordConfirm) { alert('비밀번호가 일치하지 않습니다.'); elements.passwordConfirm.focus(); return; }
    if (phone && !validatePhoneNumber(phone)) { alert('전화번호는 010-0000-0000 형식으로 입력해주세요.'); elements.phone.focus(); return; }

    var go = function () {
      var myData = {
        adminId: currentAdminId,
        name: name,
        phone: phone || null
      };
      if (password && password.trim() !== '') { myData.password = password; }

      $.ajax({
        url: contextPath + '/admin/settings/accounts/my-info',
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(myData),
        success: function (response) {
          if (response && response.success) {
            alert('정보가 성공적으로 수정되었습니다.');
            elements.password.value = '';
            elements.passwordConfirm.value = '';
            location.reload();
          } else {
            alert('수정 실패: ' + (response && response.message ? response.message : '알 수 없는 오류'));
          }
        },
        error: function (xhr, status, error) {
          console.error('내 정보 수정 오류:', error);
          alert('수정 중 오류가 발생했습니다.');
        }
      });
    };

    // 전화번호가 있으면 중복 확인 (본인 제외)
    if (phone) {
      checkPhoneDuplicate(phone, currentAdminId).then(function (isDup) {
        if (isDup) { 
          alert('이미 사용 중인 전화번호입니다.'); 
          elements.phone.focus(); 
          return; 
        }
        go();
      }).catch(function (err) {
        console.error('전화번호 중복 확인 오류:', err);
        go(); // 오류 시에는 그냥 진행
      });
    } else {
      go();
    }

  } catch (error) {
    console.error('updateMyInfo 오류:', error);
    alert('수정 중 오류가 발생했습니다.');
  }
}

// 헬퍼 함수들
function getRoleNameDetail(roleId) {
  var roleMap = {
    'SUPER': '<span class="badge badge-danger">최고관리자</span>',
    'PROD': '<span class="badge badge-success">생산관리자</span>',
    'SALES': '<span class="badge badge-info">영업관리자</span>',
    'MATERIAL': '<span class="badge badge-warning">자재관리자</span>'
  };
  return roleMap[roleId] || '<span class="badge badge-secondary">' + (roleId || '알 수 없음') + '</span>';
}

function getStatusBadge(status) {
  if (status === 'LOCKED') {
    return '<span class="badge badge-danger">잠김</span>';
  } else if (status === 'ACTIVE') {
    return '<span class="badge badge-success">재직</span>';
  } else if (status === 'INACTIVE') {
    return '<span class="badge badge-secondary">휴직</span>';
  } else {
    return '<span class="badge badge-light">' + (status || '알 수 없음') + '</span>';
  }
}

function formatDate(dateString) {
  if (!dateString) return '-';
  try {
    return new Date(dateString).toLocaleDateString('ko-KR');
  } catch (error) {
    console.error('날짜 포맷팅 오류:', error);
    return '-';
  }
}

// 전역 함수로 등록 (JSP에서 호출할 수 있도록)
window.formatPhoneNumber = formatPhoneNumber;
window.searchAdmins = searchAdmins;
window.addAdmin = addAdmin;
window.editAdmin = editAdmin;
window.updateAdmin = updateAdmin;
window.viewAdminDetail = viewAdminDetail;
window.editAdminFromDetail = editAdminFromDetail;
window.deleteAdminFromModal = deleteAdminFromModal;
window.updateMyInfo = updateMyInfo;
window.unlockAccountFromModal = unlockAccountFromModal;