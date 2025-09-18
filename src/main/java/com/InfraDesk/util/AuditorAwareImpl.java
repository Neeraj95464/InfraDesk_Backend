package com.InfraDesk.util;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    private final AuthUtils authUtils;

    public AuditorAwareImpl(AuthUtils authUtils) {
        this.authUtils = authUtils;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return Optional.empty(); // no auditor if anonymous
        }

        return Optional.of(authentication.getName());
    }

}


