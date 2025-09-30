package com.InfraDesk.auth;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.dto.SmtpConfigDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Employee;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.entity.User;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.MailIntegrationRepository;
import com.InfraDesk.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final AuthUtils authUtils;

    @Value("${gmail.client.id}")
    private String gmailClientId;
    @Value("${gmail.client.secret}")
    private String gmailClientSecret;
    @Value("${ms.client.id}")
    private String msClientId;
    @Value("${ms.client.secret}")
    private String msClientSecret;


    @GetMapping("/oauth/{provider}/authorize")
    @ResponseBody
    public Map<String, String> authorizeUrl(@PathVariable String provider,
                                            @RequestParam String companyId,
                                            @RequestParam String redirectUri) {

        String state = companyId;

        if ("gmail".equalsIgnoreCase(provider)) {
            String scopes = String.join(" ",
                    "https://www.googleapis.com/auth/gmail.readonly",   // read emails
                    "https://www.googleapis.com/auth/gmail.modify",     // read + delete/mark emails
                    "https://www.googleapis.com/auth/gmail.send",       // send emails
                    "https://www.googleapis.com/auth/userinfo.email",   // get email address
                    "https://www.googleapis.com/auth/userinfo.profile"  // optional profile info
            );

            String url = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                    .queryParam("client_id", gmailClientId)
                    .queryParam("response_type", "code")
                    .queryParam("scope", scopes)
                    .queryParam("access_type", "offline")
                    .queryParam("include_granted_scopes", "true")
                    .queryParam("prompt", "consent")
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("state", state)
                    .build().toUriString();
            return Map.of("url", url);
        } else if ("microsoft".equalsIgnoreCase(provider)) {
//            String scopes = "offline_access Mail.ReadWrite Mail.Send";
            String scopes = "offline_access User.Read Mail.ReadWrite Mail.Send";

            String url = UriComponentsBuilder.fromUriString("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                    .queryParam("client_id", msClientId)
                    .queryParam("scope", scopes)
                    .queryParam("response_type", "code")
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("response_mode", "query")
                    .queryParam("state", state)
                    .build().toUriString();
            return Map.of("url", url);
        }
        throw new IllegalArgumentException("Unsupported provider");
    }

    @GetMapping("/oauth/{provider}/callback")
    public String callback(@PathVariable String provider,
                           @RequestParam String code,
                           @RequestParam(required = false) String state
                           ) {
        String redirectUri = null;
        if(provider.equals("gmail")){
            redirectUri = "http://localhost:8080/api/mail/oauth/gmail/callback";
        } else if(provider.equals("microsoft")){
            redirectUri = "http://localhost:8080/api/mail/oauth/microsoft/callback";
        }else {
            System.out.println("Provider not found ");
        }


        String companyId = null;
        if (state != null && !state.isEmpty()) {
            companyId = state; // decode if you encoded before
            System.out.println("Received companyId from state param: " + companyId);
        } else {
            // fallback or error handling if companyId missing
            System.err.println("companyId not found in OAuth state parameter");
            throw new BusinessException("Company ID missing in OAuth flow");
        }

        System.out.println("company public id is "+companyId);

        if ("gmail".equalsIgnoreCase(provider)) {
            try {
                System.out.println("Starting Gmail OAuth callback");
                Map<String, Object> tokenResp = webClient.post()
                        .uri("https://oauth2.googleapis.com/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .bodyValue("code=" + code +
                                "&client_id=" + gmailClientId +
                                "&client_secret=" + gmailClientSecret +
                                "&redirect_uri=" + redirectUri +
                                "&grant_type=authorization_code")
                        .retrieve().bodyToMono(Map.class).block();

                System.out.println("Token response: " + tokenResp);

                if (tokenResp == null || !tokenResp.containsKey("access_token")) {
                    System.err.println("Failed to retrieve access token from token response");
                    throw new RuntimeException("No access token in token response");
                }

                String accessToken = (String) tokenResp.get("access_token");
                String refreshToken = (String) tokenResp.get("refresh_token");
                Integer expiresIn = (Integer) tokenResp.get("expires_in");

                Map profile = webClient.get()
                        .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                        .headers(h -> h.setBearerAuth(accessToken))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (profile == null || !profile.containsKey("email")) {
                    throw new RuntimeException("No email in userinfo response");
                }

                System.out.println("Gmail profile: " + profile);

                if (profile == null || !profile.containsKey("email")) {
                    System.err.println("Failed to retrieve email from userinfo response");
                    throw new RuntimeException("No email in userinfo response");
                }

                String emailAddress = (String) profile.get("email");

                Optional<MailIntegration> existing = repo.findByCompanyIdAndMailboxEmail(companyId, emailAddress);

                MailIntegration integration;
                if (existing.isPresent()) {
                    integration = existing.get();
                    // update existing record fields e.g. tokens, expiration, enabled
                    integration.setEncryptedAccessToken(encryptor.encrypt(accessToken));
                    integration.setEncryptedRefreshToken(refreshToken != null ? encryptor.encrypt(refreshToken) : null);
                    integration.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
                    integration.setScopes("gmail.modify,gmail.send");
                    integration.setEnabled(true);
                    integration.setUpdatedAt(Instant.now());
                } else {
                    integration = MailIntegration.builder()
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
                }

                repo.save(integration);


                System.out.println("Gmail integration saved for companyId: " + companyId + ", email: " + emailAddress);

                return "redirect:" + "http://localhost:5173/app/mail-connected?status=ok";

            } catch (Exception e) {
                System.err.println("Exception in Gmail OAuth callback: " + e.getMessage());
                e.printStackTrace();
                return "redirect:" + "http://localhost:5173/app/mail-connected?status=error";
            }
        }

        if ("microsoft".equalsIgnoreCase(provider)) {
            System.out.println("provider microsoft selected ");


            Map<String, Object> tokenResp = webClient.post()
                    .uri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("client_id=" + msClientId +
                            "&scope=offline_access%20Mail.ReadWrite%20Mail.Send" +
                            "&code=" + code +
                            "&redirect_uri=" + redirectUri +
                            "&grant_type=authorization_code" +
                            "&client_secret=" + msClientSecret)
                    .retrieve().bodyToMono(Map.class).block();

            String accessToken = (String) tokenResp.get("access_token");
            String refreshToken = (String) tokenResp.get("refresh_token");
            Integer expiresIn = (Integer) tokenResp.get("expires_in");

            Map profile = webClient.get()
                    .uri("https://graph.microsoft.com/v1.0/me")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(Map.class).block();

            String email = (String) profile.get("userPrincipalName");

//            MailIntegration integration = MailIntegration.builder()
//                    .companyId(companyId)
//                    .provider("MICROSOFT")
//                    .mailboxEmail(email)
//                    .encryptedAccessToken(encryptor.encrypt(accessToken))
//                    .encryptedRefreshToken(encryptor.encrypt(refreshToken))
//                    .tokenExpiresAt(Instant.now().plusSeconds(expiresIn))
//                    .scopes("Mail.ReadWrite,Mail.Send")
//                    .enabled(true)
//                    .createdAt(Instant.now())
//                    .build();
//            repo.save(integration);

            Optional<MailIntegration> existing = repo.findByCompanyIdAndMailboxEmail(companyId, email);

            MailIntegration integration;
            if (existing.isPresent()) {
                integration = existing.get();
                // update existing record fields e.g. tokens, expiration, enabled
                integration.setEncryptedAccessToken(encryptor.encrypt(accessToken));
                integration.setEncryptedRefreshToken(refreshToken != null ? encryptor.encrypt(refreshToken) : null);
                integration.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
                integration.setScopes("Mail.ReadWrite,Mail.Send");
                integration.setEnabled(true);
                integration.setUpdatedAt(Instant.now());
            } else {
                integration = MailIntegration.builder()
                        .companyId(companyId)
                        .provider("MICROSOFT")
                        .mailboxEmail(email)
                        .encryptedAccessToken(encryptor.encrypt(accessToken))
                        .encryptedRefreshToken(refreshToken != null ? encryptor.encrypt(refreshToken) : null)
                        .tokenExpiresAt(Instant.now().plusSeconds(expiresIn))
                        .scopes("Mail.ReadWrite,Mail.Send")
                        .enabled(true)
                        .createdAt(Instant.now())
                        .build();
            }

            repo.save(integration);


            return "redirect:" + "http://localhost:5173/app/mail-connected?status=ok";
        }

        throw new IllegalArgumentException("Unsupported provider");
    }


    @PostMapping("/api/mail/config")
    public ResponseEntity<?> saveSmtpConfig(@RequestBody SmtpConfigDTO dto) {
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
