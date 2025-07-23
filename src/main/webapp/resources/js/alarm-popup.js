let page = 1;
let isLoading = false;
let endOfData = false;

// 알림 목록 불러오기
function loadAlarms() {
  if (isLoading || endOfData) return;

  isLoading = true;

  fetch(`/admin/alarm/list?page=${page}`)
    .then(res => res.json())
    .then(data => {
      const list = document.getElementById("alarm-list");
      if (page === 1) list.innerHTML = "";

      if (data.length === 0) {
        endOfData = true;
        if (page === 1) {
          list.innerHTML = "<div class='text-center p-2 text-muted'>알림이 없습니다</div>";
        }
        return;
      }

      data.forEach(alarm => {
        const div = document.createElement("div");
        div.className = "alarm-item d-flex justify-content-between align-items-center px-2 py-1 border-bottom" + (alarm.isRead === 0 ? " unread font-weight-bold" : " bg-light text-muted");
        div.setAttribute("data-id", alarm.targetId);

        const created = new Date(alarm.createdAt);
        const dateStr = created.toLocaleString('ko-KR', {
          year: 'numeric', month: '2-digit', day: '2-digit',
          hour: '2-digit', minute: '2-digit'
        });

        div.innerHTML = `
          <div class="alarm-message small" style="max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer;" onclick="readAlarm(${alarm.targetId})">
            ${alarm.message} <br><span class="small text-muted">${dateStr}</span>
          </div>
          <div>
            <i class="mdi mdi-close text-danger alarm-delete" onclick="deleteAlarm(${alarm.targetId}, this)" style="cursor:pointer"></i>
          </div>
        `;

        list.appendChild(div);
      });

      page++;
      isLoading = false;
    })
    .catch(err => {
      console.error("알림 불러오기 오류:", err);
      isLoading = false;
    });
}

// 알림 개수 뱃지 업데이트
function updateBadge() {
  fetch("/admin/alarm/unread-count")
    .then(res => res.text())
    .then(count => {
      const badge = document.getElementById("alarm-badge");
      const num = parseInt(count);
      if (num > 0) {
        badge.innerText = num;
        badge.style.display = "inline-block";
      } else {
        badge.style.display = "none";
      }
    })
    .catch(err => {
      console.error("뱃지 업데이트 실패:", err);
    });
}

// 알림 읽음 처리
function readAlarm(targetId) {
  fetch(`/admin/alarm/read/${targetId}`, { method: "POST" })
    .then(() => {
      const el = document.querySelector(`[data-id='${targetId}']`);
      if (el) {
        el.classList.remove("unread", "font-weight-bold");
        el.classList.add("bg-light", "text-muted");
      }
      updateBadge();
    });
}

// 알림 삭제
function deleteAlarm(targetId, btn) {
  fetch(`/admin/alarm/delete/${targetId}`, { method: "DELETE" })
    .then(() => {
      const el = btn.closest(".alarm-item");
      if (el) el.remove();
      updateBadge();
    });
}

// 상태 변수
let isAlarmOpen = false;
let isAdminOpen = false;

window.addEventListener("DOMContentLoaded", () => {
  const dropdown = document.getElementById("alarmDropdown");
  const icon = document.getElementById("alarmIcon");
  const adminToggle = document.querySelector(".navbar-profile .dropdown-toggle");
  const adminMenu = document.querySelector(".navbar-profile .dropdown-menu");

  if (!dropdown || !icon) return;

  // 무한 스크롤
  dropdown.addEventListener("scroll", function () {
    if (this.scrollTop + this.clientHeight >= this.scrollHeight - 10) {
      loadAlarms();
    }
  });

  // 알림 아이콘 클릭
  icon.addEventListener("click", function (e) {
    e.preventDefault();
    e.stopPropagation();

    dropdown.style.position = "absolute";
    dropdown.style.right = "0px";
    dropdown.style.top = "50px";
    dropdown.style.minWidth = "280px";

    isAlarmOpen = !isAlarmOpen;
    dropdown.style.display = isAlarmOpen ? "block" : "none";

    if (isAlarmOpen) {
      page = 1;
      endOfData = false;
      loadAlarms();
    }
  });

  // 관리자 드롭다운 클릭
  if (adminToggle && adminMenu) {
    adminToggle.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      isAdminOpen = !isAdminOpen;
      adminMenu.classList.toggle("show", isAdminOpen);
    });
  }

  // 외부 클릭 시 닫기 (모달 제외)
  document.addEventListener("click", function (e) {
    const isInsideAlarm = icon.contains(e.target) || dropdown.contains(e.target);
    const isInsideAdmin = adminToggle.contains(e.target) || adminMenu.contains(e.target);
    const isInsideModal = e.target.closest(".modal") !== null;

    if (!isInsideAlarm && !isInsideModal) {
      isAlarmOpen = false;
      dropdown.style.display = "none";
    }

    if (!isInsideAdmin && !isInsideModal) {
      isAdminOpen = false;
      adminMenu.classList.remove("show");
    }
  });

  updateBadge();
});
