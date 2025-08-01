package com.itwillbs.persistence;

import java.util.List;
import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.SearchCriteria;

public interface ClientDAO {
	
    List<ClientVO> getClientList(SearchCriteria cri);
    int getClientCount(SearchCriteria cri);
    
    void insertClient(ClientVO client); //고객사 등록 
    
    ClientVO selectClientById(String clientId); //고객사 상세조회
    
    List<ClientVO> getAllClients();//고객사 전체목록 (수주등록용)

    void updateClient(ClientVO client);//고객사 수정

    List<ClientVO> getActiveClients();  // 활성화 상태인 고객 목록
}