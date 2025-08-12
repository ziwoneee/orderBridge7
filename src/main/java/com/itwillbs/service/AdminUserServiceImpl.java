package com.itwillbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.mapper.AdminUserMapper;
import com.itwillbs.util.PasswordEncoderUtil;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    /**
     * 로그인 처리
     */
    @Override
    public AdminUserVO login(AdminUserVO inputVO) {
        // 1) 아이디로 DB 조회
        AdminUserVO dbVO = adminUserMapper.findByAdminId(inputVO.getAdminId());
        if (dbVO == null) {
            return null; // 아이디 없음
        }
        // 2) 이미 잠금 상태면 바로 로그인 불가
        if ("LOCKED".equals(dbVO.getStatus())) {
            return null;
        }
        // 3) 비밀번호 비교 (BCrypt)
        boolean matched = PasswordEncoderUtil.matches(inputVO.getPassword(), dbVO.getPassword());
        if (matched) {
            // 로그인 성공 → 실패 카운트 초기화
            adminUserMapper.resetFailCount(dbVO.getAdminId());
            return dbVO;
        } else {
            // 로그인 실패 → 실패 카운트 +1
            adminUserMapper.increaseFailCount(dbVO.getAdminId());
            return null;
        }
    }
    
    /**
     * 아이디로 단건 조회
     */
    @Override
    public AdminUserVO findByAdminId(String adminId) {
        return adminUserMapper.findByAdminId(adminId);
    }
    
    /**
     * 관리자 등록
     */
    @Override
    public void insertAdmin(AdminUserVO vo) {
        // 비밀번호 BCrypt 인코딩
        String encodedPw = PasswordEncoderUtil.encode(vo.getPassword());
        vo.setPassword(encodedPw);
        adminUserMapper.insertAdmin(vo);
    }
    
    /**
     * 관리자 정보 수정 (최고관리자용)
     */
    @Override
    public void updateAdmin(AdminUserVO vo) {
        // 비밀번호가 입력된 경우에만 암호화
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            String encodedPw = PasswordEncoderUtil.encode(vo.getPassword());
            vo.setPassword(encodedPw);
        } else {
            // 비밀번호가 비어있으면 null로 설정 (SQL에서 업데이트 제외)
            vo.setPassword(null);
        }
        adminUserMapper.updateAdmin(vo);
    }
    
    /**
     * 내 정보 수정 (일반 관리자용)
     */
    @Override
    public void updateMyInfo(AdminUserVO vo) {
        // 비밀번호가 입력된 경우에만 암호화
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            String encodedPw = PasswordEncoderUtil.encode(vo.getPassword());
            vo.setPassword(encodedPw);
        } else {
            vo.setPassword(null);
        }
        adminUserMapper.updateMyInfo(vo);
    }
    
    /**
     * 소프트 삭제
     */
    @Override
    public void softDeleteAdmin(String adminId) {
        adminUserMapper.softDeleteAdmin(adminId);
    }
    
    /**
     * 계정 잠금 해제
     */
    @Override
    public void unlockAccount(String adminId) {
        adminUserMapper.unlockAccount(adminId);
    }
    
    /**
     * 전화번호 중복 확인
     */
    @Override
    public boolean isPhoneDuplicate(String phone, String currentAdminId) {
        return adminUserMapper.checkPhoneDuplicate(phone, currentAdminId) > 0;
    }
    
    // ========== 페이징 처리 관련 메서드 ==========
    
    /**
     * 관리자 목록 조회 (기존 - 단순 검색)
     */
    @Override
    public List<AdminUserVO> getAdminList(String search, String role, String status) {
        return adminUserMapper.getAdminList(search, role, status);
    }
    
    /**
     * 관리자 목록 조회 (페이징 적용)
     */
    @Override
    public List<AdminUserVO> getAdminListWithPaging(SearchCriteria cri) {
        return adminUserMapper.getAdminListWithPaging(cri);
    }
    
    /**
     * 관리자 총 개수 조회 (페이징용)
     */
    @Override
    public int getAdminCount(SearchCriteria cri) {
        return adminUserMapper.getAdminCount(cri);
    }
}