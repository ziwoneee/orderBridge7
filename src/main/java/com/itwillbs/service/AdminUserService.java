package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.AdminUserVO;

public interface AdminUserService {
	
    // 로그인 처리 
    AdminUserVO login(AdminUserVO vo);
    
    // 관리자 아이디로 정보 조회 (로그인용)
    public AdminUserVO findByAdminId(String adminId); 

    // 관리자 목록 조회 (검색 조건 포함)
    List<AdminUserVO> getAdminList(String search, String role, String status);


    // 관리자 등록
    void insertAdmin(AdminUserVO adminVO);

    // 관리자 정보 수정 (최고관리자용)
    void updateAdmin(AdminUserVO adminVO);

    
    // 관리자 삭제
    void deleteAdmin(String adminId);


    // 내 정보 수정 (일반 관리자용)
    void updateMyInfo(AdminUserVO adminVO);
}