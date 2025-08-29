package com.itwillbs.service;

import com.itwillbs.dto.PredictionInputDTO;
import com.itwillbs.dto.PredictionResultDTO;

public interface AiPredictionService {
	
	PredictionResultDTO predictEtaForWorkOrder(String workOrderId) throws Exception;
	PredictionResultDTO predictEtaForWorkOrder(String workOrderId, String requestedBy) throws Exception;
	PredictionResultDTO predict(PredictionInputDTO in);
	

}
