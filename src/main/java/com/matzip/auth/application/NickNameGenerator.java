package com.matzip.auth.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 최초 로그인 시 랜덤 닉네임을 생성하는 유틸
 */
@Component
public class NickNameGenerator {

    private static final String[] ADJECTIVES = {
            "든든한", "기민한", "대담한", "따뜻한", "부지런한", "상냥한", "성실한", "신속한",
            "유쾌한", "자상한", "재치있는", "침착한", "친절한", "활기찬", "꼼꼼한", "담대한"
    };

    private static final String[] NOUNS = {
            "곰", "여우", "토끼", "고래", "호랑이", "사자", "판다", "너구리",
            "부엉이", "수달", "앵무새", "치타", "코알라", "펭귄", "다람쥐", "고슴도치"
    };

    // 접미 숫자 범위
    private static final int SUFFIX_BOUND = 10_000;

    private static final int MAX_NICKNAME_LENGTH = 30;

    private final SecureRandom random = new SecureRandom();

    /**
     * 예: "활기찬수달_0427"
     * - 형식: {형용사}{명사}_{4자리}
     * - 길이 제한(30자)을 초과하지 않도록 보수적으로 구성.
     */
    public String generate() {
        String adj = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        String suffix = String.format("%04d", random.nextInt(SUFFIX_BOUND));

        String candidate = adj + noun + "_" + suffix;

        if (candidate.length() > MAX_NICKNAME_LENGTH) {
            candidate = candidate.substring(0, MAX_NICKNAME_LENGTH);
        }
        return candidate;
    }
}
