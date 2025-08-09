/* ---------- 전역: 전체 검증 ---------- */
window.validateAll = function () {
  let ok = true;
  $('#materialLotBody tr').each(function () {
    const req = +($(this).find('.req').data('req') || 0);
    let   sum = +($(this).find('.sum').text() || 0);
    const mid = ($(this).data('material') || '').toString();

    // ★ 물(RM-0015)은 LOT 불필요: 자동으로 합계를 req로 맞춰서 통과
    if (mid === 'RM-0015' && req > 0 && sum !== req) {
      $(this).find('.sum').text(req);
      sum = req;
    }

    if (req !== sum) { ok = false; return false; }
  });
  $('#btnSubmit').prop('disabled', !ok);
  return ok;
};


// 부족분 발주 생성
window.collectShortages = function () {
  const shortages = [];
  $('#materialLotBody tr').each(function(){
    const materialId = $(this).data('material');
    const materialName = $(this).find('.font-weight-bold').text().trim();
    const required = +($(this).find('.req').data('req') || 0);
    const sum = +($(this).find('.sum').text() || 0);
    
    if (sum < required) {
      shortages.push({ 
        materialId: materialId, 
        lackQty: required - sum, 
        materialName: materialName 
      });
    }
  });
  return shortages;
};

/* ---------- 공통 유틸 ---------- */
function toYmd(dateValue) {
  if (!dateValue) return '';
  const date = (dateValue instanceof Date) ? dateValue : new Date(dateValue);
  if (isNaN(date.getTime())) return '';        // 파싱 실패 방지
  
  const year = date.getFullYear();
  const month = ('0' + (date.getMonth() + 1)).slice(-2);
  const day = ('0' + date.getDate()).slice(-2);
  return `${year}-${month}-${day}`;
}

function fmtDate(value) { 
  return toYmd(value); 
}

/* ---------- register.jsp: 초기 로드 ---------- */
$(function(){
  if (!$('#outboundForm').length) return;
  
  const params = new URLSearchParams(location.search);
  const workOrderId = params.get('workOrderId');
  if (!workOrderId) return;

  $.get(ctx + '/material/outbound/work-order', { workOrderId }, function(dto){
    $('#workOrderNo').val(dto.workOrderNo || '');
    $('#productId').val(dto.productId || '');
    $('#lineId').val(dto.lineId || '');
    $('#dueDate').val(toYmd(dto.dueDate));
    renderMaterialRows(dto.materialList || []);
  });
});

