package com.matzip.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matzip.common.exception.code.ErrorCode;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String status;

    private final ErrorInfo error;

    public static ErrorResponse error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse error(ErrorCode errorCode, String detailMessage) {
        return ErrorResponse.builder()
                .status("ERROR")
                .error(ErrorInfo.builder()
                        .code(errorCode.name())
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
