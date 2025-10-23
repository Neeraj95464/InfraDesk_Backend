package com.InfraDesk.dto;

import com.InfraDesk.enums.Role;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MembershipFilterRequest {
    private List<String> roles;
    private Boolean isActive;
    private Boolean isDeleted;
    private String userEmail;
    private String companyName;
    private String createdBy;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;
    private int page = 0;
    private int size = 20;
    private String search;
}
