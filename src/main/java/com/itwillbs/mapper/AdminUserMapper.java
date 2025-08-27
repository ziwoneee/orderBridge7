package com.itwillbs.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.itwillbs.domain.AdminUserVO;
import com.itwillbs.domain.SearchCriteria;

@Mapper
public interface AdminUserMapper {
    
    // ==================== 기존 메서드들 ====================
    
    // [로그인용 단건 조회] - 아이디만 조회 (비밀번호 비교는 Service에서 bcrypt로)
    AdminUserVO findByAdminId(String adminId);
    
    // [등록] 운영자가 수동 생성 (Service에서 비번 bcrypt 후 넣기)
    void insertAdmin(AdminUserVO vo);
    String getMaxAdminIdByYear(String year);
    
    // [로그인 실패] 실패횟수 +1, 5회 도달 시 즉시 LOCK (XML에서 한 번에 처리)
    void increaseFailCount(String adminId);
    
    // [로그인 성공] 실패횟수 0으로 초기화
    void resetFailCount(String adminId);
    
    // [잠금 해제] SUPER만 사용: status=ACTIVE, fail_count=0
    void unlockAccount(String adminId);
    
    // [수정] 이름/전화/권한/상태 수정 (기존 메서드)
    void updateAdminUser(AdminUserVO vo);
    
    // [비밀번호 변경] bcrypt로 인코딩된 값으로 교체 (기존 메서드)
    void updatePassword(@Param("adminId") String adminId,
                        @Param("encodedPassword") String encodedPassword);
    
    // ==================== 설정 페이지용 메서드들 ====================
    
    /**
     * 관리자 목록 조회 (기존 - 검색 조건 포함)
     * @param search 검색어 (사번, 이름)
     * @param role 역할 필터 (SUPER, PROD, SALES, MATERIAL)
     * @param status 상태 필터 (ACTIVE, INACTIVE, LOCKED)
     * @return 관리자 목록
     */
    List<AdminUserVO> getAdminList(@Param("search") String search, 
                                   @Param("role") String role, 
                                   @Param("status") String status);
    
    /**
     * 관리자 정보 수정 (최고관리자용)
     * - 이름, 전화번호, 비밀번호, 역할, 상태 수정 가능
     * - 비밀번호가 null이면 업데이트하지 않음
     */
    void updateAdmin(AdminUserVO vo);
    
    /**
     * 관리자 소프트 삭제
     * - 상태를 DELETED로 변경
     */
    void softDeleteAdmin(String adminId);
    
    /**
     * 내 정보 수정 (일반 관리자용)
     * - 이름, 전화번호, 비밀번호만 수정 가능
     * - 역할이나 상태는 변경 불가
     */
    void updateMyInfo(AdminUserVO vo);
    
    /**
     * 전화번호 중복 확인
     * @param phone 확인할 전화번호
     * @param currentAdminId 현재 관리자 ID (본인 제외용)
     * @return 중복 개수
     */
    int checkPhoneDuplicate(@Param("phone") String phone, 
                           @Param("currentAdminId") String currentAdminId);
    
    // ==================== 페이징 처리 관련 메서드들 (추가) ====================
    
    /**
     * 관리자 목록 조회 (페이징 적용)
     * @param cri 검색 조건 및 페이징 정보
     * @return 관리자 목록
     */
    List<AdminUserVO> getAdminListWithPaging(SearchCriteria cri);
    
    /**
     * 관리자 총 개수 조회 (페이징용)
     * @param cri 검색 조건
     * @return 총 개수
     */
    int getAdminCount(SearchCriteria cri);
}