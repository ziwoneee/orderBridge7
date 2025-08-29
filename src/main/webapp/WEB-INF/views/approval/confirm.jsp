<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>발주 승인 처리</title>
  <style>
    :root{
      --bg:#f7f9fc; --card:#fff; --text:#1f2937; --muted:#6b7280;
      --primary:#2563eb; --success:#16a34a; --danger:#dc2626; --warning:#f59e0b;
      --border:#e5e7eb; --shadow:0 10px 30px rgba(0,0,0,.06);
      --radius:16px;
    }
    *{box-sizing:border-box}
    body{margin:0;background:var(--bg);font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Noto Sans KR","Apple SD Gothic Neo",sans-serif;color:var(--text)}
    .wrap{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:28px}
    .card{width:100%;max-width:680px;background:var(--card);border-radius:var(--radius);box-shadow:var(--shadow);border:1px solid var(--border)}
    .card-body{padding:28px}
    .top{display:flex;align-items:center;justify-content:space-between;margin-bottom:18px}
    .brand{display:flex;align-items:center;gap:12px}
    .brand img{height:40px;width:auto;display:block}
    .title{font-size:20px;font-weight:700}
    .badge{background:var(--primary);color:#fff;border-radius:999px;padding:6px 12px;font-size:12px}
    .alert{border-radius:12px;padding:14px 16px;margin:14px 0;border:1px solid var(--border)}
    .alert.success{background:#ecfdf5;color:#065f46;border-color:#a7f3d0}
    .alert.danger{background:#fef2f2;color:#7f1d1d;border-color:#fecaca}
    .alert.secondary{background:#f3f4f6;color:#374151;border-color:#e5e7eb}
    .desc{color:var(--muted);margin:6px 0 16px}
    .btns{display:flex;gap:10px;flex-wrap:wrap}
    button, .btn{
      appearance:none;border:0;border-radius:12px;padding:10px 16px;font-weight:600;cursor:pointer;
      transition:.15s transform ease, .15s filter ease; font-size:14px;
    }
    .btn:active{transform:scale(.98)}
    .btn-success{background:var(--success);color:#fff}
    .btn-danger-outline{background:#fff;color:var(--danger);border:1px solid var(--danger)}
    .footer{display:flex;justify-content:space-between;align-items:center;margin-top:18px;color:var(--muted);font-size:13px}
    .link{color:var(--primary);text-decoration:none}
    .link:hover{text-decoration:underline}
  </style>
</head>
<body>
<div class="wrap">
  <div class="card">
    <div class="card-body">
      <div class="top">
        <div class="brand">
          <img src="<c:url value='/resources/images/logo.png'/>" alt="logo">
          <div class="title">(주)오더브릿지</div>
        </div>
        <span class="badge">발주 승인</span>
      </div>

      <c:choose>
        <c:when test="${status == 'success'}">
          <div class="alert success">요청이 성공적으로 승인되었습니다.</div>
        </c:when>
        <c:when test="${status == 'rejected'}">
          <div class="alert danger">요청이 거절되었습니다.</div>
        </c:when>
        <c:when test="${status == 'expired'}">
          <div class="alert secondary">링크가 만료되었거나 이미 처리된 요청입니다.</div>
        </c:when>
        <c:otherwise>
          <p class="desc">아래 버튼을 눌러 발주 요청을 승인 또는 거절할 수 있습니다.</p>
          <div class="btns">
            <form action="<c:url value='/approval/approve'/>" method="post">
              <input type="hidden" name="tokenId" value="${tokenId}" />
              <button type="submit" class="btn btn-success">승인</button>
            </form>
            <form action="<c:url value='/approval/reject'/>" method="post">
              <input type="hidden" name="tokenId" value="${tokenId}" />
              <button type="submit" class="btn btn-danger-outline">거절</button>
            </form>
          </div>
        </c:otherwise>
      </c:choose>

      <div class="footer">
        <small>문의: 051-123-4567 · support@orderbridge.co.kr</small>       
      </div>
    </div>
  </div>
</div>
</body>
</html>
