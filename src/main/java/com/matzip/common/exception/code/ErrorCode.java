package com.matzip.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "잘못된 타입의 값입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // User & Auth Errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "카카오 로그인에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    ADMIN_ACCESS_REQUIRED(HttpStatus.FORBIDDEN, "어드민 권한이 필요합니다."),

    // Place Errors
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "맛집을 찾을 수 없습니다."),
    PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 맛집입니다."),
    PLACE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 맛집을 수정 또는 삭제할 권한이 없습니다."),

    // Place Like Errors
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "찜한 기록을 찾을 수 없습니다."),
    ALREADY_LIKED_PLACE(HttpStatus.CONFLICT, "이미 찜한 가게입니다."),

    // Menu Errors
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    RECOMMENDED_MENU_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "추천 메뉴는 최대 3개까지 선택할 수 있습니다."),

    // Category Errors
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    CATEGORY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "카테고리는 최대 5개까지 등록할 수 있습니다."),

    // Tag Errors
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."),
    TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "태그는 최대 10개까지 등록할 수 있습니다."),

    // Photo Errors
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다."),

    // Validation Errors
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),

    // External API Errors
    KAKAO_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "카카오 API 호출 중 오류가 발생했습니다."),
    KAKAO_PLACE_SEARCH_ERROR(HttpStatus.NOT_FOUND, "카카오맵에서 해당 장소를 찾을 수 없습니다."),
    KAKAO_PANEL3_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "카카오 panel3 API 호출에 실패했습니다."),
    KAKAO_CONFIRM_ID_MISSING(HttpStatus.BAD_REQUEST, "카카오 API 응답에 confirm_id가 없습니다."),
    KAKAO_CONFIRM_ID_MISMATCH(HttpStatus.BAD_REQUEST, "요청 kakaoPlaceId와 응답 confirm_id가 일치하지 않습니다."),
    KAKAO_PANEL3_FIELD_ERROR(HttpStatus.BAD_REQUEST, "카카오 panel3 응답 필드에 오류가 있습니다."),

    // Event Errors
    EVENT_ENDED(HttpStatus.BAD_REQUEST, "종료된 이벤트입니다."),
    INSUFFICIENT_ENTRY_TICKETS(HttpStatus.BAD_REQUEST, "응모권이 부족합니다."),
    EVENT_NOT_PARTICIPATED(HttpStatus.FORBIDDEN, "참여한 이벤트가 아닙니다."),
    EVENT_NOT_WINNER(HttpStatus.FORBIDDEN, "당첨자가 아닙니다."),
    AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "약관 및 개인정보 수집에 동의해 주세요."),
    DRAW_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "이벤트 추첨이 완료된 후 상품 수령 신청이 가능합니다.");

    private final HttpStatus status;
    private final String message;
}