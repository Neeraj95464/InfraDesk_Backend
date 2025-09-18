package com.InfraDesk.exception;

public class BusinessException extends RuntimeException {
    private String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}


