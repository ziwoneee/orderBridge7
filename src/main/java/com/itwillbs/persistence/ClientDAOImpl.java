package com.itwillbs.persistence;

import java.util.List;
import javax.inject.Inject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.SearchCriteria;

@Repository
public class ClientDAOImpl implements ClientDAO {

    @Inject
    private SqlSession sqlSession;
    private final String NAMESPACE = "com.itwillbs.mapper.ClientMapper";

    //고객사 목록 조회
    @Override
    public List<ClientVO> getClientList(SearchCriteria cri) {
        return sqlSession.selectList(NAMESPACE + ".getClientList", cri);
    }

    @Override
    public int getClientCount(SearchCriteria cri) {
        return sqlSession.selectOne(NAMESPACE + ".getClientCount", cri);
    }
    
    
    //고객사 신규 등록
    @Override
    public void insertClient(ClientVO client) {
        sqlSession.insert(NAMESPACE + ".insertClient", client);
    }
    
    //고객사 상세 조회
    @Override
    public ClientVO selectClientById(String clientId) {
        return sqlSession.selectOne(NAMESPACE + ".selectClientById", clientId);
    }
    
    //고객사 수주등록용 목록조회
    @Override
    public List<ClientVO> getAllClients() {
        return sqlSession.selectList(NAMESPACE + ".getAllClients");
    }
    //고객사 수정
    @Override
    public void updateClient(ClientVO client) {
        sqlSession.update(NAMESPACE + ".updateClient", client);
    }

    
    
}
