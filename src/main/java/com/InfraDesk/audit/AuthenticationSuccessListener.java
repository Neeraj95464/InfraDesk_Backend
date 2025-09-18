package com.InfraDesk.audit;

import com.InfraDesk.entity.*;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.UserLoginAuditRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserRepository userRepository;
    private final UserLoginAuditRepository loginAuditRepository;
    private final CompanyRepository companyRepository;
    private final HttpServletRequest request;

    private static final String GEOLOCATION_API = "https://ipinfo.io/{ip}/json";

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            // Anonymous or unknown, do not log
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;

        User user = userRepository.findById(userDetails.getUserId()).orElse(null);
        if (user == null) return;

        String ip = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        Company company = determineCompanyContext(user);

        String loginLocation = fetchGeolocation(ip);

        UserLoginAudit audit = UserLoginAudit.builder()
                .user(user)
                .company(company)
                .ipAddress(ip)
                .userAgent(userAgent)
                .loginLocation(loginLocation)
                .successful(true)
                .build();

        loginAuditRepository.save(audit);
    }

    private Company determineCompanyContext(User user) {
        return user.getMemberships().stream()
                .filter(m -> m.getIsActive() && !m.getIsDeleted())
                .map(Membership::getCompany)
                .findFirst()
                .orElse(null);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        // In case of multiple IP addresses in header, take first one
        return xfHeader.split(",")[0].trim();
    }

    private String fetchGeolocation(String ip) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<IpInfoResponse> response = restTemplate.getForEntity(GEOLOCATION_API, IpInfoResponse.class, ip);
            IpInfoResponse ipInfo = response.getBody();
            if (ipInfo == null) return null;
            // Example format: city, region, country
            return String.format("%s, %s, %s",
                    safeString(ipInfo.getCity()),
                    safeString(ipInfo.getRegion()),
                    safeString(ipInfo.getCountry())
            ).replaceAll("(, )+", ", ").replaceAll("^(, )+|(, )+$", "");
        } catch (Exception e) {
            // Log error if needed, but do not block login
            return null;
        }
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    // POJO for IP geolocation API JSON response parsing
    public static class IpInfoResponse {
        private String city;
        private String region;
        private String country;
        // getters and setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}
