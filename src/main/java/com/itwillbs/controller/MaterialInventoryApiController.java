package com.itwillbs.controller;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.service.MaterialOutboundService;

@Controller
@RequestMapping("/material/inventory")
public class MaterialInventoryApiController {

 @Inject
 private MaterialOutboundService moService; // 혹은 별도 InventoryService가 있으면 그걸 사용

 @RequestMapping(value="/lots", method=RequestMethod.GET)
 public List<Map<String,Object>> lots(@RequestParam("materialId") String materialId) throws Exception {
    
     return moService.getLotsByMaterial(materialId);
 
 }
}
