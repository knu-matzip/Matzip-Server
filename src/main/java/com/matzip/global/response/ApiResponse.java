package com.matzip.global.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ErrorInfo error;
    
    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("OK")
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .status("OK")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .timestamp(LocalDateTime.now())
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }
    
    /**
     * 실패 응답 생성 (ErrorCode 사용)
     */
    public static <T> ApiResponse<T> error(com.matzip.global.exception.code.ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .timestamp(LocalDateTime.now())
                .error(ErrorInfo.builder()
                        .code(String.valueOf(errorCode.getCode()))
                        .message(errorCode.getMessage())
                        .build())
                .build();
    }
    
    /**
     * 실패 응답 생성 (ErrorCode + 상세 메시지)
     */
    public static <T> ApiResponse<T> error(com.matzip.global.exception.code.ErrorCode errorCode, String detailMessage) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .timestamp(LocalDateTime.now())
                .error(ErrorInfo.builder()
                        .code(String.valueOf(errorCode.getCode()))
                        .message(detailMessage)
                        .build())
                .build();
    }
    
    @Getter
    @Builder
    public static class ErrorInfo {
        private final String code;
        private final String message;
    }
}
