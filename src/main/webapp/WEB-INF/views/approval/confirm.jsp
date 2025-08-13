<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>발주 승인 처리</title>
</head>
<body>
<h2>발주 승인 결과</h2>

<c:choose>
    <c:when test="${status == 'success'}">
        <p style="color: green;">요청이 성공적으로 승인되었습니다.</p>
    </c:when>
    <c:when test="${status == 'rejected'}">
        <p style="color: red;">요청이 거절되었습니다.</p>
    </c:when>
    <c:when test="${status == 'expired'}">
        <p style="color: gray;">링크가 만료되었거나 이미 처리된 요청입니다.</p>
    </c:when>
    <c:otherwise>
        <form action="/approval/approve" method="post" style="display:inline;">
            <input type="hidden" name="tokenId" value="${tokenId}" />
            <button type="submit">승인</button>
        </form>
        <form action="/approval/reject" method="post" style="display:inline;">
            <input type="hidden" name="tokenId" value="${tokenId}" />
            <button type="submit">거절</button>
        </form>
    </c:otherwise>
</c:choose>

</body>
</html>
