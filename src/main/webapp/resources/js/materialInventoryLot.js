function showLotDetails(materialId) {
	    console.log('=== LOT 조회 시작 ===');
	    console.log('materialId:', materialId);
	    
	    // 모달 표시
	    $('#lotModal').modal('show');
	    
	    // 즉시 데이터 로드 (모달 이벤트 대기하지 않음)
	    loadLotData(materialId);
	}
	
	function loadLotData(materialId) {
	    $.ajax({
	        url: '/material/inventory/lot-details',
	        method: 'GET',
	        data: { materialId: materialId },
	        dataType: 'json',
	        beforeSend: function() {
	            console.log('AJAX 요청 전송 중...');
	            $('#lotTableBody').html('<tr><td colspan="5" class="text-center">데이터를 불러오는 중...</td></tr>');
	        },
	        success: function(response) {
	            console.log('=== AJAX 성공 ===');
	            console.log('응답 데이터:', response);
	            console.log('응답 타입:', typeof response);
	            console.log('응답 길이:', Array.isArray(response) ? response.length : 'not array');
	            
	            const tbody = $('#lotTableBody');
	            tbody.empty();
	            
	        	 // 응답 데이터 검증
	            if (!response) {
	                console.log('응답 데이터가 null/undefined');
	                tbody.html('<tr><td colspan="5" class="text-center text-muted">응답 데이터가 없습니다.</td></tr>');
	                return;
	            }
	
	            // ✅ 먼저 dataArray 선언
	            let dataArray = Array.isArray(response) ? response : [response];
	
	            console.log('응답 타입:', typeof response);
	            console.log('응답 길이:', dataArray.length);
	
	            console.log('=== LOT 디버깅 시작 ===');
	            dataArray.forEach((item, index) => {
	              console.log(`[${index}] lotNo:`, item.lotNo);
	              console.log(`[${index}] quantity:`, item.quantity);
	              console.log(`[${index}] expirationDate:`, item.expirationDate);
	              console.log(`[${index}] warehouseCode:`, item.warehouseCode);
	              console.log(`[${index}] inventoryStatus:`, item.inventoryStatus);
	            });
	
	            
	            if (dataArray.length === 0) {
	                console.log('빈 배열 수신');
	                tbody.html('<tr><td colspan="5" class="text-center text-muted">해당 자재의 LOT 정보가 없습니다.</td></tr>');
	                return;
	            }
	            
	         // 각 LOT 데이터 처리
	            dataArray.forEach(function(item, index) {
	                console.log(`LOT ${index + 1} 처리:`, item);
	
	                // 안전한 데이터 추출
	                const lotNo = item.lotNo || item.lot_no || '-';
	                const quantity = formatNumber(item.quantity);
	                const warehouseCode = item.warehouseCode || item.warehouse_code || '-';
	                const expirationDate = formatDate(item.expirationDate || item.expiration_date);
	                const status = item.inventoryStatus || item.inventory_status || '정상';
	
	                // 상태 배지 생성
	                const statusBadge = createStatusBadge(status);
	
	                // ✅ 실제 데이터를 row에 출력
	                const row = `
	                    <tr>
	                        <td>${lotNo}</td>
	                        <td>${quantity}</td>
	                        <td>${expirationDate}</td>
	                        <td>${warehouseCode}</td>
	                        <td>${statusBadge}</td>
	                    </tr>
	                `;
	
	                tbody.append(row);
	                console.log(`LOT ${index + 1} 행 추가 완료`);
	            });
	            
	            console.log('모든 LOT 데이터 처리 완료. 총 행 수:', tbody.find('tr').length);
	        },
	        error: function(xhr, status, error) {
	            console.error('=== AJAX 오류 ===');
	            console.error('Status:', xhr.status);
	            console.error('Error:', error);
	            console.error('Response Text:', xhr.responseText);
	            
	            let errorMessage = '오류가 발생했습니다.';
	            
	            if (xhr.status === 404) {
	                errorMessage = 'API 엔드포인트를 찾을 수 없습니다.';
	            } else if (xhr.status === 500) {
	                errorMessage = '서버 내부 오류가 발생했습니다.';
	            } else if (xhr.status === 0) {
	                errorMessage = '네트워크 연결을 확인해주세요.';
	            }
	            
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