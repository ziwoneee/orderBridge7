package com.itwillbs.persistence;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ClientOrderDetailVO;

@Repository
public class ClientOrderDetailDAOImpl implements ClientOrderDetailDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ClientOrderDetailMapper";

    @Override
    public void insertDetail(ClientOrderDetailVO detailVO) {
        sqlSession.insert(NAMESPACE + ".insertDetail", detailVO);
    }

    @Override
    public List<ClientOrderDetailVO> getDetailListByOrderId(String clOrderId) {
        return sqlSession.selectList(NAMESPACE + ".getDetailListByOrderId", clOrderId);
    }
    

    // ✅ 수주 상세 전체 삭제 (by 수주ID)
    @Override
    public void deleteDetailsByOrderId(String clOrderId) {
        sqlSession.delete(NAMESPACE + ".deleteDetailsByOrderId", clOrderId);
    }
}


