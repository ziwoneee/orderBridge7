package com.itwillbs.service;

import org.springframework.stereotype.Service;

import com.itwillbs.domain.ClientOrderDetailVO;
import java.util.List;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class MailServiceImpl implements MailService {

    // ✅ Gmail 발신 계정 정보
    private final String username = "cocoahreum@gmail.com";      // Gmail 주소
    private final String password = "jjds lryi vyqp djwg";         // 앱 비밀번호 (Gmail에서 발급)

    @Override
    public void sendMail(String to, String subject, String body) {
        // SMTP 설정
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 인증 세션 생성
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 메일 메시지 구성
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "OrderBridge")); // 발신자 이름 설정
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 수신자
            message.setSubject(subject); // 제목
            message.setText(body);       // 본문

            // 메일 전송
            Transport.send(message);
            System.out.println("✅ 메일 전송 성공: " + to);

        } catch (Exception e) {
            System.err.println("❌ 메일 전송 실패:");
            e.printStackTrace();
        }
    }
    
    
 // ✅ 수주 등록 완료 메일 (텍스트 본문: 항목 라인 + 총합계)
    public void sendOrderRegisteredMail(
            String to, String clientName, String clOrderId, String deliveryDate,
            List<ClientOrderDetailVO> detailList) {

        String subject = "[OrderBridge] 수주 등록 완료 안내 - " + clOrderId;

        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

        long grandTotal = 0L;
        StringBuilder lines = new StringBuilder();

        if (detailList != null && !detailList.isEmpty()) {
            for (ClientOrderDetailVO d : detailList) {
                String name = d.getProductName() != null && !d.getProductName().isEmpty()
                        ? d.getProductName()
                        : (d.getProductId() != null ? d.getProductId() : "-");
                int qty = d.getOrderQty();                    // ← VO에 맞게 (clOrderQty면 변경)
                Integer unit = d.getUnitPrice();              // null 가능 시 가드
                if (unit == null) unit = 0;

                long lineTotal = (long) qty * unit;
                grandTotal += lineTotal;

                // 예: "- 제품A | 단가: 5,000원 | 수량: 10 | 금액: 50,000원"
                lines.append(" - ").append(name)
                     .append(" | 단가: ").append(nf.format(unit)).append("원")
                     .append(" | 수량: ").append(nf.format(qty))
                     .append(" | 금액: ").append(nf.format(lineTotal)).append("원")
                     .append("\n");
            }
        }

        String body =
            "안녕하세요, " + (clientName != null ? clientName : "고객사") + " 담당자님.\n\n" +
            "아래와 같이 수주가 정상 등록되었습니다.\n\n" +
            "수주번호 : " + clOrderId + "\n" +
            "납기요청일 : " + (deliveryDate != null ? deliveryDate : "미정") + "\n\n" +
            (lines.length() > 0 ? "【수주 내역】\n" + lines.toString() + "\n" : "") +
            "총 합계액 : " + nf.format(grandTotal) + "원\n\n" +
            "추가 문의사항이 있으시면 회신 부탁드립니다.\n\n" +
            "감사합니다.\nOrderBridge 드림";

        // 텍스트 메일 전송 (setText 사용)
        sendMail(to, subject, body);
    }
	
    
    
}
