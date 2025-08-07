function showLotDetails(materialId) {
	    console.log('=== LOT 조회 시작 ===');
	    console.log('materialId:', materialId);
	    
	    // 모달 표시
	    $('#lotModal').modal('show');
	    
	    // 즉시 데이터 로드 (모달 이벤트 대기하지 않음)
	    loadLotData(materialId);
	}
	
	//LOT 목록을 자재 ID 기준으로 불러오는 함수
	function loadLotData(materialId) {
	  $.ajax({
	    url: '/material/inventory/lot-details',
	    method: 'GET',
	    data: { materialId: materialId },
	    dataType: 'json',
	    beforeSend: function () {
	      $('#lotTableBody').html('<tr><td colspan="5" class="text-center">데이터를 불러오는 중...</td></tr>');
	    },
	    success: function (response) {
	      const tbody = $('#lotTableBody');
	      tbody.empty();
	
	      // (1) 전역 배열에 LOT 데이터 저장 → 정렬/페이징에 필요
	      lotData = Array.isArray(response) ? response : [response];
	      currentPage = 1;                  // 페이지 초기화
	      currentSort = { column: '', order: '' };  // 정렬 상태 초기화
	
	      // (2) 응답 유효성 검사
	      if (!lotData || lotData.length === 0) {
	        console.log('LOT 데이터 없음');
	        tbody.html('<tr><td colspan="5" class="text-center text-muted">해당 자재의 LOT 정보가 없습니다.</td></tr>');
	        $('#lotPagination').empty();  // 페이징 초기화
	        return;
	      }
	
	      // (3) 디버깅용 로그 (유지)
	      console.log('=== LOT 디버깅 시작 ===');
	      lotData.forEach((item, index) => {
	        console.log(`[${index}] lotNo:`, item.lotNo);
	        console.log(`[${index}] quantity:`, item.quantity);
	        console.log(`[${index}] expirationDate:`, item.expirationDate);
	        console.log(`[${index}] warehouseCode:`, item.warehouseCode);
	        console.log(`[${index}] inventoryStatus:`, item.inventoryStatus);
	      });
	
	      // (4) 테이블 & 페이징 렌더링
	      renderLotTable();       // 전역 배열 기반 테이블 렌더링
	      renderPagination();     // 페이징 버튼 생성
	      renderLotHeaders();
	    },
	    error: function (xhr, status, error) {
	      console.error('=== AJAX 오류 ===');
	      console.error('Status:', xhr.status);
	      console.error('Error:', error);
	      console.error('Response Text:', xhr.responseText);
	
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

	// 날짜 포맷 함수
	function formatDate(dateValue) {
	  if (!dateValue) return '-';
	  const date = new Date(dateValue);
	  if (isNaN(date)) return '-';
	  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
	}

	// 숫자 포맷 함수
	function formatNumber(value) {
	  const num = Number(value);
	  return isNaN(num) ? '0' : num.toLocaleString();
	}

	// 상태 배지 HTML 생성 함수
	function createStatusBadge(status) {
	  const badgeClass = {
	    '정상': 'badge-success',
	    '부족': 'badge-warning',
	    '위험': 'badge-danger'
	  }[status] || 'badge-secondary';

	  return `<span class="badge ${badgeClass}">${status}</span>`;
	}

	// 정렬 아이콘 반환 함수 (⇅ / ↑ / ↓ 형태)
	function getSortIcon(column) {
	  if (currentSort.column === column) {
	    return currentSort.order === 'asc'
        ? '<i class="ti-arrow-up"></i>'
        : '<i class="ti-arrow-down"></i>';
	  }
	  return '⇅';
	}

	// [1] 모달 열기 및 데이터 로드
	function loadInboundDetail(inboundId) {
	  $.ajax({
	    url: '/material/inbound/detail',
	    method: 'GET',
	    data: { inboundId: inboundId },
	    success: function (data) {
	      lotData = data.items || [];
	      currentPage = 1;
	      currentSort = { column: '', order: '' };
	      renderLotTable();
	      renderPagination();
	      renderLotHeaders();
	    },
	    error: function () {
	      alert('LOT 상세 데이터를 불러오는데 실패했습니다.');
	    }
	  });
	}

	// [2] LOT 테이블 렌더링 함수
	function renderLotTable() {
	  const tbody = $('#lotTableBody');
	  tbody.empty();

	  const start = (currentPage - 1) * itemsPerPage;
	  const pageItems = lotData.slice(start, start + itemsPerPage);

	  pageItems.forEach(item => {
	    const row = `<tr>
	      <td>${item.lotNo || '-'}</td>
	      <td>${formatNumber(item.quantity)}</td>
	      <td>${formatDate(item.expirationDate)}</td>
	      <td>${item.warehouseCode || '-'}</td>
	      <td>${createStatusBadge(item.inventoryStatus || '정상')}</td>
	    </tr>`;
	    tbody.append(row);
	  });
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
	  const totalPages = Math.ceil(lotData.length / itemsPerPage);
	  const pagination = $('#lotPagination');
	  pagination.empty();

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
	}

	// [4] 페이지 이동
	function goToPage(page) {
	  currentPage = page;
	  renderLotTable();
	  renderPagination();
	  renderLotHeaders();
	}

	// [5] 정렬 함수
	function sortLotBy(column) {
	  const order = (currentSort.column === column && currentSort.order === 'asc') ? 'desc' : 'asc';
	  currentSort = { column, order };

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
	}
