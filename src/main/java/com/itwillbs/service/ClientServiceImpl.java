package com.itwillbs.service;

import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.persistence.ClientDAO;

@Service
public class ClientServiceImpl implements ClientService {

    @Inject
    private ClientDAO clientDao;

    @Override
    public List<ClientVO> getClientList(SearchCriteria cri) {
        return clientDao.getClientList(cri);
    }

    @Override
    public int getClientCount(SearchCriteria cri) {
        return clientDao.getClientCount(cri);
    }
    
    @Override
    public void registerClient(ClientVO client) {
        clientDao.insertClient(client);
    }
    
    //고객사 단건 조회
    @Override
    public ClientVO getClientById(String clientId) {
        return clientDao.selectClientById(clientId);
    }
    
    //고객사 목록조회(수주등록용)
    
    @Override
    public List<ClientVO> getAllClients() {
        return clientDao.getAllClients();
    }
    
    //고객사 수정
    @Override
    public void updateClient(ClientVO client) {
        clientDao.updateClient(client);
    }

    //고객사 활성화 목록
    @Override
    public List<ClientVO> getActiveClients() {
        return clientDao.getActiveClients();
    }

    
    
}
