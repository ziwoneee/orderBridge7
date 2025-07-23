package com.itwillbs.service;

import com.itwillbs.domain.AdminUserVO;

public interface AdminUserService {
	
    // 로그인 처리 
    AdminUserVO login(AdminUserVO vo);
    
    // 관리자 아이디로 정보 조회 (로그인용)
    public AdminUserVO findByAdminId(String adminId);
    
    // 관리자 등록
    public void insertAdmin(AdminUserVO vo);
}