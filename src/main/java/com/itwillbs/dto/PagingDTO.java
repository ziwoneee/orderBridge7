package com.itwillbs.dto;

import lombok.Data;

@Data
public class PagingDTO {

	private int page;
    private int size;
    private int totalCount;

    private int startPage;
    private int endPage;
    private boolean prev;
    private boolean next;

    private int offset;

    private int totalPages; // ✅ 필드 추가

    public PagingDTO() {
        this.page = 1;
        this.size = 10;
    }

    public PagingDTO(int page, int size, int totalCount) {
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;

        this.offset = (page - 1) * size;

        int pageBlock = 5;

        this.totalPages = (int)Math.ceil(totalCount / (double)size); // ✅ 저장
        this.endPage = (int)(Math.ceil(page / (double)pageBlock)) * pageBlock;
        this.startPage = this.endPage - (pageBlock - 1);

        if (endPage > totalPages) this.endPage = totalPages;

        this.prev = this.startPage > 1;
        this.next = this.endPage < totalPages;
    }
	    
}
