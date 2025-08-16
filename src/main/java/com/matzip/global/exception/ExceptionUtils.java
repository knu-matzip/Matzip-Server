package com.matzip.global.exception;

import com.matzip.global.exception.code.ErrorCode;

public class ExceptionUtils {
    
    private ExceptionUtils() {
        // 유틸리티 클래스이므로 인스턴스 생성 방지
    }
    
    /**
     * 조건이 참이면 BusinessException을 발생시킵니다.
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }
    
    /**
     * 조건이 참이면 BusinessException을 발생 (커스텀 메시지 포함)
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BusinessException(errorCode, message);
        }
    }
    
    /**
     * 조건이 거짓이면 BusinessException을 발생
     */
    public static void throwIfNot(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }
    
    /**
     * 조건이 거짓이면 BusinessException을 발생 (커스텀 메시지 포함)
     */
    public static void throwIfNot(boolean condition, ErrorCode errorCode, String message) {
        if (!condition) {
            throw new BusinessException(errorCode, message);
        }
    }
    
    /**
     * 객체가 null이면 BusinessException을 발생
     */
    public static void throwIfNull(Object object, ErrorCode errorCode) {
        if (object == null) {
            throw new BusinessException(errorCode);
        }
    }
    
    /**
     * 객체가 null이면 BusinessException을 발생 (커스텀 메시지 포함)
     */
    public static void throwIfNull(Object object, ErrorCode errorCode, String message) {
        if (object == null) {
            throw new BusinessException(errorCode, message);
        }
    }
    
    /**
     * 문자열이 비어있으면 BusinessException을 발생
     */
    public static void throwIfEmpty(String string, ErrorCode errorCode) {
        if (string == null || string.trim().isEmpty()) {
            throw new BusinessException(errorCode);
        }
    }
    
    /**
     * 문자열이 비어있으면 BusinessException을 발생 (커스텀 메시지 포함)
     */
    public static void throwIfEmpty(String string, ErrorCode errorCode, String message) {
        if (string == null || string.trim().isEmpty()) {
            throw new BusinessException(errorCode, message);
        }
    }

    /**
     * 카테고리, 추천 메뉴, 태그 등 개수 제한
     */
    public static void throwIfExceeds(int currentCount, int limit, ErrorCode errorCode) {
        if (currentCount > limit) {
            throw new BusinessException(errorCode);
        }
    }


}
