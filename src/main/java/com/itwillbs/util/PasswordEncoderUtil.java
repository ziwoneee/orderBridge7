package com.itwillbs.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 비밀번호 암호화
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 비밀번호 일치 여부 확인
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}