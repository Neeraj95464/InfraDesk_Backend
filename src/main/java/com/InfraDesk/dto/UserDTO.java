package com.InfraDesk.dto;

import com.InfraDesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private String publicId;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

