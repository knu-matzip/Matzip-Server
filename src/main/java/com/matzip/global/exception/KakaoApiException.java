package com.matzip.global.exception;

import com.matzip.global.exception.code.ErrorCode;

public class KakaoApiException extends BaseException {
    
    public KakaoApiException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public KakaoApiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public KakaoApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public KakaoApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
