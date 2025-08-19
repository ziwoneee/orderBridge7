<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- 로그인 체크: 로그인 정보 없으면 로그인 페이지로 리다이렉트 --%>
<%
  if (session.getAttribute("loginAdmin") == null) {
    response.sendRedirect(request.getContextPath() + "/admin/login");
    return;
  }
%> 


<!-- 최상단 네비게이션 -->
<nav class="navbar col-lg-12 col-12 p-0 fixed-top d-flex flex-row">

   <!-- 로고 반응형 건들기 X - 시작 -->	
   <div class="text-center navbar-brand-wrapper d-flex align-items-center justify-content-center">
     <a class="navbar-brand brand-logo ml-5 mr-5" href="${pageContext.request.contextPath}/admin/dashboard">
     	<img src="${pageContext.request.contextPath}/resources/images/logo.png" class="mr-2" style="height: 50px;" alt="logo"/>
     </a>
     <a class="navbar-brand brand-logo-mini" href="${pageContext.request.contextPath}/admin/dashboard">
     	<img src="${pageContext.request.contextPath}/resources/images/logo-mini.png" alt="logo"/>
     </a>
   </div>
   <!-- 로고 반응형 건들기 X - 끝-->	

   <div class="navbar-menu-wrapper d-flex align-items-center justify-content-end">

       <!-- 사이드바 접기 버튼 - 시작 -->
       <button class="navbar-toggler navbar-toggler align-self-center" type="button" data-toggle="minimize">
         <span class="icon-menu"></span>
       </button>
       <!-- 사이드바 접기 버튼 - 끝 -->

    <ul class="navbar-nav mr-lg-2">
      <li class="nav-item nav-search d-none d-lg-block">
        <!-- 검색기능 자리 비워둠 -->
      </li>
    </ul>

    <ul class="navbar-nav navbar-nav-right">

      <%--  알림 아이콘 및 드롭다운 --%>
	  <li class="nav-item position-relative">
		<a href="javascript:void(0)" class="nav-link p-0 d-flex align-items-center" id="alarmIcon">
	  		<span class="position-relative d-inline-block" style="width: 28px; height: 28px;">
	    		<img src="https://api.iconify.design/mdi:bell.svg?color=%23ffc107&width=28" alt="bell icon">
		    	<span id="alarm-badge"
		          class="position-absolute bg-danger text-white fw-bold"
		          style="
		            top: -6px;
		            right: -6px;
		            width: 20px;
		            height: 20px;
		            font-size: 11px;
		            line-height: 20px;
		            text-align: center;
		            border-radius: 50%;
		            display: none;">
		     	    10
		   	   </span>
	  		</span>
		</a>
	  <div id="alarmDropdown">
	    <h6 class="p-3 mb-0">알림</h6>
	    <div id="alarm-list"></div>
	  </div>
	 </li>

      <%--  전화번호부 팝업 --%>
      <li class="nav-item dropdown">
        <a class="nav-link count-indicator" href="javascript:void(0)" onclick="yellowPage()">
          <img src="${pageContext.request.contextPath}/resources/images/free-icon-phone-book-4812550.png"
               width="35" height="35" style="border:0; background-color: transparent;">
        </a>
      </li>

      <%--  관리자 이름 및 드롭다운 메뉴 --%>
      <li class="nav-item nav-profile dropdown navbar-profile">
        <a href="javascript:void(0)" class="nav-link dropdown-toggle" id="profileDropdown">
          <span class="text-primary">${sessionScope.loginAdmin.name}</span> 님
        </a>
        <div class="dropdown-menu dropdown-menu-right navbar-dropdown" aria-labelledby="profileDropdown">
          <a class="dropdown-item" href="${pageContext.request.contextPath}/admin/settings/accounts">
            <i class="ti-settings text-primary"></i> 설정
          </a>
          <a class="dropdown-item" href="${pageContext.request.contextPath}/admin/logout">
            <i class="ti-power-off text-primary"></i> 로그아웃
          </a>
        </div>
      </li>

    </ul>

    <button class="navbar-toggler navbar-toggler-right d-lg-none align-self-center" type="button" data-toggle="offcanvas">
      <span class="icon-menu"></span>
    </button>

  </div>
</nav>

<%-- 전화번호부 팝업 스크립트 --%>
<script type="text/javascript">
function yellowPage() {
  var _width = '1200';
  var _height = '650';
  var _left = Math.ceil((window.screen.width - _width) / 2);
  var _top = Math.ceil((window.screen.height - _height) / 2);
  let popOption = 'width=' + _width + ', height=' + _height + ', left=' + _left + ', top=' + _top;
  window.open("${pageContext.request.contextPath}/employee/yellowPage", "오더브릿지", popOption);
}
</script>

<%--  알림 드롭다운 및 무한스크롤 제어 JS --%>
<script src="${pageContext.request.contextPath}/resources/js/alarm-popup.js"></script>

  <style>
    /*  반응형 알림 드롭다운 위치 고정 */
    #alarmDropdown {
      position: absolute;
      top: 50px;
      right: 0;
      min-width: 280px;
      max-width: 360px;
      width: 100%;
      max-height: 300px;
      overflow-y: auto;
      background-color: white;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      z-index: 1000;
      display: none;
    }

    /* 부모 위치 기준 지정 */
    .navbar-nav-right {
      position: relative;
      gap: 0.4rem;
    }

    /*  모바일 반응형 (작은 화면) */
    @media (max-width: 576px) {
      #alarmDropdown {
        left: 10px;
        right: 10px;
        width: auto;
        min-width: unset;
        max-width: calc(100vw - 20px);
        top: 50px;
      }
    }
  </style>