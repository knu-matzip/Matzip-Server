package com.matzip.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 도메인별로 에러 번호 체계를 구분
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (1000번대)
    INVALID_INPUT_VALUE(1000, HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(1001, HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(1002, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(1003, HttpStatus.BAD_REQUEST, "잘못된 타입의 값입니다."),
    HANDLE_ACCESS_DENIED(1004, HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

    // User & Auth Errors (2000번대)
    USER_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(2001, HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    KAKAO_LOGIN_FAILED(2002, HttpStatus.UNAUTHORIZED, "카카오 로그인에 실패했습니다."),
    UNAUTHORIZED(2003, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(2004, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(2005, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    ADMIN_ACCESS_REQUIRED(2006, HttpStatus.FORBIDDEN, "어드민 권한이 필요합니다."),

    // Place Errors (3000번대)
    PLACE_NOT_FOUND(3000, HttpStatus.NOT_FOUND, "맛집을 찾을 수 없습니다."),
    PLACE_ALREADY_EXISTS(3001, HttpStatus.CONFLICT, "이미 등록된 맛집입니다."),
    PLACE_PERMISSION_DENIED(3002, HttpStatus.FORBIDDEN, "해당 맛집을 수정 또는 삭제할 권한이 없습니다."),

    // Place Like Errors (3100번대)
    LIKE_NOT_FOUND(3101, HttpStatus.NOT_FOUND, "찜한 기록을 찾을 수 없습니다."),
    ALREADY_LIKED_PLACE(3102, HttpStatus.CONFLICT, "이미 찜한 가게입니다."),

    // Menu Errors (3500번대)
    MENU_NOT_FOUND(3500, HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    RECOMMENDED_MENU_LIMIT_EXCEEDED(3501, HttpStatus.BAD_REQUEST, "추천 메뉴는 최대 3개까지 선택할 수 있습니다."),

    // Category Errors (4000번대)
    CATEGORY_NOT_FOUND(4000, HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    CATEGORY_LIMIT_EXCEEDED(4001, HttpStatus.BAD_REQUEST, "카테고리는 최대 5개까지 등록할 수 있습니다."),

    // Tag Errors (4500번대)
    TAG_NOT_FOUND(4500, HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."),
    TAG_LIMIT_EXCEEDED(4501, HttpStatus.BAD_REQUEST, "태그는 최대 10개까지 등록할 수 있습니다."),

    // Photo Errors (5000번대)
    PHOTO_NOT_FOUND(5000, HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다."),

    // Validation Errors (6000번대)
    VALIDATION_ERROR(6000, HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),

    // External API Errors (7000번대)
    KAKAO_API_ERROR(7000, HttpStatus.SERVICE_UNAVAILABLE, "카카오 API 호출 중 오류가 발생했습니다."),
    KAKAO_PLACE_SEARCH_ERROR(7001, HttpStatus.NOT_FOUND, "카카오맵에서 해당 장소를 찾을 수 없습니다."),
    KAKAO_PANEL3_CALL_FAILED(7002, HttpStatus.SERVICE_UNAVAILABLE, "카카오 panel3 API 호출에 실패했습니다."),
    KAKAO_CONFIRM_ID_MISSING(7003, HttpStatus.BAD_REQUEST, "카카오 API 응답에 confirm_id가 없습니다."),
    KAKAO_CONFIRM_ID_MISMATCH(7004, HttpStatus.BAD_REQUEST, "요청 kakaoPlaceId와 응답 confirm_id가 일치하지 않습니다."),
    KAKAO_PANEL3_FIELD_ERROR(7005, HttpStatus.BAD_REQUEST, "카카오 panel3 응답 필드에 오류가 있습니다."),

    EVENT_ENDED(10000, HttpStatus.BAD_REQUEST, "종료된 이벤트입니다."),
    INSUFFICIENT_ENTRY_TICKETS(10001, HttpStatus.BAD_REQUEST, "응모권이 부족합니다.");

    private final int code;
    private final HttpStatus status;
    private final String message;
}