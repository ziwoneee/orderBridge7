<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

	<nav class="sidebar sidebar-offcanvas" id="sidebar">
	  <ul class="nav">
	  
	          <!-- 기준 정보 관리 시작 -->
	          <li class="nav-item ${menu eq 'basic' ? 'active' : ''}">
	            <a class="nav-link"
				   data-toggle="collapse"
				   href="#menu-basic"
				   aria-expanded="${menu eq 'basic' ? 'true' : 'false'}"
				   aria-controls="menu-basic">
	              <i class="ti-layers-alt menu-icon"></i>
	              <span class="menu-title">기준 정보 관리</span>
	              <i class="menu-arrow"></i>
	            </a>
	            <div class="collapse ${menu eq 'basic' ? 'show' : ''}" id="menu-basic">
	              <ul class="nav flex-column sub-menu">
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/material/list">자재 정보</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/master/bom/list">BOM 정보</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/master/product/list">완제품 정보</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/client/list">고객사 정보</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/supplier/list">협력사 정보</a></li>
	              </ul>
	            </div>
	          </li>
	          <!-- 기준 정보 관리 끝 -->
	          
	          <!-- 영업 관리 시작 -->
	          <li class="nav-item ${menu eq 'sales' ? 'active' : ''}">
   	            <a class="nav-link"
				   data-toggle="collapse"
				   href="#menu-sales"
				   aria-expanded="${menu eq 'sales' ? 'true' : 'false'}"
				   aria-controls="menu-sales">
	              <i class="ti-briefcase menu-icon"></i>
	              <span class="menu-title">영업 관리</span>
	              <i class="menu-arrow"></i>
	            </a>
	            <div class="collapse ${menu eq 'sales' ? 'show' : ''}" id="menu-sales">
	              <ul class="nav flex-column sub-menu">
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/clientorder/list">수주 관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/">생산 재고 관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/shipment/list">출하 관리</a></li>
	              </ul>
	            </div>
	          </li>
	          <!-- 영업 관리 끝 -->
	          
	          <!-- 생산 관리 시작 -->
	          <li class="nav-item ${menu eq 'production' ? 'active' : ''}">
	             <a class="nav-link" data-toggle="collapse"
				     href="#menu-production"
				     aria-expanded="${menu eq 'production' ? 'true' : 'false'}"
	                 aria-controls="menu-production">
	              <i class="ti-reload menu-icon"></i>
	              <span class="menu-title">생산 관리</span>
	              <i class="menu-arrow"></i>
	            </a>
	            <div class="collapse ${menu eq 'production' ? 'show' : ''}" id="menu-production">
	              <ul class="nav flex-column sub-menu">
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/workorder/list">작업지시관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/plan/list">계획 목록</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/mps/production/write">아직없음</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/mps/quality/list">실적관리</a></li>
	              </ul>
	            </div>
	          </li>
	          <!-- 생산 관리 끝 -->
	          
	          <!-- 자재 관리 시작 -->
	          <li class="nav-item ${menu eq 'material' ? 'active' : ''}">
              	<a class="nav-link" data-toggle="collapse"
				   href="#menu-material"
				   aria-expanded="${menu eq 'material' ? 'true' : 'false'}"
				   aria-controls="menu-material">
	              <i class="ti-package menu-icon"></i>
	              <span class="menu-title">자재 관리</span>
	              <i class="menu-arrow"></i>
	            </a>
	            <div class="collapse ${menu eq 'material' ? 'show' : ''}" id="menu-material">
	              <ul class="nav flex-column sub-menu">
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/material/inventory/list">재고 현황</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/material/order/list">발주 관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/material/inbound/list">입고 관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/material/outbound/list">출고 관리</a></li>
	              </ul>
	            </div>
	          </li>
	          <!-- 자재 관리 끝 -->
	          
	          <!-- 완제품 관리 시작 -->
	          <li class="nav-item ${menu eq 'product' ? 'active' : ''}">
	            <a class="nav-link" data-toggle="collapse"
				   href="#menu-product"
				   aria-expanded="${menu eq 'product' ? 'true' : 'false'}"
				   aria-controls="menu-product">
	              <i class="ti-check-box menu-icon"></i>
	              <span class="menu-title">완제품 관리</span>
	              <i class="menu-arrow"></i>
	            </a>
	            <div class="collapse ${menu eq 'product' ? 'show' : ''}" id="menu-product">
	              <ul class="nav flex-column sub-menu">
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/product/stocklist">재고 현황</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/wms/placeorder/insertOrder">발주 관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/product/inbound/list">입고 관리</a></li>
	                <li class="nav-item"> <a class="nav-link" href="${pageContext.request.contextPath}/product/outbound/list">출고 관리</a></li>
	              </ul>
	            </div>
	          </li>
	          <!-- 완제품 관리 끝 -->
	
	  </ul>
	</nav>
	<!-- End Sidebar -->