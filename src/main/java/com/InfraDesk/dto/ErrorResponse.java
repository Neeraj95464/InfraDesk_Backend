package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private String errorCode;    // Machine-readable error code, e.g. DOMAIN_EXISTS, USER_NOT_FOUND
    private String message;      // Human-friendly error message
    private String tenantId;     // Optional: tenant/company context to aid frontend logging/debugging
    private List<FieldError> fieldErrors;  // Optional: validation errors

    // Constructor for simple errors
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    // Constructor including field validation errors
    public ErrorResponse(String errorCode, String message, List<FieldError> fieldErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }
}
