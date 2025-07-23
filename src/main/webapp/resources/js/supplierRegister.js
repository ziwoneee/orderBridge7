let isBizNoChecked = false;

document.addEventListener("DOMContentLoaded", () => {

  // 서버단 전달 에러 메시지 alert 출력
  const errorInput = document.getElementById("errorMsg");
  const errorMsg = errorInput ? errorInput.value : null;
  if (errorMsg) alert(errorMsg)
	
  const form = document.querySelector("form");

  // 👉 등록 폼 유효성 검사
  form.addEventListener("submit", function(e) {
    const supplierName = form.querySelector("input[name='supplierName']");
    const businessNumber = form.querySelector("input[name='businessNumber']");
    const phone = form.querySelector("input[name='phone']");
    const email = form.querySelector("input[name='email']");
    const contactName = form.querySelector("input[name='contactName']");
    const contactPhone = form.querySelector("input[name='contactPhone']");
    const contactEmail = form.querySelector("input[name='contactEmail']");
    const accountNumber = form.querySelector("input[name='accountNumber']");

    const bizNoPattern = /^\d{3}-\d{2}-\d{5}$/;
    const phonePattern = /^01[0-9]-\d{3,4}-\d{4}$/;
    const emailPattern = /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/;
    const accountPattern = /^\d+$/;

    if (supplierName.value.trim() === "") {
      alert("거래처명을 입력하세요.");
      supplierName.focus();
      e.preventDefault();
      return;
    }

    if (!bizNoPattern.test(businessNumber.value.trim())) {
      alert("사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)");
      businessNumber.focus();
      e.preventDefault();
      return;
    }

    if (!isBizNoChecked) {
      alert("사업자등록번호 중복확인을 해주세요.");
      businessNumber.focus();
      e.preventDefault();
      return;
    }

    if (phone.value && !phonePattern.test(phone.value.trim())) {
      alert("전화번호 형식이 올바르지 않습니다.");
      phone.focus();
      e.preventDefault();
      return;
    }

    if (email.value && !emailPattern.test(email.value.trim())) {
      alert("이메일 형식이 올바르지 않습니다.");
      email.focus();
      e.preventDefault();
      return;
    }

    if (contactName.value.trim() === "") {
      alert("담당자 이름을 입력하세요.");
      contactName.focus();
      e.preventDefault();
      return;
    }

    if (contactPhone.value && !phonePattern.test(contactPhone.value.trim())) {
      alert("담당자 연락처 형식이 올바르지 않습니다.");
      contactPhone.focus();
      e.preventDefault();
      return;
    }

    if (contactEmail.value && !emailPattern.test(contactEmail.value.trim())) {
      alert("담당자 이메일 형식이 올바르지 않습니다.");
      contactEmail.focus();
      e.preventDefault();
      return;
    }

    if (accountNumber.value && !accountPattern.test(accountNumber.value.trim())) {
      alert("계좌번호는 숫자만 입력 가능합니다.");
      accountNumber.focus();
      e.preventDefault();
      return;
    }
  });

  // 👉 사업자등록번호 중복 확인
  document.getElementById("checkBizBtn").addEventListener("click", () => {
    const bizNoInput = document.getElementById("businessNumber");
    const bizNo = bizNoInput.value.trim();
    const msg = document.getElementById("bizCheckMsg");

    const pattern = /^\d{3}-\d{2}-\d{5}$/;
    if (!pattern.test(bizNo)) {
      msg.innerText = "사업자등록번호 형식이 올바르지 않습니다.";
      msg.classList.remove("text-success");
      msg.classList.add("text-danger");
      bizNoInput.focus();
      return;
    }

    fetch("/supplier/checkBizNo?businessNumber=" + encodeURIComponent(bizNo))
      .then(res => res.json())
      .then(data => {
        if (data.exists) {
          msg.innerText = "이미 등록된 사업자등록번호입니다.";
          msg.classList.remove("text-success");
          msg.classList.add("text-danger");
          isBizNoChecked = false;
        } else {
          msg.innerText = "사용 가능한 사업자등록번호입니다.";
          msg.classList.remove("text-danger");
          msg.classList.add("text-success");
          isBizNoChecked = true;
        }
      })
      .catch(() => {
        msg.innerText = "확인 중 오류가 발생했습니다.";
        msg.classList.remove("text-success");
        msg.classList.add("text-danger");
        isBizNoChecked = false;
      });
  });

  // 👉 주소검색 버튼 클릭
  document.getElementById("findAddressBtn").addEventListener("click", () => {
    new daum.Postcode({
      oncomplete: function(data) {
        document.getElementById("postcode").value = data.zonecode;
        document.getElementById("address").value = data.roadAddress || data.jibunAddress;
      }
    }).open();
  });

  // 👉 사업자등록번호 자동 하이픈
  document.querySelector("input[name='businessNumber']").addEventListener("input", function (e) {
    let value = e.target.value.replace(/\D/g, "");
    if (value.length > 10) value = value.substring(0, 10);

    let formatted = "";
    if (value.length <= 3) {
      formatted = value;
    } else if (value.length <= 5) {
      formatted = value.slice(0, 3) + "-" + value.slice(3);
    } else {
      formatted = value.slice(0, 3) + "-" + value.slice(3, 5) + "-" + value.slice(5);
    }

    e.target.value = formatted;
  });

  // 👉 전화번호 자동 하이픈 (일반/담당자 둘 다)
  document.querySelectorAll("input[name='phone'], input[name='contactPhone']").forEach(input => {
    input.addEventListener("input", function (e) {
      let value = e.target.value.replace(/\D/g, "");
      if (value.length > 11) value = value.substring(0, 11);

      let formatted = "";
      if (value.startsWith("02")) {
        if (value.length <= 2) {
          formatted = value;
        } else if (value.length <= 5) {
          formatted = value.slice(0, 2) + "-" + value.slice(2);
        } else if (value.length <= 9) {
          formatted = value.slice(0, 2) + "-" + value.slice(2, 5) + "-" + value.slice(5);
        } else {
          formatted = value.slice(0, 2) + "-" + value.slice(2, 6) + "-" + value.slice(6);
        }
      } else {
        if (value.length <= 3) {
          formatted = value;
        } else if (value.length <= 7) {
          formatted = value.slice(0, 3) + "-" + value.slice(3);
        } else {
          formatted = value.slice(0, 3) + "-" + value.slice(3, 7) + "-" + value.slice(7);
        }
      }

      e.target.value = formatted;
    });
  });
});
