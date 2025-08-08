$(document).ready(function () {
	
	// 공급 품목 중복 검사
	$("#btnAddItemSubmit").on("click", function (e) {
		  e.preventDefault();

		  const supplierId = $("#supplierId").val();
		  const materialId = $("#materialId").val();

		  const params = new URLSearchParams({
		    supplierId: supplierId,
		    materialId: materialId
		  }).toString();

		  $.ajax({
		    type: "GET",
		    url: "/supplierItem/check?" + params,
		    success: function (isDuplicate) {
		    	console.log("✅ 중복 검사 응답:", isDuplicate, typeof isDuplicate);
		      if (isDuplicate === true || isDuplicate === 'true') {
		        alert("⚠ 이미 등록된 자재입니다");
		        return;
		      }
		      $("#itemForm").submit(); // ✅ 중복 아닐 때만 실행돼야 함!
		    },
		    error: function () {
		      alert("중복 확인 중 오류 발생!");
		    }
		  });
		});


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

	  // ✅ 등록 모드에서는 select 활성화
	  $select.prop('disabled', false);

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
	  
	  const supplierId = $(this).data("supplier-id");
	  console.log("📌 등록 대상 supplierId:", supplierId);
	  $("#supplierId").val(supplierId);
	  
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
    console.log("💬 폼 제출 모드 확인:", mode);
    const formData = $(this).serialize();
    
    console.log("전송 데이터:", formData);


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
        
        location.reload();
      },
      error: function (xhr) {
        alert("처리 실패: " + xhr.responseText);
      }
    });
  });

  // ✅ [4] supplierId 추출 함수
  function supplierIdFromJsp() {
    return $("input[name='supplierId']").val();
  }

});

// ✅ 자재 선택 시 단가, 단위 자동 입력 - 수정 모드 보호 기능 추가
$(document).on('change', '#materialId', function () {
	  const mode = $('#itemForm').attr('data-mode');
	  const $select = $(this);

	  // ✅ 수정 모드이고 아직 수동 변경이 없었다면 기본값 적용 안함
	  if (mode === 'edit' && !$select.data('manual-change')) {
	    console.log('수정 모드 - 기본값 적용 안함 (DB값 유지)');
	    return; // 여기서 종료! 기본값으로 덮어쓰지 않음
	  }

	  // ✅ 등록 모드이거나, 수정 모드에서 사용자가 수동으로 변경한 경우만 기본값 적용
	  const selected = $(this).find('option:selected');
	  const unitPrice = selected.data('unitprice');
	  const unit = selected.data('unit');

	  $('#unitPrice').val(unitPrice || '');
	  $('#unit').val(unit || '');
	  
	  console.log('기본값 적용:', {mode, unitPrice, unit});
});

// ✅ select2에서 실제 선택이 발생했을 때만 manual-change 플래그 설정
$(document).on('select2:select', '#materialId', function () {
  const mode = $('#itemForm').attr('data-mode');
  if (mode === 'edit') {
    $(this).data('manual-change', true);
    console.log('사용자가 수동으로 자재 변경함 - 이제 기본값 적용됨');
  }
});

// ✅ 모달 닫힐 때 정리 - 간소화 버전
$('#itemModal').on('hidden.bs.modal', function () {
	  const $select = $('#materialId');
	  
	  // ✅ disabled 상태 해제
	  $select.prop('disabled', false);
	  
	  // ✅ select2만 정리하면 끝!
	  if ($select.hasClass('select2-hidden-accessible')) {
	    $select.val(null).trigger('change.select2');
	    $select.select2('destroy');
	  }
	  
	  // ✅ 플래그도 초기화
	  $select.removeData('manual-change');
});

// ---------------------------------------------------------------- //
// 수정 관련

// ✅ 수정 버튼 클릭 시 → Ajax로 DB에서 데이터 가져오기 (개선된 버전)
$(document).on("click", ".btn-edit", function () {
  const itemId = $(this).closest('tr').data("item-id");

  $.ajax({
    url: "/supplierItem/get",
    method: "GET",
    data: { id: itemId },
    success: function (item) {
      // 모드 설정
      $("#itemForm")[0].reset();
      $("#itemForm").attr("data-mode", "edit");
      $("#itemModal .modal-title").text("공급 품목 수정");

      // select2 초기화
      const $select = $('#materialId');
      if ($select.hasClass('select2-hidden-accessible')) {
        $select.select2('destroy');
      }
      
      // ✅ 수정 모드 플래그 미리 설정 (중요!)
      $select.data('manual-change', false);
      
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
      
      // ✅ 먼저 단가/단위 값을 설정 (change 이벤트 발생 전에!)
      $("#itemId").val(item.id);
      $("#unitPrice").val(item.unitPrice);
      $("#unit").val(item.unit);
      
      // ✅ 비고는 name으로 선택 (textarea에 id가 없을 수 있으니)
      $("textarea[name='note']").val(item.note || '');
      
      $("select[name='supplyAvailable']").val(item.supplyAvailable);
      
      // ✅ 그 다음에 select 값 설정 (이때 change 이벤트가 발생하지만 덮어쓰기 안됨)
      $select.val(item.materialId).trigger('change');
      
      // ✅ 수정 모드에서는 자재 선택을 비활성화
      $select.prop('disabled', true);
      
      console.log('수정 모드 데이터 로드 완료:', {
        materialId: item.materialId,
        unitPrice: item.unitPrice,
        unit: item.unit
      });

      $("#itemModal").modal("show");
    },
    error: function () {
      alert("공급 품목 정보를 불러오지 못했습니다.");
    }
  });
});