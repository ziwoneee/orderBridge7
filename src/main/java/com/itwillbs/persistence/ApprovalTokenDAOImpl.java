package com.itwillbs.persistence;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.itwillbs.domain.ApprovalTokenVO;

@Repository
public class ApprovalTokenDAOImpl implements ApprovalTokenDAO {

    @Autowired
    private SqlSession sqlSession;

    private static final String NAMESPACE = "com.itwillbs.mapper.ApprovalTokenMapper";

    @Override
    public void insert(ApprovalTokenVO token) {
        sqlSession.insert(NAMESPACE + ".insertToken", token);
    }

    @Override
    public ApprovalTokenVO findByTokenId(String tokenId) {
        return sqlSession.selectOne(NAMESPACE + ".findByTokenId", tokenId);
    }

    @Override
    public void markTokenUsed(String tokenId) {
        sqlSession.update(NAMESPACE + ".markTokenUsed", tokenId);
    }
}
