package com.itwillbs.service;

import com.itwillbs.domain.ApprovalTokenVO;

public interface ApprovalTokenService {
 ApprovalTokenVO findByTokenId(String tokenId);
 boolean approve(String tokenId);
 boolean reject(String tokenId);
}
