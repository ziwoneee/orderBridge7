function showLotDetails(materialId) {
    console.log('=== LOT 조회 시작 ===');
    console.log('materialId:', materialId);
    
    // 🔥 모달을 먼저 표시하고 데이터 초기화
    $('#lotModal').modal('show');
    
    // 🔥 기존 데이터 완전히 초기화
    $('#lotTableBody').html('<tr><td colspan="5" class="text-center">데이터를 불러오는 중...</td></tr>');
    $('#modalMaterialId').text('-');
    $('#modalMaterialName').text('-');
    $('#modalMaterialType').text('-');
    $('#modalMaterialUnit').text('-');
    $('#lotPagination').empty();
    
    // 🔥 전역 변수 초기화
    lotData = [];
    currentPage = 1;
    currentSort = { column: '', order: '' };
    
    // 즉시 데이터 로드
    loadLotData(materialId);
}

//LOT 목록을 자재 ID 기준으로 불러오는 함수
function loadLotData(materialId) {
  $.ajax({
    url: '/material/inventory/lot-details',
    method: 'GET',
    data: { materialId: materialId },
    dataType: 'json',
    cache: false,  // 🔥 캐시 비활성화
    beforeSend: function () {
      console.log('=== AJAX 요청 시작 ===');
      console.log('요청 URL:', '/material/inventory/lot-details');
      console.log('요청 materialId:', materialId);
      
      // 🔥 로딩 상태 명확히 표시
      $('#lotTableBody').html('<tr><td colspan="5" class="text-center">데이터를 불러오는 중...</td></tr>');
      $('#lotPagination').empty();
    },
    success: function (response) {
      console.log('=== AJAX 응답 성공 ===');
      console.log('응답 데이터:', response);
      console.log('응답 데이터 타입:', typeof response);
      console.log('응답 데이터 길이:', response ? response.length : 'undefined');
      
      const tbody = $('#lotTableBody');
      tbody.empty();
      
      // 🔥 응답 데이터 유효성 검증 강화
      if (!response) {
        console.error('응답 데이터가 null 또는 undefined입니다.');
        tbody.html('<tr><td colspan="5" class="text-center text-danger">데이터를 불러올 수 없습니다.</td></tr>');
        return;
      }
      
      // 배열이 아닌 경우 배열로 변환
      const responseArray = Array.isArray(response) ? response : [response];
      console.log('변환된 배열:', responseArray);
      
      // 🔥 응답 구조 확인 (백엔드 수정 후)
      if (response.materialId) {
        // 새로운 응답 구조: {materialId, materialName, materialType, unit, lotList}
        console.log('새로운 응답 구조 감지');
        
        $('#modalMaterialId').text(response.materialId || '-');
        $('#modalMaterialName').text(response.materialName || '-');
        $('#modalMaterialType').text(response.materialType || '-');
        $('#modalMaterialUnit').text(response.unit || '-');
        
        // LOT 데이터는 lotList에서 가져옴
        lotData = response.lotList || [];
        
      } else {
        // 기존 응답 구조: LOT 배열 직접 반환
        console.log('기존 응답 구조 사용');
        
        const responseArray = Array.isArray(response) ? response : [response];
        
        // 자재 정보 바인딩 (빈 배열이어도 materialId로 기본 정보 설정)
        $('#modalMaterialId').text(materialId || '-');
        
        if (responseArray.length > 0) {
          const material = responseArray[0];
          console.log('자재 정보 바인딩:', material);
          
          $('#modalMaterialName').text(material.materialName || '-');
          $('#modalMaterialType').text(material.materialType || '-');
          $('#modalMaterialUnit').text(material.unit || '-');
          
        } else {
          console.log('LOT 데이터가 비어있어 기본 자재 정보만 표시');
          // 🔥 빈 배열인 경우 자재 이름 등을 알 수 없으므로 기본값 설정
          $('#modalMaterialName').text('정보 없음');
          $('#modalMaterialType').text('-');
          $('#modalMaterialUnit').text('-');
        }
        
        // LOT 데이터 설정
        lotData = responseArray;
      }

      // (1) 전역 배열에 LOT 데이터 저장 → 정렬/페이징에 필요
      currentPage = 1;                  // 페이지 초기화
      currentSort = { column: '', order: '' };  // 정렬 상태 초기화
      
      console.log('전역 lotData 설정:', lotData);
      console.log('lotData 길이:', lotData.length);

      // (2) 응답 유효성 검사
      if (!lotData || lotData.length === 0) {
        console.log('LOT 데이터 없음');
        tbody.html('<tr><td colspan="5" class="text-center text-muted">해당 자재의 LOT 정보가 없습니다.</td></tr>');
        $('#lotPagination').empty();  // 페이징 초기화
        return;
      }

      // (3) 디버깅용 로그
      console.log('=== LOT 디버깅 시작 ===');
      lotData.forEach((item, index) => {
        console.log(`[${index}] LOT 데이터:`, item);
        console.log(`[${index}] lotNo:`, item.lotNo);
        console.log(`[${index}] quantity:`, item.quantity);
        console.log(`[${index}] expirationDate:`, item.expirationDate);
        console.log(`[${index}] warehouseCode:`, item.warehouseCode);
        console.log(`[${index}] inventoryStatus:`, item.inventoryStatus);
      });

      // (4) 테이블 & 페이징 렌더링
      console.log('테이블 렌더링 시작');
      renderLotTable();       // 전역 배열 기반 테이블 렌더링
      renderPagination();     // 페이징 버튼 생성
      renderLotHeaders();     // 헤더 아이콘 렌더링
      
      console.log('테이블 렌더링 완료');
    },
    error: function (xhr, status, error) {
      console.error('=== AJAX 오류 ===');
      console.error('Status:', xhr.status);
      console.error('Error:', error);
      console.error('Response Text:', xhr.responseText);
      console.error('materialId:', materialId);

      let errorMessage = '오류가 발생했습니다.';
      if (xhr.status === 404) errorMessage = 'API 엔드포인트를 찾을 수 없습니다.';
      else if (xhr.status === 500) errorMessage = '서버 내부 오류가 발생했습니다.';
      else if (xhr.status === 0) errorMessage = '네트워크 연결을 확인해주세요.';

      $('#lotTableBody').html(`
        <tr>
          <td colspan="5" class="text-center text-danger">
            ${errorMessage}<br>
            <small class="text-muted">(Status: ${xhr.status}, Error: ${error})</small>
          </td>
        </tr>
      `);
      
      // 🔥 오류 시에도 전역 변수 초기화
      lotData = [];
      currentPage = 1;
      currentSort = { column: '', order: '' };
    }
  });
}

