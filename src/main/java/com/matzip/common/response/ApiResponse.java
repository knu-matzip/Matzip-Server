package com.matzip.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import static java.util.Collections.*;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final T data;

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("OK")
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     * DELETE, PATCH 등 데이터 반환이 필요 없는 요청에 사용
     */
    public static <T> ApiResponse<T> successWithoutData() {
        return ApiResponse.<T>builder()
                .status("OK")
                // data 필드는 null로 유지
                .build();
    }

    /**
     * 성공 응답 생성 (빈 리스트)
     */
    public static <T> ApiResponse<T> successWithEmptyList() {
        return ApiResponse.<T>builder()
                .status("OK")
                .data((T) emptyList()) // data 필드에 빈 리스트를 명시적으로 할당
                .build();
    }
}
