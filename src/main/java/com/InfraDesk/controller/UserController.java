package com.InfraDesk.controller;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.UserDTO;
import com.InfraDesk.entity.User;
import com.InfraDesk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_VIEW')")
    public ResponseEntity<PaginatedResponse<UserDTO>> searchUsers(
            @RequestParam String companyId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<UserDTO> users = userService.searchUsersForCompanyAndSubsidiaries(companyId, keyword, page, size);
        return ResponseEntity.ok(users);
    }

}