// 숫자 포맷팅 함수
function formatNumber(value) {
    if (value === null || value === undefined || value === '') {
        return '0';
    }
    
    const num = Number(value);
    if (isNaN(num)) {
        return '0';
    }
    
    return num.toLocaleString();
}

// 날짜 포맷팅 함수
function formatDate(dateValue) {
    if (!dateValue) {
        return '-';
    }
    
    try {
        let date;
        
        // 문자열인 경우
        if (typeof dateValue === 'string') {
            // ISO 형식 또는 타임스탬프 확인
            if (dateValue.includes('-') || dateValue.includes('/')) {
                date = new Date(dateValue);
            } else if (/^\d+$/.test(dateValue)) {
                // 숫자 문자열인 경우 타임스탬프로 처리
                date = new Date(parseInt(dateValue));
            } else {
                date = new Date(dateValue);
            }
        } 
        // 숫자인 경우 타임스탬프로 처리
        else if (typeof dateValue === 'number') {
            date = new Date(dateValue);
        } 
        // Date 객체인 경우
        else if (dateValue instanceof Date) {
            date = dateValue;
        } 
        // 객체인 경우 (예: {time: timestamp})
        else if (typeof dateValue === 'object' && dateValue.time) {
            date = new Date(dateValue.time);
        } else {
            return '-';
        }
        
        // 유효한 날짜인지 확인
        if (isNaN(date.getTime())) {
            console.error('Invalid date:', dateValue);
            return '-';
        }
        
        // YYYY-MM-DD 형식으로 포맷
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
        
    } catch (error) {
        console.error('Date formatting error:', error, 'Original value:', dateValue);
        return '-';
    }
}

// 상태 배지 생성 함수
function createStatusBadge(status) {
    const statusMap = {
        '정상': 'badge-success',
        '부족': 'badge-warning', 
        '위험': 'badge-danger'
    };
    
    const badgeClass = statusMap[status] || 'badge-secondary';
    return `<span class="badge ${badgeClass}">${status}</span>`;
}

/************************************************************
 * materialInventoryLot.js - 모달 내 LOT 테이블 정렬 및 페이징 (최신)
 ************************************************************/

let lotData = []; // LOT 전체 데이터를 담는 전역 배열
let currentPage = 1; // 현재 페이지
let itemsPerPage = 10; // 페이지당 항목 수
let currentSort = { column: '', order: '' }; // 정렬 상태

// 정렬 아이콘 반환 함수 (⇅ / ↑ / ↓ 형태)
function getSortIcon(column) {
  if (currentSort.column === column) {
    return currentSort.order === 'asc'
        ? '<i class="ti-arrow-up"></i>'
        : '<i class="ti-arrow-down"></i>';
  }
  return '⇅';
}

