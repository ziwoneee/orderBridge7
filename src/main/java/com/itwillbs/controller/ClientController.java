package com.itwillbs.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.service.ClientService;

@Controller
public class ClientController {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Inject
    private ClientService clientService;
    
    //고객사 목록 보기

    @GetMapping("/client/list")
    public String clientList(SearchCriteria cri, Model model) {

    	// 🔁 JSP에서 사용하는 camelCase 기준으로 수정
    	List<String> allowed = List.of("clientName", "businessNumber", "createdAt", "statusCode");

        if (cri.getSortColumn() == null || !allowed.contains(cri.getSortColumn())) {
            cri.setSortColumn("client_name");
        }
        if (!"asc".equalsIgnoreCase(cri.getSortOrder()) && !"desc".equalsIgnoreCase(cri.getSortOrder())) {
            cri.setSortOrder("desc");
        }

        List<ClientVO> clientList = clientService.getClientList(cri);
        int totalCount = clientService.getClientCount(cri);

        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("clientList", clientList);
        model.addAttribute("cri", cri);
        model.addAttribute("pageMaker", pageMaker);  

        return "client/list";
    }

                
    
    
    //고객사 신규 등록하기
        @GetMapping("/client/register")
        public String registerForm(Model model) {
            model.addAttribute("client", new ClientVO());
            return "client/register"; // → /WEB-INF/views/client/register.jsp
        }

        @PostMapping("/client/register")
        public String registerPost(ClientVO client, Model model) {
            try {
                // 고객사 ID 생성
                String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
                String uuidPart = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 3); 
                String clientId = "CLIENT-" + datePart + uuidPart;

                client.setClientId(clientId);
                
                // 등록 시도
                clientService.registerClient(client);
                return "redirect:/client/list";

            } catch (org.springframework.dao.DuplicateKeyException e) {
                logger.warn("중복된 사업자등록번호: {}", client.getBusinessNumber());

                model.addAttribute("error", "중복된 사업자등록번호입니다. 다시 확인해주세요.");
                model.addAttribute("client", client);  // 기존 입력값 유지
                return "client/register"; // ★ 리다이렉트(X) → 뷰 포워딩(O)

            } catch (Exception e) {
                logger.error("고객사 등록 중 오류 발생", e);

                model.addAttribute("error", "등록 중 오류가 발생했습니다. 관리자에게 문의하세요.");
                model.addAttribute("client", client); // 기존 입력값 유지
                return "client/register";
            }
        }

    
        
     // 상세보기 이동
        @GetMapping("/client/detail")
        public String clientDetail(@RequestParam("clientId") String clientId, Model model) {
            ClientVO client = clientService.getClientById(clientId);
            model.addAttribute("client", client);
            return "client/detail"; // 예: /WEB-INF/views/client/client_detail.jsp
        }

     // ✅ 수정 폼 이동 처리
        @GetMapping("/client/edit")
        public String editClientForm(@RequestParam("clientId") String clientId, Model model) {
            ClientVO client = clientService.getClientById(clientId);
            model.addAttribute("client", client);
            return "client/edit"; // /WEB-INF/views/client/edit.jsp
        }
   
        
     // 고객사 수정 처리
        @PostMapping("/client/update")
        public String updateClient(ClientVO client, RedirectAttributes rttr) {
            try {
                clientService.updateClient(client);
                rttr.addFlashAttribute("success", "수정이 완료되었습니다.");
                return "redirect:/client/detail?clientId=" + client.getClientId();
            } catch (Exception e) {
                logger.error("고객사 수정 중 오류 발생", e);
                rttr.addFlashAttribute("error", "수정 중 오류가 발생했습니다.");
                return "redirect:/client/edit?clientId=" + client.getClientId();
            }
        }


        
        
        
}
