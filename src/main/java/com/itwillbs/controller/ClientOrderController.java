package com.itwillbs.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

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
import com.itwillbs.persistence.ClientDAO;
import com.itwillbs.domain.ClientOrderDetailVO;
import com.itwillbs.service.ClientService;
import com.itwillbs.service.MailService;
import com.itwillbs.service.ClientDeliveryService;
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

    @Autowired
    private ClientDeliveryService deliveryService;

    @Autowired
    private ClientDAO clientDAO;

    @Autowired
    private MailService mailService;

    // ===== 공통 유틸: 클래스패스 리소스를 byte[]로 (JDK8 호환) =====
    private static byte[] readResourceBytes(String path) throws IOException {
        try (InputStream is = ClientOrderController.class.getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException("리소스를 찾을 수 없습니다: " + path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
            return baos.toByteArray();
        }
    }

    // 거래처 리스트 (자동완성/드롭다운용)
    @GetMapping("/clients")
    @ResponseBody
    public List<ClientVO> getClientList() {
        return clientService.getActiveClients();
    }

    // 제품 리스트 (드롭다운용)
    @GetMapping("/products")
    @ResponseBody
    public List<ProductVO> getProductList() {
        return productService.getAllProducts();
    }

    // 수주 등록 폼
    @GetMapping("/register")
    public String registerForm(Model model) {
        List<ProductVO> productList = productService.getAllProducts();
        model.addAttribute("productList", productList);
        model.addAttribute("menu", "sales");
        return "clientOrder/register";
    }

    // 수주 저장(마스터 + 상세 다건)
    @PostMapping("/register")
    public String registerOrder(
            ClientOrderVO orderVO,
            @RequestParam(value = "productId", required = false) List<String> productIdList,
            @RequestParam(value = "orderQty", required = false) List<Integer> orderQtyList,
            @RequestParam(value = "unitPrice", required = false) List<Integer> unitPriceList,
            @RequestParam(value = "detailMemo", required = false) List<String> detailMemoList,
            HttpSession session, RedirectAttributes rttr, Model model) {

        if (productIdList == null || productIdList.isEmpty()) {
            model.addAttribute("errorMessage", "최소 1개 이상의 제품을 선택해야 합니다.");
            model.addAttribute("productList", productService.getAllProducts());
            model.addAttribute("orderVO", orderVO);
            model.addAttribute("menu", "sales");
            return "clientOrder/register";
        }

        String adminId = (String) session.getAttribute("adminId");
        String adminName = (String) session.getAttribute("adminName");
        if (adminId == null) return "redirect:/admin/login";
        orderVO.setAdminId(adminId);
        orderVO.setAdminName(adminName);

        int totalPrice = 0;
        for (int i = 0; i < productIdList.size(); i++) {
            int unitPrice = (unitPriceList != null && unitPriceList.size() > i) ? unitPriceList.get(i) : 0;
            int qty = (orderQtyList != null && orderQtyList.size() > i) ? orderQtyList.get(i) : 0;
            totalPrice += unitPrice * qty;
        }
        orderVO.setOrderTotalPrice(totalPrice);

        clientOrderService.registerOrder(orderVO);

        for (int i = 0; i < productIdList.size(); i++) {
            ClientOrderDetailVO detail = new ClientOrderDetailVO();
            detail.setClOrderId(orderVO.getClOrderId());
            detail.setProductId(productIdList.get(i));
            detail.setOrderQty(orderQtyList.get(i));
            if (unitPriceList != null && unitPriceList.size() > i) detail.setUnitPrice(unitPriceList.get(i));
            if (detailMemoList != null && detailMemoList.size() > i) detail.setDetailMemo(detailMemoList.get(i));
            clientOrderService.registerOrderDetail(detail);
        }

        try {
            List<ClientOrderDetailVO> detailList =
                    clientOrderService.getOrderDetailList(orderVO.getClOrderId());

            String to = (orderVO.getManagerEmail() != null && !orderVO.getManagerEmail().isEmpty())
                    ? orderVO.getManagerEmail() : clientDAO.findEmailById(orderVO.getClientId());
            String clientName = (orderVO.getClientName() != null && !orderVO.getClientName().isEmpty())
                    ? orderVO.getClientName() : clientDAO.findNameById(orderVO.getClientId());
            String deliveryDate = (orderVO.getClDeliveryDate() != null)
                    ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(orderVO.getClDeliveryDate())
                    : "미정";

            if (to != null && !to.isEmpty()) {
                mailService.sendOrderRegisteredMail(
                        to,
                        (clientName != null && !clientName.isEmpty()) ? clientName : "고객사",
                        orderVO.getClOrderId(),
                        deliveryDate,
                        detailList
                );
                rttr.addFlashAttribute("msg", "수주가 등록되고 고객사에게 안내 메일이 발송되었습니다.");
            } else {
                rttr.addFlashAttribute("msg", "수주가 등록되었지만 고객 이메일이 없어 메일은 생략되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("msg", "수주가 등록되었지만 메일 발송 중 오류가 발생했습니다.");
        }

        return "redirect:/clientorder/list";
    }

    // 수주 목록 페이지 (검색/정렬/페이징/기간)
    @GetMapping("/list")
    public String orderList(@ModelAttribute("cri") SearchCriteria cri, Model model) {
        List<String> allowedSortColumns = Arrays.asList(
                "cl_order_num", "client_name", "cl_order_date", "cl_delivery_date", "cl_order_status");
        if (cri.getSortColumn() == null || !allowedSortColumns.contains(cri.getSortColumn())) {
            cri.setSortColumn("cl_order_date");
        }
        if (cri.getSortOrder() == null) cri.setSortOrder("desc");
        String order = cri.getSortOrder().toLowerCase();
        if (!order.equals("asc") && !order.equals("desc")) order = "desc";
        cri.setSortOrder(order);
        if (cri.getPerPageNum() <= 0) cri.setPerPageNum(10);

        int totalCount = clientOrderService.getOrderCount(cri);
        int allCount = clientOrderService.countAllOrders();
        int requestedCount = clientOrderService.countOrdersByStatus("REQUESTED");
        int confirmedCount = clientOrderService.countOrdersByStatus("CONFIRMED");
        int shippedCount = clientOrderService.countOrdersByStatus("SHIPPED");
        int cancelledCount = clientOrderService.countOrdersByStatus("CANCELLED");

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

    // 수주 상태 변경
    @PostMapping("/updateStatus")
    public String updateOrderStatus(@RequestParam("orderIds") String orderIds,
                                    @RequestParam("newStatus") String newStatus) {
        if (orderIds != null && !orderIds.isEmpty() && newStatus != null && !newStatus.isEmpty()) {
            String[] ids = orderIds.split(",");
            clientOrderService.bulkUpdateStatus(ids, newStatus);
        }
        return "redirect:/clientorder/list";
    }

    // 수주 상세보기
    @GetMapping("/detail")
    public String orderDetail(@RequestParam("clOrderId") String clOrderId, Model model) {
        ClientOrderVO order = clientOrderService.getOrderById(clOrderId);
        List<ClientOrderDetailVO> detailList = clientOrderDetailService.getDetailListByOrderId(clOrderId);
        int totalPrice = clientOrderDetailService.calculateTotalPrice(detailList);

        model.addAttribute("order", order);
        model.addAttribute("detailList", detailList);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryHistory", deliveryService.getDeliveriesByOrderId(clOrderId));
        model.addAttribute("menu", "sales");
        return "clientOrder/orderDetail";
    }

    // 수주 입금확인-상태변경
    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        clientOrderService.updateOrderStatus(clOrderId, "CONFIRMED");
        rttr.addFlashAttribute("message", "입금이 확인되어 주문이 확정되었습니다.");
        return "redirect:/clientorder/detail?clOrderId=" + clOrderId;
    }

    // 수주 삭제(취소)
    @PostMapping("/delete")
    public String deleteOrder(@RequestParam("clOrderId") String clOrderId, RedirectAttributes rttr) {
        clientOrderService.deleteOrder(clOrderId);
        rttr.addFlashAttribute("message", "수주가 취소되었습니다.");
        return "redirect:/clientorder/list";
    }

 // ===== 메인: PDF 다운로드 (드롭인 교체) =====
    @GetMapping("/pdf/{clOrderId}")
    public void downloadOrderPdf(@PathVariable("clOrderId") String clOrderId,
                                 HttpServletResponse response) throws IOException {
        Document document = new Document();
        PdfWriter writer = null;

        try {
            // 0) 데이터 조회 (null 방어)
            com.itwillbs.domain.ClientOrderVO order =
                    clientOrderService.getOrderById(clOrderId);
            List<com.itwillbs.domain.ClientOrderDetailVO> detailList =
                    Optional.ofNullable(clientOrderDetailService.getDetailListByOrderId(clOrderId))
                            .orElse(Collections.emptyList());
            List<com.itwillbs.dto.DeliveryHistoryDTO> shipmentList =
                    Optional.ofNullable(deliveryService.getDeliveriesByOrderId(clOrderId))
                            .orElse(Collections.emptyList());

            // 1) 리소스(폰트/로고) "먼저" 로딩 — 실패 시 문서 열기 전에 예외 발생 → no pages 방지
            byte[] fontBytes = loadResourceBytes(FONT_CLASSPATH);
            com.lowagie.text.pdf.BaseFont baseFont =
                com.lowagie.text.pdf.BaseFont.createFont(
                    "NotoSansKR-Regular.ttf",
                    com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                    com.lowagie.text.pdf.BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null
                );

            final com.lowagie.text.Font titleFont  = new com.lowagie.text.Font(baseFont, 16);
            final com.lowagie.text.Font normalFont = new com.lowagie.text.Font(baseFont, 11);


            Image logo = null;
            try {
                byte[] logoBytes = loadResourceBytes(LOGO_CLASSPATH);
                logo = Image.getInstance(logoBytes);
                logo.scaleToFit(100, 50);
            } catch (Exception ignore) {}

            // 2) 응답 헤더
            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"order_" + clOrderId + ".pdf\"");

            // 3) 문서/라이터 오픈 (리소스 준비된 후)
            writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // 4) 헤더(로고/제목/회사정보)
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new int[]{3, 4, 3});

            PdfPCell logoCell = (logo != null) ? new PdfPCell(logo, true)
                                               : new PdfPCell(new Phrase(""));
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph docTitle = new Paragraph("수주 내역서", titleFont);
            PdfPCell titleCell = new PdfPCell(docTitle);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph companyInfo = new Paragraph(
                    "(주)오더브릿지\n부산 해운대구 센텀서로 30\nTEL: 051-123-4567",
                    normalFont);
            PdfPCell infoCell = new PdfPCell(companyInfo);
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            headerTable.addCell(logoCell);
            headerTable.addCell(titleCell);
            headerTable.addCell(infoCell);
            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            // 5) 수주 기본 정보
            PdfPTable masterTable = new PdfPTable(4);
            masterTable.setWidths(new int[]{1, 2, 1, 2});
            masterTable.setWidthPercentage(100);

            addCell(masterTable, "수주번호", normalFont);
            addCell(masterTable, order != null ? order.getClOrderId() : "-", normalFont);
            addCell(masterTable, "거래처명", normalFont);
            addCell(masterTable, order != null ? String.valueOf(order.getClientName()) : "-", normalFont);

            addCell(masterTable, "담당자", normalFont);
            addCell(masterTable, order != null ? String.valueOf(order.getManagerName()) : "-", normalFont);
            addCell(masterTable, "전화번호", normalFont);
            addCell(masterTable, order != null ? String.valueOf(order.getManagerTel()) : "-", normalFont);

            addCell(masterTable, "배송주소", normalFont);
            addCell(masterTable, order != null ? String.valueOf(order.getDeliveryAddress()) : "-", normalFont);
            addCell(masterTable, "우편번호", normalFont);
            addCell(masterTable, order != null ? String.valueOf(order.getPostCode()) : "-", normalFont);

            addCell(masterTable, "수주일자", normalFont);
            addCell(masterTable,
                    (order != null && order.getClOrderDate() != null) ?
                            sdf.format(order.getClOrderDate()) : "-", normalFont);
            addCell(masterTable, "납기요청일", normalFont);
            addCell(masterTable,
                    (order != null && order.getClDeliveryDate() != null) ?
                            sdf.format(order.getClDeliveryDate()) : "-", normalFont);

            addCell(masterTable, "수주상태", normalFont);
            addCell(masterTable, order != null ? String.valueOf(order.getClOrderStatus()) : "-", normalFont);
            addCell(masterTable, "메모", normalFont);
            addCell(masterTable,
                    (order != null && order.getClOrderMemo() != null) ? order.getClOrderMemo() : "-", normalFont);

            document.add(masterTable);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("※ 수주담당자: " + (order != null ? order.getAdminName() : "-"), normalFont));
            document.add(Chunk.NEWLINE);

            // 6) 수주 상세 내역
            document.add(new Paragraph("수주 상세 내역", normalFont));
            PdfPTable detailTable = new PdfPTable(6);
            detailTable.setWidths(new int[]{1, 3, 2, 2, 2, 2});
            detailTable.setWidthPercentage(100);
            Stream.of("No", "제품명", "수량", "단가", "합계", "비고")
                    .forEach(h -> addCell(detailTable, h, normalFont, true));

            int idx = 1, total = 0;
            for (com.itwillbs.domain.ClientOrderDetailVO d : detailList) {
                int unitPrice = Optional.ofNullable(d.getUnitPrice()).orElse(0);
                int qty       = Optional.ofNullable(d.getOrderQty()).orElse(0);
                int price     = qty * unitPrice;
                total += price;

                detailTable.addCell(String.valueOf(idx++));
                detailTable.addCell(new PdfPCell(new Phrase(
                        d.getProductName() != null ? d.getProductName() : "-", normalFont)));
                detailTable.addCell(String.valueOf(qty));
                detailTable.addCell(String.format("%,d", unitPrice));
                detailTable.addCell(String.format("%,d", price));
                detailTable.addCell(d.getDetailMemo() != null ? d.getDetailMemo() : "-");
            }
            addEmptyCells(detailTable, 3);
            addCell(detailTable, "총 합계", normalFont);
            addCell(detailTable, String.format("%,d", total), normalFont);
            addCell(detailTable, "", normalFont);
            document.add(detailTable);
            document.add(Chunk.NEWLINE);

            // 7) 출하 이력
            document.add(new Paragraph("출하 이력", normalFont));
            PdfPTable shipTable = new PdfPTable(7);
            shipTable.setWidths(new int[]{1, 3, 2, 3, 2, 3, 2});
            shipTable.setWidthPercentage(100);
            Stream.of("No", "제품명", "출하수량", "LOT", "출하일자", "송장번호", "출하상태")
                    .forEach(h -> addCell(shipTable, h, normalFont, true));

            int sIdx = 1;
            for (com.itwillbs.dto.DeliveryHistoryDTO s : shipmentList) {
                shipTable.addCell(String.valueOf(sIdx++));
                shipTable.addCell(new PdfPCell(new Phrase(
                        s.getProductName() != null ? s.getProductName() : "-", normalFont)));
                shipTable.addCell(String.valueOf(Optional.ofNullable(s.getDeliveryQty()).orElse(0)));
                shipTable.addCell(s.getLotNo() != null ? s.getLotNo() : "-");
                shipTable.addCell(s.getDeliveryDate() != null ? sdf.format(s.getDeliveryDate()) : "-");
                shipTable.addCell(s.getTrackingNumber() != null ? s.getTrackingNumber() : "-");
                shipTable.addCell(new Phrase(s.getDeliveryStatus() != null ? s.getDeliveryStatus() : "-", normalFont));
            }
            document.add(shipTable);

        } catch (Exception e) {
            // 자세한 원인 브라우저에 노출
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.reset();
                String msg = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "no message");
                response.sendError(500, "PDF 생성 중 오류 - " + msg);
            }
            return; // rethrow 대신 종료
        } finally {
            try {
                if (document.isOpen()) {
                    if (writer != null && writer.getPageNumber() == 0) {
                        // 혹시라도 콘텐츠가 하나도 안 들어갔으면 빈 문단 추가로 페이지 생성
                        document.add(new Paragraph(" "));
                    }
                    document.close();
                }
            } catch (Exception ignore) {}
        }
    }



 // 클래스 필드
    private static final String FONT_CLASSPATH = "/fonts/NotoSansKR-Regular.ttf"; // 또는 NanumGothic.ttf
    private static final String LOGO_CLASSPATH = "/images/logo.png";             // 선택

    // 클래스 메서드 (JDK8 호환)
 // 기존 중복 메서드 중, '하나'는 이름을 바꿉니다.
    private static byte[] loadResourceBytes(String path) throws IOException {
        try (InputStream is = ClientOrderController.class.getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException("리소스를 찾을 수 없습니다: " + path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096]; int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
            return baos.toByteArray();
        }
    }



    // PDF 테이블 셀 유틸
    private void addCell(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
    private void addCell(PdfPTable table, String text, com.lowagie.text.Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        if (isHeader) cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        table.addCell(cell);
    }
    private void addEmptyCells(PdfPTable table, int count) {
        for (int i = 0; i < count; i++) table.addCell("");
    }
}