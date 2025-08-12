/* client-form.js
 * - 주소 검색(카카오/다음 우편번호)
 * - 사업자등록번호 자동 하이픈 & 검증
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
    faxNumber: 'faxNumber'
  };

  const ClientForm = {
    ids: { ...DEFAULT_IDS },

    init(options = {}) {
      if (options.ids) this.ids = { ...DEFAULT_IDS, ...options.ids };

      document.addEventListener('DOMContentLoaded', () => {
        this.bindPhoneMask([this.ids.clientTel, this.ids.managerTel, this.ids.faxNumber]);
        this.bindBizNumberMask(this.ids.businessNumber);
      });
    },

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

    validateForm() {
      const biz = document.querySelector('input[name="businessNumber"]');
      if (biz && !/^\d{3}-\d{2}-\d{5}$/.test(biz.value || '')) {
        alert("사업자등록번호는 반드시 '123-45-67890' 형식이어야 합니다.");
        return false;
      }

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

    bindBizNumberMask(id) {
      const $biz = document.getElementById(id);
      if (!$biz) return;
      $biz.addEventListener('input', function () {
        let number = (this.value || '').replace(/[^0-9]/g, '');
        if (number.length < 4) this.value = number;
        else if (number.length < 6) this.value = number.slice(0, 3) + '-' + number.slice(3);
        else this.value = number.slice(0, 3) + '-' + number.slice(3, 5) + '-' + number.slice(5, 10);
      });
    },

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

    // ★ 유형별 최대 숫자 자릿수 결정 (하이픈 제외한 숫자만)
    maxDigitsFor(num) {
      if (!num) return 11;
      if (/^1/.test(num)) return 8;                        // 대표번호 1xxx-xxxx
      if (num.startsWith('02')) return 10;                 // 서울
      if (num.startsWith('050')) return 12;                // 050X
      if (num.startsWith('070') || num.startsWith('080')) return 11; // 070, 080
      if (/^01[016789]/.test(num)) return 11;              // 휴대폰
      if (/^0[3-6]\d/.test(num)) return 11;                // 031~064 등
      return 11;                                           // 기본
    },

    // 한국 전화번호 포맷터 (자릿수 하드 제한 포함)
    formatKRPhone(raw) {
      let num = (raw || '').replace(/[^0-9]/g, '');
      // 자릿수 하드 제한
      const max = this.maxDigitsFor(num);
      if (num.length > max) num = num.slice(0, max);

      // 대표번호: 15xx,16xx,18xx,14xx 등 (예: 1588-1234)
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

      // 050X(개인번호) - 지역번호 길이 4
      if (num.startsWith('050')) {
        if (num.length <= 4) return num;
        if (num.length <= 8) return num.slice(0, 4) + '-' + num.slice(4);
        return num.slice(0, 4) + '-' + num.slice(4, num.length - 4) + '-' + num.slice(-4);
      }

      // 그외(모바일 010/011/016~019, 070, 080, 지역번호 031~064 등) - 지역번호 3
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

  window.ClientForm = ClientForm;
  window.execDaumPostcode = ClientForm.execDaumPostcode.bind(ClientForm);
  window.validateForm     = ClientForm.validateForm.bind(ClientForm);

})(window, document);
