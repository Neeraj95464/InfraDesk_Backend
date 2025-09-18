package com.InfraDesk.util;

import com.InfraDesk.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        throw new IllegalStateException("Not authenticated");
    }
}

