<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/main/layout_head.jsp" %>

<div class="container-scroller">
  <%@ include file="/WEB-INF/views/main/top.jsp" %>      
  <div class="container-fluid page-body-wrapper">
    <%@ include file="/WEB-INF/views/main/sidebar.jsp" %>

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">

    <style>
        .list-group-item { cursor:pointer; }
        .list-group-item.active, .list-group-item:active { background:#4a95f7; color:white; }
    </style>

    <script>
    var detailsIndex = 0;

    function selectMaterial(id, name, unit) {
        document.getElementById("materialId").value = id;
        document.getElementById("materialName").value = name;
        document.getElementById("matUnit").value = unit;
    }

    function addMaterial() {
        var mid = document.getElementById("materialId").value;
        var mname = document.getElementById("materialName").value;
        var qty = document.getElementById("matQty").value;
        var unit = document.getElementById("matUnit").value;
        var materialType = document.querySelector('input[name="matType"]:checked').value;

        if (!mid || !qty || qty <= 0) {
            alert("자재 선택 및 수량 입력!");
            return;
        }

        var rowId = "row-" + mid + "-" + materialType;
        if (document.getElementById(rowId)) {
            alert("이미 추가된 원자재입니다!");
            return;
        }

        var tbody = materialType === "육수" ? document.getElementById("soupDetailBody") : document.getElementById("solidDetailBody");
        var idx = detailsIndex++;
        var tr = document.createElement("tr");
        tr.id = rowId;
        tr.innerHTML =
            '<td>' + mname +
                '<input type="hidden" name="details[' + idx + '].materialId" value="' + mid + '" />' +
                '<input type="hidden" name="details[' + idx + '].materialName" value="' + mname + '" />' +
                '<input type="hidden" name="details[' + idx + '].materialType" value="' + materialType + '" />' +
            '</td>' +
            '<td>' + qty +
                '<input type="hidden" name="details[' + idx + '].qty" value="' + qty + '" />' +
            '</td>' +
            '<td>' + unit +
                '<input type="hidden" name="details[' + idx + '].unit" value="' + unit + '" />' +
            '</td>' +
            '<td><button type="button" class="btn btn-danger btn-sm" onclick="deleteRow(this)">삭제</button></td>';

        tbody.appendChild(tr);

        document.getElementById("materialId").value = "";
        document.getElementById("materialName").value = "";
        document.getElementById("matQty").value = "";
        document.getElementById("matUnit").value = "";
    }

    function deleteRow(btn) {
        btn.closest('tr').remove();
        renumberDetails();
    }

    function renumberDetails() {
        var rows = document.querySelectorAll("#soupDetailBody tr, #solidDetailBody tr");
        for (let i = 0; i < rows.length; i++) {
            rows[i].querySelectorAll("input").forEach(function(input) {
                input.name = input.name.replace(/details\[\d+\]/, "details[" + i + "]");
            });
        }
        detailsIndex = rows.length;
    }

    function validateBeforeSubmit() {
        const hasDetails = document.querySelectorAll('input[name^="details["]').length > 0;
        if (!hasDetails) {
            alert("원자재를 하나 이상 추가해주세요!");
            return false;
        }
        return true;
    }
    </script>

    <!-- 본문 시작 -->
    <div class="main-panel">
      <div class="content-wrapper">
        <div class="row">    
          <div class="col-12 mb-4">
            <h3 class="font-weight-bold">BOM (생산 레시피) 신규등록</h3>
          </div>

          <div class="container mt-5" style="margin-left: 0; padding-left: 0;">
            <form method="post" action="${pageContext.request.contextPath}/master/bom/insert" onsubmit="return validateBeforeSubmit()">
              <!-- 제품 선택 -->
              <div class="form-group row">
                <label class="col-sm-2 col-form-label">대상 제품</label>
                <div class="col-sm-6">
                  <select name="productId" class="form-control" required>
                    <option value="">선택</option>
                    <c:forEach var="prod" items="${productList}">
                      <option value="${prod.productId}">${prod.productName}</option>
                    </c:forEach>
                  </select>
                </div>
              </div>

              <div class="form-group row">
                <label class="col-sm-2 col-form-label">BOM명</label>
                <div class="col-sm-6">
                  <input type="text" name="bomName" class="form-control" required />
                </div>
              </div>

              <div class="form-group row">
                <label class="col-sm-2 col-form-label">상태</label>
                <div class="col-sm-4">
                  <select name="status" class="form-control">
                    <option value="ACTIVE">활성</option>
                    <option value="INACTIVE">비활성</option>
                  </select>
                </div>
              </div>

              <hr />

              <div class="row">
                <!-- 자재 선택 -->
                <div class="col-3">
                  <div class="mb-2 font-weight-bold">원자재 선택</div>
                  <ul class="list-group">
                    <c:forEach var="mat" items="${materialList}">
                      <li class="list-group-item"
                          onclick="selectMaterial('${mat.materialId}', '${mat.materialName}', '${mat.unit}')">
                        ${mat.materialName}
                      </li>
                    </c:forEach>
                  </ul>
                </div>

                <!-- 자재 입력 -->
                <div class="col-9">
                  <div class="form-inline mb-3">
                    <label class="mr-2"><input type="radio" name="matType" value="육수" checked /> 육수용</label>
                    <label class="mr-4"><input type="radio" name="matType" value="원료" /> 원료용</label>

                    <input type="hidden" id="materialId" name="dummyMaterialId" />
                    <input type="text" id="materialName" class="form-control mr-2" style="width:160px;" placeholder="자재명" readonly />
                    <input type="number" id="matQty" class="form-control mr-2" style="width:90px;" min="0.01" step="0.01" placeholder="수량" />
                    <input type="text" id="matUnit" class="form-control mr-2" style="width:70px;" placeholder="단위" readonly />
                    <button type="button" class="btn btn-primary" onclick="addMaterial()">추가</button>
                    <button type="submit" class="btn btn-success ml-5">레시피 저장</button>
                  </div>

                  <h5>육수용 원자재</h5>
                  <table class="table table-bordered">
                    <thead>
                      <tr><th>원자재명</th><th>소요량</th><th>단위</th><th>관리</th></tr>
                    </thead>
                    <tbody id="soupDetailBody"></tbody>
                  </table>

                  <h5>원료용 원자재</h5>
                  <table class="table table-bordered">
                    <thead>
                      <tr><th>원자재명</th><th>소요량</th><th>단위</th><th>관리</th></tr>
                    </thead>
                    <tbody id="solidDetailBody"></tbody>
                  </table>
                </div>
              </div>

              <div class="form-group row mt-3">
                <label class="col-sm-2 col-form-label">비고</label>
                <div class="col-sm-4">
                  <input type="text" name="bomEtc" class="form-control" />
                </div>
              </div>

            </form>
          </div>
        </div>

        <%@ include file="/WEB-INF/views/main/layout_footer.jsp" %>
      </div>
    </div>
  </div>
</div>