/* ---------- 자재 행 렌더링 ---------- */
function renderMaterialRows(items) {
  const $tbody = $('#materialLotBody').empty();

  const promises = (items || []).map(function(item) {
    return $.get(ctx + '/material/inventory/lot-details', { materialId: item.materialId })
      .then(function(lots) {
        lots = lots || [];

        // ★ LOT 필요여부/기본창고 세팅 (fallback: lotFlag, 물은 강제 N)
        const lotRequired = (item.lotRequired === 'N' || item.lotFlag === 'N' || item.materialId === 'RM-0015') ? 'N' : 'Y';
        const defaultWh   = item.defaultWarehouseCode || item.warehouseCode || 'WH003';

        const $row = $('<tr>')
          .attr('data-material', item.materialId)
          .attr('data-lot-required', lotRequired)
          .attr('data-default-warehouse', defaultWh);

        // 1) 자재 정보
        $row.append(
          $('<td>').append(
            $('<div>').addClass('font-weight-bold').text(item.materialName || item.materialId),
            $('<div>').addClass('text-muted').append($('<small>').text(item.materialId))
          )
        );

        // 2) 필요 수량
        $row.append(
          $('<td>').addClass('text-right align-middle').append(
            $('<span>').addClass('req').attr('data-req', item.requiredQty).text(item.requiredQty)
          )
        );

        // 3) LOT 입력
        const $lotCell = $('<td>');
        if (!lots.length) {
          if (lotRequired === 'N') {
            // ★ LOT 불필요: 자동 출고 입력 한 줄 생성 (값=필요수량, max=req)
            const req = Number(item.requiredQty) || 0;

            const $lotRow = $('<div>').addClass('form-row align-items-center lot-row mb-1');
            const $infoCol = $('<div>').addClass('col-md-5')
              .append($('<small>').addClass('text-muted').text('LOT 불필요'))
              .append('<br>')
              .append($('<small>').text('직접 출고'));
            const $qtyCol = $('<div>').addClass('col-md-4').append(
              $('<input>', { type:'number', min:0, step:1, value:req, max:req }) // ★ max=req
                .addClass('form-control form-control-sm lot-qty')
                .data({ material: item.materialId, lot: '', warehouse: defaultWh }) // ★ lot 공백
            );
            const $whCol = $('<div>').addClass('col-md-3')
              .append($('<span>').addClass('badge badge-secondary').text(defaultWh));

            $lotRow.append($infoCol, $qtyCol, $whCol);
            $lotCell.append($lotRow);
          } else {
            // 기존: LOT 필요인데 없음
            $lotCell.append($('<span>').addClass('text-danger').text('사용 가능한 LOT 없음'));
          }
        } else {
          // 기존 LOT 목록 렌더
          lots.sort((a,b)=> new Date(a.expirationDate) - new Date(b.expirationDate));
          lots.forEach(function(lot) {
            const warehouseCode = lot.warehouseCode || defaultWh;
            const availableQty  = lot.quantity != null ? lot.quantity : 0;

            const $lotRow = $('<div>').addClass('form-row align-items-center lot-row mb-1')
              .attr('data-lot', lot.lotNo);
            const $infoCol = $('<div>').addClass('col-md-5')
              .append($('<small>').addClass('text-muted').text(lot.lotNo || '-'))
              .append('<br>')
              .append($('<small>').text(`유통기한: ${fmtDate(lot.expirationDate)} / 재고: ${availableQty}`));
            const $qtyCol = $('<div>').addClass('col-md-4').append(
              $('<input>', { type:'number', min:0, max:availableQty, step:1, value:0 })
                .addClass('form-control form-control-sm lot-qty')
                .data({ material:item.materialId, lot:lot.lotNo, warehouse:warehouseCode })
            );
            const $whCol = $('<div>').addClass('col-md-3')
              .append($('<span>').addClass('badge badge-secondary').text(warehouseCode));

            $lotRow.append($infoCol, $qtyCol, $whCol);
            $lotCell.append($lotRow);
          });
        }
        $row.append($lotCell);

        // 4) 합계
        $row.append($('<td>').addClass('text-right align-middle')
                  .append($('<span>').addClass('sum').text('0')));

        $('#materialLotBody').append($row);
        updateRowSumAndValidate($row);
      });
  });

  // 모든 LOT 정보 로드 완료 후 처리
  $.when.apply($, promises).then(function() { 
    // 페이지 진입 시: 재고가 충분한 자재만 자동 배정
    autoAllocateAll(false);     
    window.validateAll(); 
  });
}

/* ---------- 행별 합계 계산 및 상태 업데이트 ---------- */
function updateRowSumAndValidate($row) {
  const required = +($row.find('.req').data('req') || 0);
  const lotRequired = ($row.data('lot-required') === 'N') ? 'N' : 'Y';

  let sum = 0;
  $row.find('.lot-qty').each(function(){ sum += +(this.value || 0); });
  $row.find('.sum').text(sum);

  $row.removeClass('table-success table-warning table-danger');

  if (lotRequired === 'N') {
    // LOT 없이도 OK → 기본적으로 필요수량만큼 자동값을 넣었으니 성공으로 표시
    if (sum >= required && required > 0) $row.addClass('table-success');
    else if (sum > 0) $row.addClass('table-warning');
    else $row.addClass('table-danger');
    return;
  }

  // LOT 필요한 자재는 기존 규칙
  if (sum === required) $row.addClass('table-success');
  else if (sum > 0 && sum !== required) $row.addClass('table-warning');
  else $row.addClass('table-danger');
}


/* ---------- 입력 제한 및 실시간 검증 ---------- */
$(document).on('wheel', '.lot-qty', function(e) { 
  e.preventDefault(); 
});

