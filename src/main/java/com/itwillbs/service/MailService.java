package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.ClientOrderDetailVO;

public interface MailService {
	void sendMail(String to, String subject, String body);

	void sendOrderRegisteredMail(String to, String clientName, String clOrderId, String deliveryDate,
            List<ClientOrderDetailVO> detailList);

}
