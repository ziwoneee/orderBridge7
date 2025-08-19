package com.itwillbs.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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
}
