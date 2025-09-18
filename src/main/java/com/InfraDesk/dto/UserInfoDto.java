package com.InfraDesk.dto;

import lombok.Builder;

//@Builder
//public record UserInfoDto(
//        Long id,
//        String email,
//        String name,
//        String role
//) {}


@Builder
public record UserInfoDto(
        String id,           // publicId like "usr_xxx"
        String email,
        String fullName,
        String role  // SUPER_ADMIN, COMPANY_CONFIGURE, SUPPORT, USER
) {}
