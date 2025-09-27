package com.matzip.common.exception;

import com.matzip.common.exception.code.ErrorCode;

public class PlaceAlreadyExistsException extends BusinessException {
    public PlaceAlreadyExistsException() {
        super(ErrorCode.PLACE_ALREADY_EXISTS);
    }

    public PlaceAlreadyExistsException(String message) {
        super(ErrorCode.PLACE_ALREADY_EXISTS, message);
    }
}
