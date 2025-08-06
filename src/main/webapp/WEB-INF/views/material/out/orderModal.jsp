<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!-- 작업지시서 선택 모달 -->
<div class="modal fade" id="orderModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-xl" role="document">
    <div class="modal-content">
    
      <div class="modal-header">
        <h5 class="modal-title">작업지시서 선택</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      
      <div class="modal-body">
        <table class="table table-bordered text-center">
          <thead style="background-color: #1C355E; color: white;">
            <tr>
              <th>작업지시번호</th>
              <th>제품코드</th>
              <th>라인</th>
              <th>지시수량</th>
              <th>비고</th>
              <th>선택</th>
            </tr>
          </thead>
          <tbody id="orderTableBody">
            <!-- JS로 동적으로 채워질 영역 -->
          </tbody>
        </table>
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>
