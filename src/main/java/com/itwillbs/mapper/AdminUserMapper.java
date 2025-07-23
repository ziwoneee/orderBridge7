package com.itwillbs.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.itwillbs.domain.AdminUserVO;

@Mapper
public interface AdminUserMapper {

    // 관리자 로그인
    public AdminUserVO login(AdminUserVO vo);
    
    // 관리자 ID로 정보 조회
    AdminUserVO findByAdminId(String adminId);
    
    // 관리자 등록
    void insertAdmin(AdminUserVO vo);
    
    // 비밀번호 틀렸을 때 실패 카운트 증가
    public void increaseFailCount(String adminId);
    
    // 실패 5회 이상이면 계정 잠금
    public void lockAccount(String adminId);
    
    // 로그인 성공 시 실패 카운트 초기화
    public void resetFailCount(String adminId);
}