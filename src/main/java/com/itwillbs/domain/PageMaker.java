package com.itwillbs.domain;

public class PageMaker {
    private int totalCount;
    private int startPage;
    private int endPage;
    private boolean prev;
    private boolean next;
    private int displayPageNum = 10;
    private SearchCriteria cri;

    public PageMaker(SearchCriteria cri, int totalCount) {
        this.cri = cri;
        this.totalCount = totalCount;

        this.endPage = (int) (Math.ceil(cri.getPage() / (double) displayPageNum) * displayPageNum);
        this.startPage = (endPage - displayPageNum) + 1;
        int tempEndPage = (int) (Math.ceil(totalCount / (double) cri.getPerPageNum()));
        if (endPage > tempEndPage) endPage = tempEndPage;
        this.prev = startPage != 1;
        this.next = endPage * cri.getPerPageNum() < totalCount;
    }

    // --- Getter ---
    public int getTotalCount() {
        return totalCount;
    }
    public int getStartPage() {
        return startPage;
    }
    public int getEndPage() {
        return endPage;
    }
    public boolean isPrev() {
        return prev;
    }
    public boolean isNext() {
        return next;
    }
    public int getDisplayPageNum() {
        return displayPageNum;
    }
    public SearchCriteria getCri() {
        return cri;
    }

    // --- Setter ---
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }
    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }
    public void setPrev(boolean prev) {
        this.prev = prev;
    }
    public void setNext(boolean next) {
        this.next = next;
    }
    public void setDisplayPageNum(int displayPageNum) {
        this.displayPageNum = displayPageNum;
    }
    public void setCri(SearchCriteria cri) {
        this.cri = cri;
    }
}
