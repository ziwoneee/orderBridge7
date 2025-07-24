package com.itwillbs.domain;

public class PageMaker {
    private int totalCount;
    private int startPage;
    private int endPage;
    private boolean prev;
    private boolean next;
    private int displayPageNum = 10;  // 한 블록당 보여줄 페이지 수
    private SearchCriteria cri;

    public PageMaker(SearchCriteria cri, int totalCount) {
        this.cri = cri;
        this.totalCount = totalCount;

        // 1. 끝 페이지 번호 계산
        this.endPage = (int) Math.ceil(cri.getPage() / (double) displayPageNum) * displayPageNum;

        // 2. 시작 페이지 번호 계산
        this.startPage = this.endPage - displayPageNum + 1;

        // 3. 전체 페이지 수 계산
        int totalPages = (int) Math.ceil(totalCount / (double) cri.getPerPageNum());

        // 4. endPage 보정 (총 페이지 수보다 큰 경우)
        if (this.endPage > totalPages) {
            this.endPage = totalPages;
        }

        // 5. 이전, 다음 버튼 조건 설정
        this.prev = this.startPage > 1;
        this.next = this.endPage < totalPages;
    }

    // --- Getter ---
    public int getTotalCount() { return totalCount; }
    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }
    public boolean isPrev() { return prev; }
    public boolean isNext() { return next; }
    public int getDisplayPageNum() { return displayPageNum; }
    public SearchCriteria getCri() { return cri; }

    // --- Setter ---
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public void setStartPage(int startPage) { this.startPage = startPage; }
    public void setEndPage(int endPage) { this.endPage = endPage; }
    public void setPrev(boolean prev) { this.prev = prev; }
    public void setNext(boolean next) { this.next = next; }
    public void setDisplayPageNum(int displayPageNum) { this.displayPageNum = displayPageNum; }
    public void setCri(SearchCriteria cri) { this.cri = cri; }
}
