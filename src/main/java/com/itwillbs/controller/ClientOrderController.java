package com.itwillbs.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.ClientOrderVO;
import com.itwillbs.domain.ClientVO;
import com.itwillbs.domain.PageMaker;
import com.itwillbs.domain.ProductVO;
import com.itwillbs.domain.SearchCriteria;
import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.service.ClientService;
import com.itwillbs.service.ClientOrderDetailService;
import com.itwillbs.service.ClientOrderService;
import com.itwillbs.service.ProductService;

@Controller
@RequestMapping("/clientorder")
public class ClientOrderController {

    @Autowired
    private ClientOrderService clientOrderService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private ClientOrderDetailService clientOrderDetailService;
    
    
    // 거래처 리스트 (자동완성/드롭다운용)
    @GetMapping("/clients")
    @ResponseBody
    public List<ClientVO> getClientList() {
        return clientService.getAllClients();
    }

    // 제품 리스트 (드롭다운용)
    @GetMapping("/products")
    @ResponseBody
    public List<ProductVO> getProductList() {    	
    	// DB에서 제품 목록을 가져오는 서비스 로직 호출
        List<ProductVO> productList = productService.getAllProducts();

        // Java 리스트를 반환하면 @ResponseBody가 JSON 배열로 자동 변환하여 응답합니다.
        return productList;
    }
    
    // 수주 등록 폼
    @GetMapping("/register")
    public String registerForm(Model model) {
        List<ProductVO> productList = productService.getAllProducts();
        model.addAttribute("productList", productList);
        return "clientOrder/register"; // JSP 위치: /WEB-INF/views/clientOrder/register.jsp
    }

    // *** 수주 저장(마스터 + 상세 다건) ***
    @PostMapping("/register")
    public String registerOrder(
        ClientOrderVO orderVO,
        @RequestParam(value = "productId", required = false) List<String> productIdList,
        @RequestParam(value = "orderQty", required = false) List<Integer> orderQtyList,
        @RequestParam(value = "unitPrice", required = false) List<Integer> unitPriceList,
        @RequestParam(value = "detailMemo", required = false) List<String> detailMemoList,
        HttpSession session,
        Model model
    ) {
        // 1. 제품이 선택되지 않았을 경우 → 등록 폼으로 되돌아감
    	if (productIdList == null || productIdList.isEmpty()) {
    	    model.addAttribute("errorMessage", "최소 1개 이상의 제품을 선택해야 합니다.");
    	    model.addAttribute("productList", productService.getAllProducts());
    	    model.addAttribute("orderVO", orderVO); // 🔥 사용자 입력값 다시 넘겨줌
    	    return "clientOrder/register";
    	}


        // 2. adminId 세팅
        String adminId = (String) session.getAttribute("adminId");
        if (adminId == null) adminId = "ADMIN_TEST";
        orderVO.setAdminId(adminId);

        // 3. 총금액 계산
        int totalPrice = 0;
        for (int i = 0; i < productIdList.size(); i++) {
            int unitPrice = (unitPriceList != null && unitPriceList.size() > i) ? unitPriceList.get(i) : 0;
            int qty = (orderQtyList != null && orderQtyList.size() > i) ? orderQtyList.get(i) : 0;
            totalPrice += unitPrice * qty;
        }
        orderVO.setOrderTotalPrice(totalPrice);

        // 4. 마스터 등록
        clientOrderService.registerOrder(orderVO);

        // 5. 상세 등록
        for (int i = 0; i < productIdList.size(); i++) {
            ClientOrderDetailVO detail = new ClientOrderDetailVO();
            detail.setClOrderId(orderVO.getClOrderId());
            detail.setProductId(productIdList.get(i));
            detail.setOrderQty(orderQtyList.get(i));
            if (unitPriceList != null && unitPriceList.size() > i)
                detail.setUnitPrice(unitPriceList.get(i));
            if (detailMemoList != null && detailMemoList.size() > i)
                detail.setDetailMemo(detailMemoList.get(i));
            clientOrderService.registerOrderDetail(detail);
        }

        return "redirect:/clientorder/list";
    }


    // 수주 목록 페이지 (검색, 정렬, 페이징, 기간 필터 모두 반영)
    @GetMapping("/list")
    public String orderList(@ModelAttribute("cri") SearchCriteria cri, Model model) {
        // 정렬 컬럼/방향 기본값 (허용 컬럼만)
        if (cri.getSortColumn() == null ||
           (!"cl_order_date".equals(cri.getSortColumn())
         && !"client_name".equals(cri.getSortColumn())
         && !"product_name".equals(cri.getSortColumn()))) {
            cri.setSortColumn("cl_order_date");
        }
        if (cri.getSortOrder() == null ||
           (!"asc".equalsIgnoreCase(cri.getSortOrder())
         && !"desc".equalsIgnoreCase(cri.getSortOrder()))) {
            cri.setSortOrder("desc");
        }
        if (cri.getPerPageNum() <= 0) cri.setPerPageNum(10);

        int totalCount = clientOrderService.getOrderCount(cri);
        List<ClientOrderVO> orderList = clientOrderService.getOrderList(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("orderList", orderList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        return "clientOrder/orderList";
    }
    
    
    //수주상태변경
    @PostMapping("/updateStatus")
    public String updateOrderStatus(
        @RequestParam("orderIds") String orderIds,  // "id1,id2,id3"
        @RequestParam("newStatus") String newStatus
    ) {
        if(orderIds != null && !orderIds.isEmpty() && newStatus != null && !newStatus.isEmpty()) {
            String[] ids = orderIds.split(",");
            clientOrderService.bulkUpdateStatus(ids, newStatus);
        }
        return "redirect:/clientorder/list";
    }
    
    // 수주상세보기 화면
    @GetMapping("/detail")
    public String orderDetail(@RequestParam("clOrderId") String clOrderId, Model model) {
        // 마스터 정보
        ClientOrderVO order = clientOrderService.getOrderById(clOrderId);
        // 상세(제품별) 리스트
        List<ClientOrderDetailVO> detailList = clientOrderDetailService.getDetailListByOrderId(clOrderId);

        model.addAttribute("order", order);
        model.addAttribute("detailList", detailList);

        return "clientOrder/orderDetail"; // JSP 경로: /WEB-INF/views/clientOrder/orderDetail.jsp
    }
    
    //수주 입금확인-상태변경
    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        // 상태 업데이트 (CONFIRMED)
        clientOrderService.updateOrderStatus(clOrderId, "CONFIRMED");

        // 알림 메시지
        rttr.addFlashAttribute("message", "입금이 확인되어 주문이 확정되었습니다.");

        // 상세페이지로 리다이렉트
        return "redirect:/clientorder/detail?clOrderId=" + clOrderId;
    }


    

}