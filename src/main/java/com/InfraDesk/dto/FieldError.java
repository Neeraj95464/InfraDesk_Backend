package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {
    private String field;
    private String rejectedValue;
    private String errorMessage;

    public FieldError(String field, Object rejectedValue, String errorMessage) {
        this.field = field;
        this.rejectedValue = rejectedValue != null ? rejectedValue.toString() : null;
        this.errorMessage = errorMessage;
    }
}

