package com.itwillbs.persistence;

import com.itwillbs.domain.ApprovalTokenVO;

public interface ApprovalTokenDAO {

    void insert(ApprovalTokenVO token);

    ApprovalTokenVO findByTokenId(String tokenId);

   	void markTokenUsed(String tokenId);
}