$(document).on('input change blur', '.lot-qty', function() {
  const $input = $(this);
  const $row = $input.closest('tr');

  // ★ max 없으면 Infinity로 처리 (이전: 0으로 깎여버림)
  const hasMax   = $input.is('[max]');
  const maxLotQty = hasMax ? Number($input.attr('max')) : Infinity;

  let inputValue = Number($input.val()) || 0;
  
  // 음수 방지
  if (inputValue < 0) inputValue = 0;
  // LOT 재고량 초과 방지
  if (inputValue > maxLotQty) inputValue = maxLotQty;

  const required = Number($row.find('.req').data('req')) || 0;
  const othersSum = $row.find('.lot-qty').not($input).toArray()
    .reduce((sum, el) => sum + (Number(el.value) || 0), 0);
  const remaining = Math.max(required - othersSum, 0);
  
  // 필요수량 초과 방지
  if (inputValue > remaining) inputValue = remaining;

  $input.val(inputValue);
  updateRowSumAndValidate($row);
  window.validateAll();
});

/* ---------- 폼 제출 시 hidden 필드 생성 ---------- */
$(document).off('submit', '#outboundForm').on('submit', '#outboundForm', function(e) {
  const $form = $(this);
  
  // 기존 hidden 필드 제거
  $form.find('input[name=materialIdList],input[name=reqQtyList],input[name=lotMaterialIdList],input[name=lotNoList],input[name=qtyList],input[name=lotWarehouseList]').remove();

  // 자재별 정보 추가
  $('#materialLotBody tr').each(function() {
    const materialId = $(this).data('material');
    const required = +($(this).find('.req').data('req') || 0);
    
    $form.append($('<input>', { type:'hidden', name:'materialIdList', value: materialId }));
    $form.append($('<input>', { type:'hidden', name:'reqQtyList',    value: required   }));
  });

  // LOT별 출고 정보 추가
  $('.lot-qty').each(function() {
    const quantity = +(this.value || 0);
    if (quantity > 0) {
      const $element = $(this);
      $form.append($('<input>', { type:'hidden', name:'lotMaterialIdList', value: $element.data('material') }));
      $form.append($('<input>', { type:'hidden', name:'lotNoList',         value: $element.data('lot')       }));
      $form.append($('<input>', { type:'hidden', name:'qtyList',           value: quantity                    }));
      $form.append($('<input>', { type:'hidden', name:'lotWarehouseList',  value: ($element.data('warehouse') || '') }));
    }
  });

  return true;
});

/* ---------- 부족분 발주 초안 생성 ---------- */
$('#btnCreateDraft').off('click.draft').on('click.draft', function (e) {
  e.preventDefault();

  const workOrderId = $('#workOrderNo').val();
  if (!workOrderId) { 
    alert('작업지시서를 먼저 선택하세요.'); 
    return; 
  }

  const shortages = collectShortages();
  if (!shortages.length) { 
    alert('부족분이 없습니다.'); 
    return; 
  }

  const payload = {
    workOrderId: workOrderId,
    items: shortages.map(shortage => ({ 
      materialId: shortage.materialId, 
      lackQty: shortage.lackQty 
    }))
  };

  // 로딩 상태 표시
  const $btn = $(this);
  $btn.prop('disabled', true).text('생성 중...');

  $.ajax({
    url: ctx + '/material/order/draft',
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(payload)
  })
  .done(function(response) {
    if (response.orderId) {
      alert('발주 초안이 생성되었습니다.\n발주번호: ' + response.orderId);
      
      if (response.unmappedMaterials && response.unmappedMaterials.length > 0) {
        alert('일부 자재는 거래처 매핑이 없어 발주에서 제외되었습니다:\n' + 
              response.unmappedMaterials.join(', '));
      }
      
      // 발주 등록 페이지로 이동할지 확인
      if (confirm('생성된 발주 초안을 확인하시겠습니까?')) {
        window.open(ctx + '/material/order/list', '_blank');
      }
    } else {
      alert('발주 초안 생성에 실패했습니다.');
    }
  })
  .fail(function(xhr) {
    console.error('발주 초안 생성 실패:', xhr.responseText);
    alert('발주 초안 생성에 실패했습니다.\n' + 
          (xhr.responseJSON && xhr.responseJSON.message 
            ? xhr.responseJSON.message 
            : '서버 오류'));
  })
  .always(function() {
    // 버튼 상태 복원
    $btn.prop('disabled', false).text('부족분 발주');
  });
});

