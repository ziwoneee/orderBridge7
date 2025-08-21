/* client-form.js
 * - 주소 검색(카카오/다음 우편번호)
 * - 사업자등록번호 자동 하이픈 & 검증
 * - 사업자등록번호 중복확인(Ajax) + 폼 제출 전 강제체크
 * - 한국 전화번호(휴대폰/일반/070/080/050X/대표번호) 자동 하이픈 & 검증
 * - 자릿수 하드 제한 (유형별 max digit)
 * - DOM 로드 시 대상 input 바인딩
 */
(function (window, document) {
  'use strict';

  const DEFAULT_IDS = {
    postcode: 'postcode',
    address: 'address',
    addressDetail: 'addressDetail',
    businessNumber: 'businessNumber',
    clientTel: 'clientTel',
    managerTel: 'managerTel',
    faxNumber: 'faxNumber',
    // ▼ 중복확인 추가
    checkBizBtn: 'checkBizBtn',
    bizCheckMsg: 'bizCheckMsg'
  };

  const ClientForm = {
    ids: { ...DEFAULT_IDS },
    // 중복확인 통과 여부(프론트 상태값)
    bizValidFlag: false,

    init(options = {}) {
      if (options.ids) this.ids = { ...DEFAULT_IDS, ...options.ids };

      document.addEventListener('DOMContentLoaded', () => {
        this.bindPhoneMask([this.ids.clientTel, this.ids.managerTel, this.ids.faxNumber]);
        this.bindBizNumberMask(this.ids.businessNumber);
        this.bindBizDuplicateCheck(); // ★ 중복확인 버튼 바인딩
        this.bindBizNumberOnChangeInvalidate(); // ★ 번호 변경 시 플래그 무효화
      });
    },

    // ===== 주소검색 =====
    execDaumPostcode() {
      if (typeof daum === 'undefined' || !daum.Postcode) {
        console.warn('[ClientForm] daum.Postcode 스크립트가 로드되지 않았습니다.');
        alert('주소 검색 스크립트가 준비되지 않았습니다. 새로고침 후 다시 시도해주세요.');
        return;
      }
      new daum.Postcode({
        oncomplete: (data) => {
          const $postcode = document.getElementById(this.ids.postcode);
          const $address = document.getElementById(this.ids.address);
          const $detail  = document.getElementById(this.ids.addressDetail);
          if ($postcode) $postcode.value = data.zonecode || '';
          if ($address)  $address.value  = data.roadAddress || data.jibunAddress || '';
          if ($detail)   $detail.focus();
        }
      }).open();
    },

    // ===== 폼 전체 검증 =====
    async validateForm() {
      // 1) 사업자등록번호 형식
      const $biz = document.getElementById(this.ids.businessNumber);
      const bizVal = ($biz?.value || '').trim();
      const reBiz = /^\d{3}-\d{2}-\d{5}$/;
      
      if (!reBiz.test(bizVal)) {
        alert("사업자등록번호는 반드시 '123-45-67890' 형식이어야 합니다.");
        $biz && $biz.focus();
        return false;
      }

      // 2) 중복확인 통과했는지. 미통과면 자동으로 한 번 호출해서 확인
      if (!this.bizValidFlag) {
        const ok = await this.checkBizDuplicate(bizVal);
        if (!ok) {
          alert('사업자등록번호 중복확인을 완료해주세요.');
          return false;
        }
      }

      // 3) 전화번호 형식
      const telIds = [this.ids.clientTel, this.ids.managerTel];
      for (const id of telIds) {
        const el = document.getElementById(id);
        if (!el || !el.value) continue;
        if (!this.isValidKRPhone(el.value)) {
          el.focus();
          el.reportValidity && el.reportValidity();
          alert('유효한 전화번호 형식이 아닙니다.');
          return false;
        }
      }
      return true;
    },

    // ===== 사업자등록번호 마스킹 =====
    bindBizNumberMask(id) {
      const $biz = document.getElementById(id);
      if (!$biz) return;
      $biz.addEventListener('input', () => {
        let number = ($biz.value || '').replace(/[^0-9]/g, '');
        if (number.length < 4) $biz.value = number;
        else if (number.length < 6) $biz.value = number.slice(0, 3) + '-' + number.slice(3);
        else $biz.value = number.slice(0, 3) + '-' + number.slice(3, 5) + '-' + number.slice(5, 10);
      });
    },

    // 번호가 바뀌면 중복확인 플래그를 무효화
    bindBizNumberOnChangeInvalidate() {
      const $biz = document.getElementById(this.ids.businessNumber);
      if (!$biz) return;
      ['input','change','blur'].forEach(evt => {
        $biz.addEventListener(evt, () => {
          this.setBizMsg('', null);     // 메시지 초기화
          this.bizValidFlag = false;    // 다시 확인 필요
        });
      });
    },

    
 // ===== 중복확인 버튼 바인딩 (새 코드) =====
    bindBizDuplicateCheck() { 
      const $btn = document.getElementById(this.ids.checkBizBtn);
      if (!$btn) return;

      $btn.addEventListener('click', () => {
        const $biz = document.getElementById(this.ids.businessNumber);
        const bizVal = ($biz?.value || '').trim();

        // 유효성 검사
        const re = /^\d{3}-\d{2}-\d{5}$/;
        if (!re.test(bizVal)) {
          alert("사업자등록번호는 반드시 '123-45-67890' 형식이어야 합니다.");
          $biz && $biz.focus();
          return;
        }

        // Ajax 호출
        fetch(`/client/checkBizNo?businessNumber=${encodeURIComponent(bizVal)}`, {
          method: 'GET',
          headers: { 'Accept': 'application/json' }
        })
        .then(res => {
          if (!res.ok) throw new Error("서버 오류");
          return res.json();
        })
        .then(data => {
          const el = document.getElementById(ClientForm.ids.bizCheckMsg);
          if (!el) return;
          if (data.exists) {
            el.textContent = '이미 등록된 사업자번호입니다.';
            el.className = 'form-text text-danger';
          } else {
            el.textContent = '사용 가능한 사업자번호입니다.';
            el.className = 'form-text text-success';
          }
        })
        .catch(err => {
          alert("중복확인 중 오류 발생");
          console.error(err);
        });
      });
    },


    // 메시지 표시 헬퍼
    setBizMsg(text, ok /* true/false/null */) {
      const el = document.getElementById(this.ids.bizCheckMsg);
      if (!el) return;
      el.textContent = text || '';
      if (ok === true)      el.className = 'form-text text-success';
      else if (ok === false)el.className = 'form-text text-danger';
      else                  el.className = 'form-text text-muted';
    },

    // ===== 전화번호 마스킹 =====
    bindPhoneMask(ids = []) {
      ids.forEach((id) => {
        const $el = document.getElementById(id);
        if (!$el) return;

        $el.addEventListener('input', function () {
          const formatted = ClientForm.formatKRPhone(this.value);
          this.value = formatted;
          this.setCustomValidity('');
        });

        $el.addEventListener('blur', function () {
          if (this.value && !ClientForm.isValidKRPhone(this.value)) {
            this.setCustomValidity('유효한 전화번호 형식이 아닙니다.');
          } else {
            this.setCustomValidity('');
          }
        });
      });
    },

    // ★ 유형별 최대 숫자 자릿수 결정 (하이픈 제외)
    maxDigitsFor(num) {
      if (!num) return 11;
      if (/^1/.test(num)) return 8;                        // 대표번호 1xxx-xxxx
      if (num.startsWith('02')) return 10;                 // 서울
      if (num.startsWith('050')) return 12;                // 050X
      if (num.startsWith('070') || num.startsWith('080')) return 11; // 070, 080
      if (/^01[016789]/.test(num)) return 11;              // 휴대폰
      if (/^0[3-6]\d/.test(num)) return 11;                // 031~064 등
      return 11;
    },

    // 한국 전화번호 포맷터
    formatKRPhone(raw) {
      let num = (raw || '').replace(/[^0-9]/g, '');
      const max = this.maxDigitsFor(num);
      if (num.length > max) num = num.slice(0, max);

      // 대표번호: 15xx/16xx/18xx/14xx 등
      if (/^1\d{3}/.test(num)) {
        if (num.length <= 4) return num;
        return num.slice(0, 4) + '-' + num.slice(4, 8);
      }

      // 02 (서울)
      if (num.startsWith('02')) {
        if (num.length <= 2) return num;
        if (num.length <= 6) return num.slice(0, 2) + '-' + num.slice(2);
        return num.slice(0, 2) + '-' + num.slice(2, num.length - 4) + '-' + num.slice(-4);
      }

      // 050X(개인번호) - 지역번호 4
      if (num.startsWith('050')) {
        if (num.length <= 4) return num;
        if (num.length <= 8) return num.slice(0, 4) + '-' + num.slice(4);
        return num.slice(0, 4) + '-' + num.slice(4, num.length - 4) + '-' + num.slice(-4);
      }

      // 그외(모바일/070/080/지역 031~064 등) - 지역번호 3
      if (num.length <= 3) return num;
      if (num.length <= 7) return num.slice(0, 3) + '-' + num.slice(3);
      return num.slice(0, 3) + '-' + num.slice(3, num.length - 4) + '-' + num.slice(-4);
    },

    isValidKRPhone(val) {
      const pMobile  = /^01[016789]-\d{3,4}-\d{4}$/;
      const pSeoul   = /^02-\d{3,4}-\d{4}$/;
      const pArea    = /^0(3[1-3]|4[1-4]|5[1-5]|6[1-4])-\d{3,4}-\d{4}$/;
      const p070     = /^070-\d{3,4}-\d{4}$/;
      const p080     = /^080-\d{3,4}-\d{4}$/;
      const p050x    = /^050\d-\d{3,4}-\d{4}$/;
      const pService = /^1\d{3}-\d{4}$/;
      return pMobile.test(val) || pSeoul.test(val) || pArea.test(val) ||
             p070.test(val) || p080.test(val) || p050x.test(val) || pService.test(val);
    }
  };

  ClientForm.init();

  // 전역 바인딩
  window.ClientForm = ClientForm;
  window.execDaumPostcode = ClientForm.execDaumPostcode.bind(ClientForm);
  window.validateForm     = ClientForm.validateForm.bind(ClientForm);

})(window, document);

