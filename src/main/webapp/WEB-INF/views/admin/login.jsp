<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>관리자 로그인 | OrderBridge</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Bootstrap CDN -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
  
  <!-- Custom CSS -->
  <link rel="stylesheet" href="/resources/maincss/css/login.css">
</head>
<body class="bg-light">

 <div class="container d-flex justify-content-center align-items-center min-vh-100">
  <div class="card shadow p-4" style="width: 100%; max-width: 400px;">
	<div class="text-center mb-4">
		<img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="OrderBridge 로고" style="height: 100px;">
        <br>
        <small class="text-muted">관리자 로그인</small>
	</div>

	<form action="/admin/login" method="post">
		<div class="form-group">
			<label for="adminId">아이디</label>
			<input type="text" name="adminId" id="adminId" class="form-control" placeholder="아이디 입력"
				   value="${rememberedId}" required>
        </div>

        <div class="form-group">
          <label for="adminPw">비밀번호</label>
          <input type="password" name="password" id="adminPw" class="form-control" placeholder="비밀번호 입력" required>
        </div>
        
	<!-- 로그인 실패 메시지 영역 -->
	<c:if test="${not empty errorMsg}">
		<div class="alert alert-danger text-center mt-3">${errorMsg}</div>
	</c:if>
	  
	<!-- 로그아웃 성공 메시지 영역 -->
	<c:if test="${not empty msg}">
		<div class="alert alert-success text-center mt-3">${msg}</div>
	</c:if>

		<button type="submit" class="btn btn-primary btn-block">로그인</button>

		<div class="form-check mt-2">
			<input class="form-check-input" type="checkbox" id="rememberMe" name="remember"
			<c:if test="${not empty rememberedId}">checked</c:if> >
			<label class="form-check-label" for="rememberMe">아이디 저장</label>
		</div>
	</form>
  </div>
 </div>

</body>
</html>
