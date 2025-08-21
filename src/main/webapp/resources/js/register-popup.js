// resources/js/register-popup.js
(function () {
  'use strict';

  $(document).ready(function () {
    console.log('register-popup.js loaded');

    // ===============================
    // 1) 병합 수주 JSON 안전 파싱
    // ===============================
    let mergedOrders = [];
    try {
      // 1) select-order.js에서 저장해 둔 상세 목록 [{clOrderId, orderQty, clientName?, dueDate?}]
      const stored = sessionStorage.getItem('WO_MERGED_ORDERS');
      if (stored) {
        mergedOrders = JSON.parse(stored);
      } else {
        // 2) 폴백: 서버가 내려준 JSON (["CL-001","CL-002"] or [{clOrderId:"...", orderQty:...}])
        const el = document.getElementById('clOrderIdsJson');
        if (el && el.textContent) {
          const parsed = JSON.parse(el.textContent.trim());
          // 문자열 배열이면 객체 형태로 보정
          mergedOrders = parsed.map(function (x) {
            return (typeof x === 'string') ? { clOrderId: x } : x;
          });
        }
      }
    } catch (e) {
      console.warn('mergedOrders parse error:', e);
      mergedOrders = [];
    }
    console.log('병합된 수주:', mergedOrders);

    // ===============================
    // 2) 병합 수주 섹션 표시/렌더링
    // ===============================
    if (Array.isArray(mergedOrders) && mergedOrders.length > 1) {
      document.getElementById('mergedOrdersSection').style.display = 'block';
      document.getElementById('mergedCount').innerText = mergedOrders.length;

      const tbody = document.getElementById('mergedOrdersTableBody');
      tbody.innerHTML = '';
      mergedOrders.forEach(function (row) {
        const tr = document.createElement('tr');
        tr.innerHTML =
          '<td>' + (row.clOrderId || '-') + '</td>' +
          '<td>' + (row.clientName || '-') + '</td>' +
          '<td class="text-right">' + (row.orderQty != null ? row.orderQty : '-') + '</td>';
        tbody.appendChild(tr);
      });
    }

    // 접기/펼치기 아이콘 토글
    $('#mergedOrdersList')
      .on('show.bs.collapse', function () {
        $('a[href="#mergedOrdersList"] i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
      })
      .on('hide.bs.collapse', function () {
        $('a[href="#mergedOrdersList"] i').removeClass('fa-chevron-up').addClass('fa-chevron-down');
      });

    // ===============================
    // 3) BOM 로딩
    // ===============================
    const productId = document.getElementById('productId').value;
    const orderQty = parseInt(document.getElementById('orderQty').value || '0', 10);
    console.log('BOM 로딩 시작 - 제품ID:', productId, '수량:', orderQty);

    function loadBom(pid, qty) {
      if (!pid || qty <= 0) {
        console.warn('BOM 로딩 중단 - 잘못된 파라미터');
        return;
      }
      $.ajax({
        url: (window.CONTEXT_PATH || '') + '/workorder/getBomByProduct',
        type: 'GET',
        data: { productId: pid, orderQty: qty },
        beforeSend: function () {
          console.log('BOM 요청 전송...');
        },
        success: function (bomList) {
          console.log('BOM 응답 받음:', bomList);
          const tbody = $('#bomTableBody');
          tbody.empty();

          if (!bomList || !bomList.length) {
            tbody.html('<tr><td colspan="6" class="text-center text-muted">BOM 정보 없음</td></tr>');
            window.materialList = [];
            return;
          }

          const packs = qty / 10; // 10팩 기준
          const hiddenMaterials = [];

          bomList.forEach(function (item) {
            const per10 = Number(item.qty) || 0;
            const total = per10 * packs;

            hiddenMaterials.push({
              materialId: item.materialId,
              requiredQty: total
            });

            const row =
              '<tr>' +
              '<td>' + (item.materialId || '') + '</td>' +
              '<td>' + (item.materialName || '') + '</td>' +
              '<td>' + (item.materialType || '') + '</td>' +
              '<td class="text-center">' + per10.toFixed(1) + '</td>' +
              '<td class="text-center font-weight-bold">' + total.toFixed(1) + '</td>' +
              '<td>' + (item.unit || '') + '</td>' +
              '</tr>';
            tbody.append(row);
          });

          window.materialList = hiddenMaterials;
          console.log('자재 리스트 설정 완료:', window.materialList);
        },
        error: function (xhr, status, error) {
          console.error('BOM 로딩 실패:', { xhr: xhr, status: status, error: error });
          $('#bomTableBody').html('<tr><td colspan="6" class="text-center text-danger">로딩 실패: ' + error + '</td></tr>');
          window.materialList = [];
        }
      });
    }

    if (productId && orderQty > 0) {
      loadBom(productId, orderQty);
    } else {
      console.warn('BOM 로딩 불가 - productId:', productId, 'orderQty:', orderQty);
    }

    // ===============================
    // 4) CSRF 토큰 (필요 시)
    // ===============================
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');
    var csrfToken = $('meta[name="_csrf"]').attr('content');

    // ===============================
    // 5) 제출 처리
    // ===============================
    let isSubmitting = false;

    $('#workOrderForm').on('submit', function (e) {
      e.preventDefault();
      console.log('폼 제출 시작');

      if (isSubmitting) {
        console.log('이미 제출 중입니다.');
        return false;
      }

      const lineIdVal = $('#lineId').val();
      if (!lineIdVal) {
        alert('생산라인을 선택하세요.');
        return false;
      }

      isSubmitting = true;
      const $btn = $('#submitBtn');
      const originalText = $btn.text();

      $btn.prop('disabled', true)
        .text('등록 중...')
        .removeClass('btn-primary')
        .addClass('btn-secondary');

      const payload = {
        productId: productId,
        orderQty: orderQty,
        dueDate: $('input[name="dueDate"]').val(),
        lineId: lineIdVal,
        priority: $('select[name="priority"]').val(),
        remarks: $('textarea[name="remarks"]').val(),
        status: 'WAITING',
        materialList: window.materialList || [],
        mergedOrders: mergedOrders // [{clOrderId, orderQty, ...}]
      };

      console.log('전송 데이터:', payload);

      $.ajax({
        url: (window.CONTEXT_PATH || '') + '/workorder/register',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        timeout: 30000,
        xhrFields: { withCredentials: true },
        crossDomain: false,
        beforeSend: function (xhr) {
          if (csrfHeader && csrfToken) {
            xhr.setRequestHeader(csrfHeader, csrfToken);
          }
          console.log('요청 전송 중...');
        },
        success: function (res) {
          console.log('응답 받음:', res);

          if (res && res.success) {
            // 병합 수주 세션스토리지 정리
            try { sessionStorage.removeItem('WO_MERGED_ORDERS'); } catch (e) {
              console.warn('세션스토리지 정리 실패:', e);
            }

            // 부모 창 새로고침
            try {
              if (window.opener && !window.opener.closed) {
                window.opener.postMessage({ type: 'WORK_ORDER_CREATED' }, window.location.origin);
                window.opener.location.reload();
              }
            } catch (e) {
              console.warn('부모 창 새로고침 실패:', e);
            }

            try {
              localStorage.setItem('WORK_ORDER_REFRESH', String(Date.now()));
            } catch (e) {
              console.warn('localStorage 설정 실패:', e);
            }

            alert('작업지시 등록이 완료되었습니다.');
            window.close();
          } else {
            throw new Error((res && res.message) ? res.message : '알 수 없는 오류');
          }
        },
        error: function (xhr, status, error) {
          console.error('등록 오류:', { xhr: xhr, status: status, error: error });
          var errorMessage = '서버 오류가 발생했습니다.';

          if (status === 'timeout') {
            errorMessage = '요청 시간이 초과되었습니다. 다시 시도해주세요.';
          } else if (xhr.responseJSON && xhr.responseJSON.message) {
            errorMessage = xhr.responseJSON.message;
          } else if (xhr.responseText) {
            errorMessage = '서버 응답 오류: ' + xhr.status;
          }

          alert(errorMessage);
        },
        complete: function () {
          console.log('요청 완료');
          isSubmitting = false;
          $btn.prop('disabled', false)
            .text(originalText)
            .removeClass('btn-secondary')
            .addClass('btn-primary');
        }
      });
    });

    // 페이지 이탈 시 리셋
    $(window).on('beforeunload', function () {
      isSubmitting = false;
    });
  });
})();