// [2] LOT 테이블 렌더링 함수
function renderLotTable() {
  console.log('=== renderLotTable 시작 ===');
  console.log('현재 lotData:', lotData);
  console.log('현재 페이지:', currentPage);
  console.log('페이지당 항목수:', itemsPerPage);
  
  const tbody = $('#lotTableBody');
  tbody.empty();

  if (!lotData || lotData.length === 0) {
    console.log('렌더링할 데이터가 없음');
    tbody.html('<tr><td colspan="5" class="text-center text-muted">LOT 정보가 없습니다.</td></tr>');
    return;
  }

  const start = (currentPage - 1) * itemsPerPage;
  const pageItems = lotData.slice(start, start + itemsPerPage);
  
  console.log('렌더링 범위:', start, '~', start + itemsPerPage);
  console.log('페이지 아이템:', pageItems);

  pageItems.forEach((item, index) => {
    console.log(`렌더링 아이템 [${index}]:`, item);
    
    const row = `<tr>
      <td>${item.lotNo || '-'}</td>
      <td>${formatNumber(item.quantity)}</td>
      <td>${formatDate(item.expirationDate)}</td>
      <td>${item.warehouseCode || '-'}</td>
      <td>${createStatusBadge(item.inventoryStatus || '정상')}</td>
    </tr>`;
    tbody.append(row);
  });
  
  console.log('=== renderLotTable 완료 ===');
}

// [2.1] 헤더 아이콘 렌더링
function renderLotHeaders() {
  $('#lotTableHeader th').each(function () {
    const col = $(this).data('column');
    if (col) {
      $(this).find('span.sort-icon').remove();
      const icon = getSortIcon(col);
      $(this).find('a').append(`<span class="ml-1 sort-icon">${icon}</span>`);
    }
  });
}

// [3] 페이징 버튼 생성 (이전/다음 포함)
function renderPagination() {
  console.log('=== renderPagination 시작 ===');
  
  if (!lotData || lotData.length === 0) {
    console.log('페이징할 데이터가 없음');
    $('#lotPagination').empty();
    return;
  }
  
  const totalPages = Math.ceil(lotData.length / itemsPerPage);
  console.log('총 페이지 수:', totalPages);
  console.log('현재 페이지:', currentPage);
  
  const pagination = $('#lotPagination');
  pagination.empty();

  if (totalPages <= 1) {
    console.log('페이지가 1개뿐이므로 페이징 버튼 생성하지 않음');
    return;
  }

  if (currentPage > 1) {
    pagination.append(`
      <li class="page-item">
        <a class="page-link" href="#" onclick="goToPage(${currentPage - 1})">&laquo;</a>
      </li>
    `);
  }

  for (let i = 1; i <= totalPages; i++) {
    const active = i === currentPage ? 'active' : '';
    pagination.append(`
      <li class="page-item ${active}">
        <a class="page-link" href="#" onclick="goToPage(${i})">${i}</a>
      </li>
    `);
  }

  if (currentPage < totalPages) {
    pagination.append(`
      <li class="page-item">
        <a class="page-link" href="#" onclick="goToPage(${currentPage + 1})">&raquo;</a>
      </li>
    `);
  }
  
  console.log('=== renderPagination 완료 ===');
}

// [4] 페이지 이동
function goToPage(page) {
  console.log('페이지 이동:', currentPage, '->', page);
  currentPage = page;
  renderLotTable();
  renderPagination();
  renderLotHeaders();
}

// [5] 정렬 함수
function sortLotBy(column) {
  console.log('정렬 요청:', column);
  
  if (!lotData || lotData.length === 0) {
    console.log('정렬할 데이터가 없음');
    return;
  }
  
  const order = (currentSort.column === column && currentSort.order === 'asc') ? 'desc' : 'asc';
  currentSort = { column, order };
  
  console.log('정렬 설정:', currentSort);

  lotData.sort((a, b) => {
    let valA = a[column];
    let valB = b[column];

    if (column === 'expirationDate') {
      valA = new Date(valA);
      valB = new Date(valB);
    }

    if (valA == null) return 1;
    if (valB == null) return -1;

    if (typeof valA === 'number' && typeof valB === 'number') {
      return order === 'asc' ? valA - valB : valB - valA;
    } else if (valA instanceof Date && valB instanceof Date) {
      return order === 'asc' ? valA - valB : valB - valA;
    } else {
      return order === 'asc'
        ? String(valA).localeCompare(String(valB))
        : String(valB).localeCompare(String(valA));
    }
  });

  currentPage = 1;
  renderLotTable();
  renderPagination();
  renderLotHeaders();
  
  console.log('정렬 완료');
}