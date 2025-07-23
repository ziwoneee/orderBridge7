package com.itwillbs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.mapper.AdminUserMapper;
import com.itwillbs.util.PasswordEncoderUtil;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    // 로그인 처리
    @Override
    public AdminUserVO login(AdminUserVO inputVO) {
        AdminUserVO dbVO = adminUserMapper.findByAdminId(inputVO.getAdminId());

        if (dbVO != null) {
            if ("LOCKED".equals(dbVO.getStatus())) {
                return null; // 계정 잠김
            }

            if (PasswordEncoderUtil.matches(inputVO.getPassword(), dbVO.getPassword())) {
                adminUserMapper.resetFailCount(inputVO.getAdminId()); // 성공 시 실패 카운트 초기화
                return dbVO;
            } else {
                adminUserMapper.increaseFailCount(inputVO.getAdminId()); // 실패 시 카운트 증가
                if (dbVO.getFailCount() + 1 >= 5) {
                    adminUserMapper.lockAccount(inputVO.getAdminId()); // 5회 이상이면 계정 잠금
                }
            }
        }

        return null;
    }

    // 관리자 ID로 정보 조회
    @Override
    public AdminUserVO findByAdminId(String adminId) {
        return adminUserMapper.findByAdminId(adminId);
    }

    // 관리자 등록 (비밀번호 암호화 후 DB 저장)
    @Override
    public void insertAdmin(AdminUserVO vo) {
        String rawPw = vo.getPassword();
        String encodedPw = PasswordEncoderUtil.encode(rawPw); // 암호화
        vo.setPassword(encodedPw);

        //  로그 찍기
        System.out.println("== [ADMIN 등록 로그] ==");
        System.out.println("원본 비밀번호: " + rawPw);
        System.out.println("암호화된 비밀번호: " + encodedPw);
        System.out.println("관리자 ID: " + vo.getAdminId());
        System.out.println("역할 (roleId): " + vo.getRoleId());  // 이게 null이면 문제 발생
        System.out.println("이름: " + vo.getName());
        System.out.println("전화번호: " + vo.getPhone());


        adminUserMapper.insertAdmin(vo);
    }
}
