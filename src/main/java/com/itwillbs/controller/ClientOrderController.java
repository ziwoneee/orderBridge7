package com.itwillbs.controller;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.lowagie.text.Document;


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
import com.itwillbs.dto.DeliveryHistoryDTO;
import com.itwillbs.domain.ClientDeliveryVO;
import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.service.ClientService;
import com.itwillbs.service.ClientDeliveryService;
import com.itwillbs.service.ClientOrderDetailService;
import com.itwillbs.service.ClientOrderService;
import com.itwillbs.service.ProductService;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;


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
    
    
    @Autowired
    private ClientDeliveryService deliveryService;
   
    
    // 거래처 리스트 (자동완성/드롭다운용)
    @GetMapping("/clients")
    @ResponseBody
    public List<ClientVO> getClientList() {
        return clientService.getActiveClients();  // ✅ 활성 고객사만

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
        model.addAttribute("menu", "sales");
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
    	    model.addAttribute("menu", "sales");
    	    return "clientOrder/register";
    	}

    	  // ✅ 로그인한 관리자 ID 가져오기
        String adminId = (String) session.getAttribute("adminId");
        String adminName = (String) session.getAttribute("adminName");
        

        if (adminId == null) {
            // 로그인되지 않은 경우 → 로그인 페이지로 리다이렉트
            return "redirect:/admin/login";
        }
        
        orderVO.setAdminId(adminId);
        orderVO.setAdminName(adminName);

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

        // ✅ 허용된 정렬 컬럼 화이트리스트
        List<String> allowedSortColumns = Arrays.asList(
            "cl_order_num",     // 수주번호
            "client_name",      // 거래처명
            "cl_order_date",    // 수주일자
            "cl_delivery_date", // 납기요청일
            "cl_order_status"   // 수주상태
        );

        // ✅ 정렬 컬럼 유효성 검사 + 기본값
        if (cri.getSortColumn() == null || !allowedSortColumns.contains(cri.getSortColumn())) {
            cri.setSortColumn("cl_order_date");
        }

        // ✅ 정렬 방향 소문자 표준화 + 기본값(desc)
        if (cri.getSortOrder() == null) cri.setSortOrder("desc");
        String order = cri.getSortOrder().toLowerCase();
        if (!order.equals("asc") && !order.equals("desc")) order = "desc";
        cri.setSortOrder(order);

        if (cri.getPerPageNum() <= 0) cri.setPerPageNum(10);

        int totalCount = clientOrderService.getOrderCount(cri);

        // 상태별 건수
        int allCount = clientOrderService.countAllOrders();
        int requestedCount = clientOrderService.countOrdersByStatus("REQUESTED");
        int confirmedCount = clientOrderService.countOrdersByStatus("CONFIRMED");
        int shippedCount = clientOrderService.countOrdersByStatus("SHIPPED");
        int cancelledCount = clientOrderService.countOrdersByStatus("CANCELLED");

        // 목록 조회
        List<ClientOrderVO> orderList = clientOrderService.getOrderList(cri);
        PageMaker pageMaker = new PageMaker(cri, totalCount);

        model.addAttribute("orderList", orderList);
        model.addAttribute("pageMaker", pageMaker);
        model.addAttribute("cri", cri);
        model.addAttribute("menu", "sales");

        model.addAttribute("totalCount", allCount);
        model.addAttribute("requestedCount", requestedCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippedCount", shippedCount);
        model.addAttribute("cancelledCount", cancelledCount);

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

        // ✅ 총 금액 계산
        int totalPrice = clientOrderDetailService.calculateTotalPrice(detailList);

        model.addAttribute("order", order);
        model.addAttribute("detailList", detailList);
        model.addAttribute("totalPrice", totalPrice); // ✅ JSP에서 출력 가능
        model.addAttribute("deliveryHistory", deliveryService.getDeliveriesByOrderId(clOrderId));
        model.addAttribute("menu", "sales");
        
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

 // 수주 삭제
    @PostMapping("/delete")
    public String deleteOrder(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        clientOrderService.deleteOrder(clOrderId); // 상태만 'CANCELLED'로 변경
        rttr.addFlashAttribute("message", "수주가 취소되었습니다.");
        return "redirect:/clientorder/list";
    }
    
    //PDF 다운로드 파일 생성
    @GetMapping("/pdf/{clOrderId}")
    public void downloadOrderPdf(@PathVariable("clOrderId") String clOrderId,
                                  HttpServletResponse response, HttpServletRequest request) throws Exception {

        // 1. 수주 마스터 정보 조회
        ClientOrderVO order = clientOrderService.getOrderById(clOrderId);
        List<ClientOrderDetailVO> detailList = clientOrderDetailService.getDetailListByOrderId(clOrderId);
        List<DeliveryHistoryDTO> shipmentList = deliveryService.getDeliveriesByOrderId(clOrderId);

        // 2. 응답 헤더 설정
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=order_" + clOrderId + ".pdf");

        // 3. ReportLab 기반 PDF 작성
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

     // ✅ 4. 한글 폰트 설정
        String fontPath = "C:/Windows/Fonts/malgun.ttf"; 
        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(baseFont, 16);
        Font normalFont = new Font(baseFont, 11);
        
     // 3칸짜리 테이블 생성 (로고 / 제목 / 회사정보)
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{3, 4, 3}); // 비율 (원하는 대로 조정 가능)

        // 로고 셀
        String logoPath = request.getServletContext().getRealPath("/resources/images/logo.png");
        Image logo = Image.getInstance(logoPath);
        logo.scaleToFit(100, 50);
        PdfPCell logoCell = new PdfPCell(logo, true);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // 제목 셀
        Paragraph docTitle = new Paragraph("수주 내역서", titleFont);
        PdfPCell titleCell = new PdfPCell(docTitle);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // 회사정보 셀
        Paragraph companyInfo = new Paragraph("(주)오더브릿지\n부산 해운대구 센텀서로 30\nTEL: 051-123-4567", normalFont);
        PdfPCell infoCell = new PdfPCell(companyInfo);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // 테이블에 추가
        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        headerTable.addCell(infoCell);

        // 문서에 추가
        document.add(headerTable);
        document.add(Chunk.NEWLINE); // 밑에 여백


        // ✅ 섹션 제목
//        Paragraph sectionTitle = new Paragraph("수주 상세정보", titleFont);
//        sectionTitle.setAlignment(Element.ALIGN_CENTER);
//        document.add(sectionTitle);
//        document.add(Chunk.NEWLINE);

        // 수주 기본 정보 테이블
        PdfPTable masterTable = new PdfPTable(4);
        masterTable.setWidths(new int[]{1, 2, 1, 2});
        masterTable.setWidthPercentage(100);

        addCell(masterTable, "수주번호", normalFont);
        addCell(masterTable, order.getClOrderId(), normalFont);
        addCell(masterTable, "거래처명", normalFont);
        addCell(masterTable, order.getClientName(), normalFont);

        addCell(masterTable, "담당자", normalFont);
        addCell(masterTable, order.getManagerName(), normalFont);
        addCell(masterTable, "전화번호", normalFont);
        addCell(masterTable, order.getManagerTel(), normalFont);

        addCell(masterTable, "배송주소", normalFont);
        addCell(masterTable, order.getDeliveryAddress(), normalFont);
        addCell(masterTable, "우편번호", normalFont);
        addCell(masterTable, order.getPostCode(), normalFont);

        addCell(masterTable, "수주일자", normalFont);
        addCell(masterTable, order.getClOrderDate().toString(), normalFont);
        addCell(masterTable, "납기요청일", normalFont);
        addCell(masterTable, order.getClDeliveryDate().toString(), normalFont);

        addCell(masterTable, "수주상태", normalFont);
        addCell(masterTable, order.getClOrderStatus(), normalFont);
        addCell(masterTable, "메모", normalFont);
        addCell(masterTable, (order.getClOrderMemo() != null ? order.getClOrderMemo() : "-"), normalFont);

        document.add(masterTable);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("※ 수주담당자: " + order.getAdminName(), normalFont));
        document.add(Chunk.NEWLINE);

        // 수주 상세 테이블
        document.add(new Paragraph("수주 상세 내역", normalFont));
        PdfPTable detailTable = new PdfPTable(6);
        detailTable.setWidths(new int[]{1, 3, 2, 2, 2, 2});
        detailTable.setWidthPercentage(100);
        Stream.of("No", "제품명", "수량", "단가", "합계", "비고").forEach(h -> addCell(detailTable, h, normalFont, true));

        int idx = 1;
        int total = 0;
        for (ClientOrderDetailVO d : detailList) {
            int price = d.getOrderQty() * d.getUnitPrice();
            total += price;
            detailTable.addCell(String.valueOf(idx++));
            detailTable.addCell(new PdfPCell(new Phrase(
            	    d.getProductName() != null ? d.getProductName() : "-", normalFont)));
            System.out.println("상품명: " + d.getProductName());

            detailTable.addCell(String.valueOf(d.getOrderQty()));
            detailTable.addCell(String.format("%,d", d.getUnitPrice()));
            detailTable.addCell(String.format("%,d", price));
            detailTable.addCell(d.getDetailMemo() != null ? d.getDetailMemo() : "-");
        }
        addEmptyCells(detailTable, 3);
        addCell(detailTable, "총 합계", normalFont);
        addCell(detailTable, String.format("%,d", total), normalFont);
        addCell(detailTable, "", normalFont);
        document.add(detailTable);
        document.add(Chunk.NEWLINE);

        // 출하 이력 테이블
        document.add(new Paragraph("출하 이력", normalFont));
        PdfPTable shipTable = new PdfPTable(7);
        shipTable.setWidths(new int[]{1, 3, 2, 3, 2, 3, 2});
        shipTable.setWidthPercentage(100);
        Stream.of("No", "제품명", "출하수량", "LOT", "출하일자", "송장번호", "출하상태").forEach(h -> addCell(shipTable, h, normalFont, true));

        int sIdx = 1;
        for (DeliveryHistoryDTO s : shipmentList) {
            shipTable.addCell(String.valueOf(sIdx++));
            shipTable.addCell(new PdfPCell(new Phrase(
            	    s.getProductName() != null ? s.getProductName() : "-", normalFont)));
            shipTable.addCell(String.valueOf(s.getDeliveryQty()));
            shipTable.addCell(s.getLotNo());
            shipTable.addCell(s.getDeliveryDate().toString());
            shipTable.addCell(s.getTrackingNumber());

            shipTable.addCell(new Phrase(s.getDeliveryStatus(), normalFont));
        }
        document.add(shipTable);

        document.close();
    }

//PDF생성 필수 요소
 // 일반 셀 추가
    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    // 헤더 셀 추가 (배경색 포함)
    private void addCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        if (isHeader) {
            cell.setBackgroundColor(Color.LIGHT_GRAY); // 또는 BaseColor.LIGHT_GRAY (iText 버전에 따라 다름)
        }
        table.addCell(cell);
    }

    // 빈 셀 여러 개 추가
    private void addEmptyCells(PdfPTable table, int count) {
        for (int i = 0; i < count; i++) {
            table.addCell("");
        }
    }


    

}