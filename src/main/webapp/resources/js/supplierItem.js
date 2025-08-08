$(document).ready(function () {

  // ✅ [1] 등록 버튼 → 모달 열기
  $("#btnAddItem").on("click", function () {
    $("#itemForm")[0].reset(); // 폼 초기화
    $("#itemModal .modal-title").text("공급 품목 등록");
    $("#itemModal").modal("show");
  });
  
  
//모달 열릴 때마다 select2 적용
//select2는 모달 open 이후에 초기화해야 깨지지 않음!
  $('#itemModal').on('shown.bs.modal', function () {
	  const $select = $(this).find('.select2');

	  // 항상 먼저 destroy
	  if ($select.hasClass('select2-hidden-accessible')) {
	    $select.select2('destroy');
	  }

	  // 그리고 새로 초기화
	  $select.select2({
	    theme: 'bootstrap',
	    placeholder: '자재를 선택하세요',
	    width: '100%',
	    allowClear: true,
	    language: {
	        noResults: function () {
	          return "자재 정보가 없습니다"; // ← 원하는 문구로 바꾸면 됨
	        }
	      }
	  });
	});


  // 모달 닫힐 때 select2 초기화 해제
  $('#registerModal').on('hidden.bs.modal', function () {
	  $('#materialId').val(null).trigger('change.select2'); // 선택값 초기화
	});


  // ✅ [2] 등록 폼 제출 시 → Ajax 등록 처리
  $("#itemForm").on("submit", function (e) {
    e.preventDefault(); // 폼 기본 동작 막기

    const formData = $(this).serialize();

    $.ajax({
      url: "/supplierItem/register",
      method: "POST",
      data: formData,
      success: function () {
        alert("등록 성공");
        $("#itemModal").modal("hide");
        loadItemList(); // 목록 새로고침
      },
      error: function (xhr) {
        alert("등록 실패: " + xhr.responseText);
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

        // 👉 한 행씩 생성
        data.forEach(function (item) {
          const tr = $("<tr>");
          tr.append($("<td>").text(item.materialName ? item.materialName : "-"));
          tr.append($("<td>").text(item.materialType ? item.materialType : "-"));
          tr.append($("<td>").text(item.unitPrice ? item.unitPrice : "-"));
          tr.append($("<td>").text(item.unit ? item.unit : "-"));
          tr.append($("<td>").text(item.supplyAvailable === "Y" ? "가능" : "불가"));
          tr.append($("<td>").text(item.note ? item.note : "-"));


          // ✅ ❌ 여기서 IDE가 싫어하던 부분 → 완전 안전한 방식으로 바꿈
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

  // ✅ [4] supplierId 추출 함수 (JSP에서 받아옴)
  function supplierIdFromJsp() {
    return $("input[name='supplierId']").val();
  }

  // ✅ [5] 페이지 진입 시 목록 자동 로딩
  loadItemList();
});

//자재 선택 시 단가, 단위 자동 입력
$(document).on('change', '#materialId', function () {
  const selected = $(this).find('option:selected');
  const unitPrice = selected.data('unitprice');
  const unit = selected.data('unit');

  $('#unitPrice').val(unitPrice || '');
  $('#unit').val(unit || '');
});

// 모달 닫힐 떄 완전 destroy
$('#itemModal').on('hidden.bs.modal', function () {
	  const $select = $(this).find('.select2');
	  $select.val(null).trigger('change.select2');
	  $select.select2('destroy');
	});
