package com.itwillbs.domain;

import lombok.Data;

/**
 * 범용 검색 + 정렬 + 페이징 처리를 위한 통합 SearchCriteria
 */
@Data
public class SearchCriteria {

    // 검색 키워드
    private String keyword;
    private String condition;

    // 정렬
    private String sortColumn;    // 정렬 기준 컬럼 (항상 DB 컬럼명(snake_case)로 세팅)
    private String sortOrder = "desc"; // asc / desc

    // 페이징
    private int page = 1;         // 현재 페이지
    private int perPageNum = 10;  // 페이지당 개수

    // 기본 생성자 - 기본 정렬 컬럼을 안전한 DB 컬럼명으로
    public SearchCriteria() {
        this.sortColumn = null; // 기본값 없음, 컨트롤러에서 세팅하도록
    }

    // 사용자 지정 기본 정렬 컬럼 지정 생성자
    public SearchCriteria(String defaultSortColumn) {
        this.sortColumn = defaultSortColumn;
    }

    // 계산 메서드
    public int getPageStart() {
        return (page - 1) * perPageNum;
    }
    
    
    private String status;   // 상태 필터
    private String startDate;     // 기간조회 시작일 (yyyy-MM-dd)
    private String endDate;       // 기간조회 종료일 (yyyy-MM-dd)
    private int totalCount;		 //
    private String mode;  // 목록전체보기

    	
 // 방어 코드 추가
    public void setPage(int page) {
        if (page <= 0) {
            this.page = 1;
        } else {
            this.page = page;
        }
    }
    
 // 총 페이지 수 계산
    public int getTotalPageCount() {
        return (int) Math.ceil((double) totalCount / perPageNum);
    }

    // 이전 페이지 존재 여부
    public boolean isHasPrevPage() {
        return page > 1;
    }

    // 다음 페이지 존재 여부
    public boolean isHasNextPage() {
        return page < getTotalPageCount();
    }
   

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "keyword='" + keyword + '\'' +
                ", sortColumn='" + sortColumn + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", page=" + page +
                ", perPageNum=" + perPageNum +
                '}';
    }
}
