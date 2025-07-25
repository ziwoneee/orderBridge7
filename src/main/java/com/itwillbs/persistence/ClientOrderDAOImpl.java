package com.itwillbs.persistence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.SearchCriteria;


@Repository
public class ClientOrderDAOImpl implements ClientOrderDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ClientOrderMapper";

    
    //수주등록
    @Override
    public void insertOrder(ClientOrderVO orderVO) {
        sqlSession.insert(NAMESPACE + ".insertOrder", orderVO);
    }
    //수주번호자동생성
    @Override
    public int getTodayMaxSeq(String today) {
        Integer result = sqlSession.selectOne(NAMESPACE + ".getTodayMaxSeq", today);
        return result != null ? result : 0;
    }

    //수주목록
    @Override
    public List<ClientOrderVO> getOrderList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".getOrderList", cri);
    }
    
   
    @Override
    public int getOrderCount(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".getOrderCount", cri);
    }
    
    //수주상태 변경
    @Override
    public void bulkUpdateStatus(String[] orderIds, String newStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderIds", Arrays.asList(orderIds));
        params.put("newStatus", newStatus);
        sqlSession.update("com.itwillbs.mapper.ClientOrderMapper.bulkUpdateStatus", params);
    }

    //수주 상세 보기
    
    @Override
    public ClientOrderVO getOrderById(String clOrderId) {
        return sqlSession.selectOne(NAMESPACE + ".getOrderById", clOrderId);
    }
    
    
    //입금확인
    @Override
    public void updateOrderStatus(String orderNum, String status) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderNum", orderNum);
        paramMap.put("status", status);
        sqlSession.update(NAMESPACE + ".updateOrderStatus", paramMap);
    }

}
