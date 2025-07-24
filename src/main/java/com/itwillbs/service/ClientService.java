package com.itwillbs.service;

import java.util.List;
import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.SearchCriteria;

public interface ClientService {
    List<ClientVO> getClientList(SearchCriteria cri);  //고객사 목록 조회
    int getClientCount(SearchCriteria cri);
    
    void registerClient(ClientVO client); // 고객사 등록
    
    ClientVO getClientById(String clientId); // 고객사 단건 조회
    
    List<ClientVO> getAllClients();//고객사 목록조회(수주등록용)

    void updateClient(ClientVO client); //고객사 수정


}