/* ---------- 출고 처리 ---------- */
window.processOutbound = function(outboundId) {
  if (!confirm('출고 처리하시겠습니까?')) return;
  
  $.post(ctx + '/material/outbound/process', { outboundId: outboundId })
    .done(function(response) {
      if (response.success) {
        alert(response.message);
        location.reload(); // 목록 새로고침
      } else {
        alert(response.message);
      }
    })
    .fail(function() {
      alert('출고 처리에 실패했습니다.');
    });
};

/* ---------- 출고 상세 조회 ---------- */
window.loadOutboundDetail = function(outboundId) {
  $.get(ctx + '/material/outbound/detail', { outboundId: outboundId })
    .done(function(data) {
      let html = '<div class="row">';
      html += '<div class="col-12"><h6>출고 정보</h6>';
      html += '<table class="table table-sm">';
      html += '<tr><th>출고번호</th><td>' + data.outboundId + '</td>';
      html += '<th>작업지시</th><td>' + data.workOrderNo + '</td></tr>';
      html += '<tr><th>출고일자</th><td>' + (data.outboundDate || '-') + '</td>';
      html += '<th>담당자</th><td>' + data.handledBy + '</td></tr>';
      html += '</table></div>';
      
      if (data.items && data.items.length > 0) {
        html += '<div class="col-12 mt-3"><h6>출고 항목</h6>';
        html += '<table class="table table-sm table-bordered">';
        html += '<thead><tr><th>자재</th><th>LOT</th><th>출고수량</th><th>창고</th></tr></thead><tbody>';
        
        data.items.forEach(function(item) {
          html += '<tr>';
          html += '<td>' + item.materialName + '<br><small class="text-muted">' + item.materialId + '</small></td>';
          html += '<td>' + item.lotNo + '</td>';
          html += '<td class="text-right">' + item.quantity + '</td>';
          html += '<td>' + item.warehouseCode + '</td>';
          html += '</tr>';
        });
        
        html += '</tbody></table></div>';
      }
      html += '</div>';
      
      $('#outboundDetailBody').html(html);
      $('#outboundDetailModal').modal('show');
    })
    .fail(function() {
      alert('상세 정보를 불러올 수 없습니다.');
    });
};

/* ---------- 자동 배정 기능 ---------- */
// 행 단위 자동 배정 (FEFO 순으로 배정)
function autoAllocateForRow($row, onlyIfEnough) {
  const required = +($row.find('.req').data('req') || 0);
  const $lotInputs = $row.find('.lot-qty');

  // 총 가능 수량 계산
  const totalAvailable = $lotInputs.toArray().reduce((sum, element) => {
    const $el = $(element);
    const hasMax = $el.is('[max]');
    const maxQty = hasMax ? Number($el.attr('max')) : required; // ★ max 없으면 required로 간주
    return sum + maxQty;
  }, 0);

  // 충분하지 않으면 배정하지 않음 (옵션에 따라)
  if (onlyIfEnough && totalAvailable < required) {
    return false;
  }

  // FEFO 순으로 배정
  let allocatedSum = 0;
  $lotInputs.each(function () {
    const $input = $(this);
    const hasMax = $input.is('[max]');
    const maxQty = hasMax ? Number($input.attr('max')) : required; // ★
    
    if (allocatedSum >= required) { 
      $input.val(0); 
      return; 
    }
    
    const needed = required - allocatedSum;
    const allocateQty = Math.min(maxQty, needed);
    $input.val(allocateQty);
    allocatedSum += allocateQty;
  });

  updateRowSumAndValidate($row);
  return allocatedSum >= required;
}

// 전체 자재 자동 배정
function autoAllocateAll(onlyIfEnough) {
  $('#materialLotBody tr').each(function () {
    autoAllocateForRow($(this), onlyIfEnough);
  });
  window.validateAll();
}
