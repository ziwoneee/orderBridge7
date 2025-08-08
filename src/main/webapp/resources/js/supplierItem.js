$(document).ready(function () {

	// ✅ [1] 등록 버튼 → 모달 열기 - 더 깔끔한 버전
	$("#btnAddItem").on("click", function () {
	  // ✅ 먼저 폼 리셋하고 모드 설정
	  $("#itemForm")[0].reset();
	  $("#itemForm").attr("data-mode", "create");
	  $("#itemModal .modal-title").text("공급 품목 등록");
	  
	  // ✅ select2가 있다면 먼저 파괴
	  const $select = $('#materialId');
	  if ($select.hasClass('select2-hidden-accessible')) {
	    $select.select2('destroy');
	  }

	  // ✅ select2 즉시 초기화 (모달 열기 전에!)
	  $select.select2({
	    theme: 'bootstrap',
	    placeholder: '자재를 선택하세요',
	    width: '100%',
	    allowClear: true,
	    language: {
	      noResults: function () {
	        return "자재 정보가 없습니다";
	      }
	    }
	  });

	  $("#itemModal").modal("show");
	});

  
// ✅ 모달 shown 이벤트는 이제 필요없음 (미리 초기화하므로)
// 하지만 수정 모드에서는 값을 다시 설정해야 할 수 있으니 유지
$('#itemModal').on('shown.bs.modal', function () {
	  // ✅ 수정 모드인 경우에만 값 재설정
	  const mode = $('#itemForm').attr('data-mode');
	  if (mode === 'edit') {
	    const materialId = $('#materialId').val();
	    if (materialId) {
	      $('#materialId').val(materialId).trigger('change');
	    }
	  }
});



  // ✅ [2] 등록 폼 제출 시 → Ajax 등록 처리
  $("#itemForm").on("submit", function (e) {
    e.preventDefault();

    const mode = $(this).attr('data-mode');
    const formData = $(this).serialize();

    const url = (mode === 'edit') 
      ? "/supplierItem/update" 
      : "/supplierItem/register";

    $.ajax({
      url: url,
      method: "POST",
      data: formData,
      success: function () {
        alert(mode === 'edit' ? "자재 정보 수정 완료" : "자재 정보 등록 완료");
        $("#itemModal").modal("hide");
        loadItemList();
      },
      error: function (xhr) {
        alert("처리 실패: " + xhr.responseText);
      }
    });
  });

  // ✅ [3] 공급 품목 목록 로딩 함수
  function loadItemList() {
    $.ajax({
      url: "/supplierItem/list?supplierId=" + supplierIdFromJsp(),
      method: "GET",
      success: function (data) {
        const tbody = $("#itemTable tbody");
        tbody.empty();

        if (!data || data.length === 0) {
          tbody.append(`<tr><td colspan="7" class="text-center text-muted">등록된 공급 품목이 없습니다.</td></tr>`);
          return;
        }

        data.forEach(function (item) {
        	const tr = $("<tr>")
        	.attr("data-item-id", item.id)
            .attr("data-material-id", item.materialId);
        	
          tr.append($("<td>").text(item.materialName ? item.materialName : "-"));
          tr.append($("<td>").text(item.materialType ? item.materialType : "-"));
          tr.append($("<td>").text(item.unitPrice ? item.unitPrice : "-"));
          tr.append($("<td>").text(item.unit ? item.unit : "-"));
          tr.append($("<td>").text(item.supplyAvailable === "Y" ? "가능" : "불가"));
          tr.append($("<td>").text(item.note ? item.note : "-"));

          const td = $("<td>");
          const editBtn = $("<button>").addClass("btn btn-sm btn-outline-warning btn-edit").text("수정");
          const deleteBtn = $("<button>").addClass("btn btn-sm btn-outline-danger btn-delete").text("삭제");
          td.append(editBtn).append(" ").append(deleteBtn);
          tr.append(td);

          tbody.append(tr);
        });
      },
      error: function (xhr) {
        console.error("목록 조회 실패:", xhr);
      }
    });
  }

  // ✅ [4] supplierId 추출 함수
  function supplierIdFromJsp() {
    return $("input[name='supplierId']").val();
  }

  // ✅ [5] 페이지 진입 시 목록 자동 로딩
  loadItemList();
});

// 자재 선택 시 단가, 단위 자동 입력
$(document).on('change', '#materialId', function () {
  const selected = $(this).find('option:selected');
  const unitPrice = selected.data('unitprice');
  const unit = selected.data('unit');

  $('#unitPrice').val(unitPrice || '');
  $('#unit').val(unit || '');
});

// ✅ 모달 닫힐 때 정리 - 간소화 버전
$('#itemModal').on('hidden.bs.modal', function () {
	  const $select = $('#materialId');
	  
	  // ✅ select2만 정리하면 끝!
	  if ($select.hasClass('select2-hidden-accessible')) {
	    $select.val(null).trigger('change.select2');
	    $select.select2('destroy');
	  }
});

// ---------------------------------------------------------------- //
// 수정 관련

// ✅ 수정 버튼 클릭 이벤트 - 최종 깔끔한 버전
$(document).on('click', '.btn-edit', function () {
	  const $tr = $(this).closest('tr');

	  // 각 값 추출
	  const itemId = $tr.data('item-id');
	  const materialId = $tr.data('material-id');
	  const unitPrice = $tr.find('td:eq(2)').text().trim();
	  const unit = $tr.find('td:eq(3)').text().trim();
	  const supplyAvailable = $tr.find('td:eq(4)').text().trim() === '가능' ? 'Y' : 'N';
	  const note = $tr.find('td:eq(5)').text().trim();

	  // ✅ select2 미리 파괴
	  const $select = $('#materialId');
	  if ($select.hasClass('select2-hidden-accessible')) {
	    $select.select2('destroy');
	  }
	  
	  // 폼 초기화 및 값 설정
	  $("#itemForm")[0].reset();
	  $("#itemModal .modal-title").text("공급 품목 수정");
	  $('#itemForm').attr('data-mode', 'edit');
	  $('#itemId').val(itemId);
	  
	  // ✅ 값 설정
	  $('#materialId').val(materialId);
	  $('#unitPrice').val(unitPrice);
	  $('#unit').val(unit);
	  $('select[name="supplyAvailable"]').val(supplyAvailable);
	  $('textarea[name="note"]').val(note === '-' ? '' : note);

	  // ✅ select2 초기화 (모달 열기 전에!)
	  $select.select2({
	    theme: 'bootstrap',
	    placeholder: '자재를 선택하세요',
	    width: '100%',
	    allowClear: true,
	    language: {
	      noResults: function () {
	        return "자재 정보가 없습니다";
	      }
	    }
	  });
	  
	  // ✅ 값 다시 설정 (select2 초기화 후)
	  $select.val(materialId).trigger('change');

	  // 모달 열기
	  $("#itemModal").modal("show");
});