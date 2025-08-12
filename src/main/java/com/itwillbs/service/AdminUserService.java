package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.domain.SearchCriteria;

public interface AdminUserService {
	
    // 로그인 처리 
    AdminUserVO login(AdminUserVO vo);
    
    // 관리자 아이디로 정보 조회 (로그인용)
    AdminUserVO findByAdminId(String adminId); 
    
    // 관리자 등록
    void insertAdmin(AdminUserVO adminVO);
    
    // 관리자 정보 수정 (최고관리자용)
    void updateAdmin(AdminUserVO adminVO);
    
    // 내 정보 수정 (일반 관리자용)
    void updateMyInfo(AdminUserVO adminVO);
    
    // 관리자 삭제 (소프트 삭제) 
    void softDeleteAdmin(String adminId);
    
    // 계정 잠금 해제
    void unlockAccount(String adminId);
    
    // 전화번호 중복 확인
    boolean isPhoneDuplicate(String phone, String currentAdminId);
    
    // ========== 페이징 처리 관련 메서드 ==========
    
    // 관리자 목록 조회 (기존 - 단순 검색)
    List<AdminUserVO> getAdminList(String search, String role, String status);
    
    // 관리자 목록 조회 (페이징 적용)
    List<AdminUserVO> getAdminListWithPaging(SearchCriteria cri);
    
    // 관리자 총 개수 조회 (페이징용)
    int getAdminCount(SearchCriteria cri);
}
