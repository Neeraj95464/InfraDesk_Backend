package com.InfraDesk.auth;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.dto.SmtpConfigDTO;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.repository.MailIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailAuthController {

    private final MailIntegrationRepository repo;
    private final SimpleEncryptor encryptor;
    private final WebClient webClient = WebClient.create();

    // Build authorize url for provider
    @GetMapping("/oauth/{provider}/authorize")
    @ResponseBody
    public Map<String, String> authorizeUrl(@PathVariable String provider,
                                            @RequestParam Long companyId,
                                            @RequestParam String redirectUri) {

        if ("gmail".equalsIgnoreCase(provider)) {
            String clientId = System.getenv("GMAIL_CLIENT_ID");
            String scopes = "https://www.googleapis.com/auth/gmail.modify https://www.googleapis.com/auth/gmail.send";
            String url = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                    .queryParam("client_id", clientId)
                    .queryParam("response_type", "code")
                    .queryParam("scope", scopes)
                    .queryParam("access_type", "offline")
                    .queryParam("include_granted_scopes", "true")
                    .queryParam("prompt", "consent")
                    .queryParam("redirect_uri", redirectUri)
                    .build().toUriString();
            return Map.of("url", url);
        } else if ("microsoft".equalsIgnoreCase(provider)) {
            String clientId = System.getenv("MS_CLIENT_ID");
            String scopes = "offline_access Mail.ReadWrite Mail.Send";
            String url = UriComponentsBuilder.fromUriString("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                    .queryParam("client_id", clientId)
                    .queryParam("scope", scopes)
                    .queryParam("response_type", "code")
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("response_mode", "query")
                    .build().toUriString();
            return Map.of("url", url);
        } else {
            throw new IllegalArgumentException("Unsupported provider");
        }
    }

    // Callback: exchange code for tokens and save integration
    // redirectUri must match the one used in authorize step
    @GetMapping("/oauth/{provider}/callback")
    public String callback(@PathVariable String provider,
                           @RequestParam String code,
                           @RequestParam Long companyId,
                           @RequestParam String redirectUri) {

        if ("gmail".equalsIgnoreCase(provider)) {
            String clientId = System.getenv("GMAIL_CLIENT_ID");
            String clientSecret = System.getenv("GMAIL_CLIENT_SECRET");
            // Exchange code for tokens
            Map<String, Object> tokenResp = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("code=" + code +
                            "&client_id=" + clientId +
                            "&client_secret=" + clientSecret +
                            "&redirect_uri=" + redirectUri +
                            "&grant_type=authorization_code")
                    .retrieve().bodyToMono(Map.class).block();

            String accessToken = (String) tokenResp.get("access_token");
            String refreshToken = (String) tokenResp.get("refresh_token");
            Integer expiresIn = (Integer) tokenResp.get("expires_in");

            // get mailbox email by calling Gmail profile
            Map profile = webClient.get()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/profile")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(Map.class).block();
            String emailAddress = (String) profile.get("emailAddress");

            // Save integration
            MailIntegration integration = MailIntegration.builder()
                    .companyId(companyId)
                    .provider("GMAIL")
                    .mailboxEmail(emailAddress)
                    .encryptedAccessToken(encryptor.encrypt(accessToken))
                    .encryptedRefreshToken(refreshToken != null ? encryptor.encrypt(refreshToken) : null)
                    .tokenExpiresAt(Instant.now().plusSeconds(expiresIn))
                    .scopes("gmail.modify,gmail.send")
                    .enabled(true)
                    .createdAt(Instant.now())
                    .build();
            repo.save(integration);
            // Redirect to front-end success page
            return "redirect:" + "https://your-frontend/app/mail-connected?status=ok";
        }

        if ("microsoft".equalsIgnoreCase(provider)) {
            String clientId = System.getenv("MS_CLIENT_ID");
            String clientSecret = System.getenv("MS_CLIENT_SECRET");

            // Exchange code
            Map<String, Object> tokenResp = webClient.post()
                    .uri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("client_id=" + clientId +
                            "&scope=offline_access%20Mail.ReadWrite%20Mail.Send" +
                            "&code=" + code +
                            "&redirect_uri=" + redirectUri +
                            "&grant_type=authorization_code" +
                            "&client_secret=" + clientSecret)
                    .retrieve().bodyToMono(Map.class).block();

            String accessToken = (String) tokenResp.get("access_token");
            String refreshToken = (String) tokenResp.get("refresh_token");
            Integer expiresIn = (Integer) tokenResp.get("expires_in");

            // get mailbox email via Graph
            Map profile = webClient.get()
                    .uri("https://graph.microsoft.com/v1.0/me")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(Map.class).block();

            String email = (String) profile.get("userPrincipalName");

            MailIntegration integration = MailIntegration.builder()
                    .companyId(companyId)
                    .provider("MICROSOFT")
                    .mailboxEmail(email)
                    .encryptedAccessToken(encryptor.encrypt(accessToken))
                    .encryptedRefreshToken(encryptor.encrypt(refreshToken))
                    .tokenExpiresAt(Instant.now().plusSeconds(expiresIn))
                    .scopes("Mail.ReadWrite,Mail.Send")
                    .enabled(true)
                    .createdAt(Instant.now())
                    .build();
            repo.save(integration);

            return "redirect:" + "https://your-frontend/app/mail-connected?status=ok";
        }

        throw new IllegalArgumentException("Unsupported provider");
    }

    @PostMapping("/api/mail/config")
    public ResponseEntity<?> saveSmtpConfig(@RequestBody SmtpConfigDTO dto) {
        // check authenticated user is admin for company (AuthUtils)
        MailIntegration m = MailIntegration.builder()
                .companyId(dto.getCompanyId())
                .provider("SMTP")
                .mailboxEmail(dto.getMailboxEmail())
                .smtpHost(dto.getSmtpHost())
                .smtpPort(dto.getSmtpPort())
                .smtpTls(dto.getSmtpTls())
                .encryptedSmtpPassword(encryptor.encrypt(dto.getSmtpPassword()))
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        repo.save(m);
        return ResponseEntity.ok(Map.of("ok", true));
    }

}

