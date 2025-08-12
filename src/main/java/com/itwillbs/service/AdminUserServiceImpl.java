package com.itwillbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.mapper.AdminUserMapper;
import com.itwillbs.util.PasswordEncoderUtil;

/**
 * 관리자 계정 서비스 구현체
 * - 로그인 / 단건 조회 / 계정 등록 / 계정 관리
 * - 비밀번호는 BCrypt로 비교/저장
 * - 로그인 실패 5회 도달 시 Mapper SQL에서 자동으로 LOCK 처리
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    /**
     * 로그인 처리
     * 1) 아이디로 사용자 조회
     * 2) 잠금(LOCKED)이면 바로 실패
     * 3) BCrypt로 비밀번호 비교
     *    - 성공: 실패횟수 0으로 초기화 후 사용자 VO 반환
     *    - 실패: 실패횟수 +1 (5회 도달 시 SQL에서 자동 LOCK) 후 null 반환
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
            // (여기서 5회에 도달하면 SQL에서 자동으로 status='LOCKED'로 변경됨)
            adminUserMapper.increaseFailCount(dbVO.getAdminId());
            return null;
        }
    }
    
    /**
     * 아이디로 단건 조회 (로그인/수정용 공통)
     */
    @Override
    public AdminUserVO findByAdminId(String adminId) {
        return adminUserMapper.findByAdminId(adminId);
    }
    
    /**
     * 관리자 등록
     * - 비밀번호는 반드시 BCrypt로 인코딩해서 저장
     * - fail_count(0), status('ACTIVE')는 DB 기본값 사용 → 코드에서 별도 세팅 불필요
     */
    @Override
    public void insertAdmin(AdminUserVO vo) {
        // 1) 원본 비밀번호 → BCrypt 인코딩
        String encodedPw = PasswordEncoderUtil.encode(vo.getPassword());
        vo.setPassword(encodedPw);
        // 2) DB 기본값을 믿고 그대로 insert
        //    (admin_user.fail_count INT DEFAULT 0, status VARCHAR(20) DEFAULT 'ACTIVE')
        adminUserMapper.insertAdmin(vo);
    }
    
    /**
     * 관리자 목록 조회 (검색 조건 포함)
     * - 최고관리자만 사용
     * - 검색어, 역할, 상태별 필터링 지원
     */
    @Override
    public List<AdminUserVO> getAdminList(String search, String role, String status) {
        return adminUserMapper.getAdminList(search, role, status);
    }
    
    /**
     * 관리자 정보 수정 (최고관리자용)
     * - 비밀번호가 있는 경우에만 암호화해서 업데이트
     * - 이름, 전화번호, 역할, 상태 수정 가능
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
     * 관리자 삭제
     * - 물리 삭제 또는 상태를 DELETED로 변경 (정책에 따라)
     */
    @Override
    public void deleteAdmin(String adminId) {
        adminUserMapper.deleteAdmin(adminId);
    }
    
    /**
     * 내 정보 수정 (일반 관리자용)
     * - 본인의 이름, 전화번호, 비밀번호만 수정 가능
     * - 역할이나 상태는 변경 불가
     */
    @Override
    public void updateMyInfo(AdminUserVO vo) {
        // 비밀번호가 입력된 경우에만 암호화
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            String encodedPw = PasswordEncoderUtil.encode(vo.getPassword());
            vo.setPassword(encodedPw);
        } else {
            // 비밀번호가 비어있으면 null로 설정 (SQL에서 업데이트 제외)
            vo.setPassword(null);
        }
        
        adminUserMapper.updateMyInfo(vo);
    }
}