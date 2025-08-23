package com.matzip.common.exception;

import com.matzip.common.exception.code.ErrorCode;

public class PlaceRegistrationException extends BaseException {
    
    public PlaceRegistrationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public PlaceRegistrationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public PlaceRegistrationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public PlaceRegistrationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
