package com.InfraDesk.dto;

import lombok.Data;

@Data
public class UserSearchCriteria {
    private String keyword;  // can match email, name, employee id, phone, etc.
}

