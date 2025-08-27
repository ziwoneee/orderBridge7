package com.itwillbs.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger log = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    /** DB(increaseFailCount XML)의 5와 반드시 동일해야 함 */
    private static final int MAX_FAIL = 5;
    
    /**
     * 로그인 처리
     */
    @Override
    public AdminUserVO login(AdminUserVO inputVO) {
        // 1) 사용자 조회
        AdminUserVO dbVO = adminUserMapper.findByAdminId(inputVO.getAdminId());
        if (dbVO == null) return null;

        // 2) ACTIVE이면서 잠기지 않은 상태만 로그인 허용
        String st = dbVO.getStatus();
        Boolean locked = dbVO.getIsLocked();
        
        if (!"ACTIVE".equals(st)) {
            log.warn("Login blocked (status={}): adminId={}", st, inputVO.getAdminId());
            return null;
        }
        
        if (Boolean.TRUE.equals(locked)) {
            log.warn("Login blocked (locked=true): adminId={}", inputVO.getAdminId());
            return null;
        }

        // 3) 비밀번호 비교 (DB 비번 null이면 실패)
        String rawPw = inputVO.getPassword();
        String encPw = dbVO.getPassword();
        boolean matched = (encPw != null) && PasswordEncoderUtil.matches(rawPw, encPw);

        if (matched) {
            // 성공: 실패횟수 0, 잠금해제, last_login_at = NOW()
            adminUserMapper.resetFailCount(dbVO.getAdminId());
            AdminUserVO fresh = adminUserMapper.findByAdminId(dbVO.getAdminId());
            log.info("Login success: adminId={}", dbVO.getAdminId());
            return fresh;
        }

        // 4) 실패: 실패횟수 +1, 5회 시 잠금
        adminUserMapper.increaseFailCount(dbVO.getAdminId());

        // 증가 후 상태 확정
        AdminUserVO after = adminUserMapper.findByAdminId(dbVO.getAdminId());
        int fc = (after != null ? after.getFailCount() : 0);
        int remaining = Math.max(0, MAX_FAIL - fc);

        if (after != null && Boolean.TRUE.equals(after.getIsLocked())) {
            log.warn("Account locked (failCount={}): adminId={}", fc, dbVO.getAdminId());
            return null;
        }

        log.info("Login failed: adminId={}", dbVO.getAdminId());
        log.info("failCount={}, remaining={}", fc, remaining); 
        return null;
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
        
        // 기본값 설정
        if (vo.getStatus() == null) vo.setStatus("ACTIVE");
        if (vo.getIsLocked() == null) vo.setIsLocked(false);
        
        adminUserMapper.insertAdmin(vo);
    }
    
    /**
     * 다음 사번 자동 생성 (2025001, 2025002...)
     */
    @Override
    public String generateNextAdminId() {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String maxId = adminUserMapper.getMaxAdminIdByYear(String.valueOf(currentYear));
        
        // admin 같은 문자 ID는 무시하고, 숫자로만 된 7자리 ID 중에서 최대값 찾기
        if (maxId == null || maxId.isEmpty() || !maxId.matches("^\\d{7}$")) {
            // 해당 연도 첫 번째 관리자
            return currentYear + "001";
        }
        
        // 기존 최대값에서 +1
        int nextNum = Integer.parseInt(maxId.substring(4)) + 1;
        return currentYear + String.format("%03d", nextNum);
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
        String norm = phone == null ? null : phone.replaceAll("[^0-9]", "");
        return adminUserMapper.checkPhoneDuplicate(norm, currentAdminId) > 0;
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