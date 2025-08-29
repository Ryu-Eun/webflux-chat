package com.practice.projectchat.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

// QWER1234 처럼 앞 4자리는 대문자알파벳, 뒤 4자리는 숫자인 FriendCode를 생성
@Component
public class FriendCodeGenerator {

    private static final int LETTER_COUNT = 4;
    private static final int DIGIT_COUNT = 4;
    private static final int CODE_LENGTH = DIGIT_COUNT * LETTER_COUNT; // 총 8글자
    private static final String LETTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT = "0123456789";
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z]{" + LETTER_COUNT + "}\\d{" + DIGIT_COUNT + "}$");

    private final SecureRandom random = new SecureRandom();

    public String generateFriendCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for(int i=0; i<LETTER_COUNT; i++) {
            sb.append(LETTER.charAt(random.nextInt(LETTER.length())));
        }
        for(int i=0; i<DIGIT_COUNT; i++){
            sb.append(DIGIT.charAt(random.nextInt(DIGIT.length())));
        }
        return sb.toString();
    }

    // null이 아니고 pattern에 맞게 잘 생성됐는지 체크
    public boolean isValid(String code){
        return code != null && VALID_PATTERN.matcher(code).matches();
    }

}