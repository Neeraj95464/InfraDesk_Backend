package com.InfraDesk.config;

import com.InfraDesk.entity.User;
import com.InfraDesk.util.AuthUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AuditConfig {

    private final AuthUtils authUtils;

    @Bean
    public AuditorAware<User> auditorAware() {
        return authUtils::getAuthenticatedUser;
        // returns Optional<User>, exactly what AuditorAware<User> expects
    }

}

