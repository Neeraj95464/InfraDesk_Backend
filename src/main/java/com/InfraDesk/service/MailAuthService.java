package com.InfraDesk.service;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.repository.MailIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailAuthService {

    private final MailIntegrationRepository repo;
    private final SimpleEncryptor encryptor;
    private final WebClient webClient = WebClient.create();

    public MailIntegration refreshIfNeeded(MailIntegration m) {
        if (m.getEncryptedRefreshToken() == null) return m;

        if (m.getTokenExpiresAt() != null && Instant.now().isBefore(m.getTokenExpiresAt().minusSeconds(60))) {
            return m; // not expired
        }

        if ("GMAIL".equalsIgnoreCase(m.getProvider())) {
            return refreshGoogleToken(m);
        } else if ("MICROSOFT".equalsIgnoreCase(m.getProvider())) {
            return refreshMicrosoftToken(m);
        }
        return m;
    }

    private MailIntegration refreshGoogleToken(MailIntegration m) {
        String clientId = System.getenv("GMAIL_CLIENT_ID");
        String clientSecret = System.getenv("GMAIL_CLIENT_SECRET");
        String refreshToken = encryptor.decrypt(m.getEncryptedRefreshToken());
        Map<String, Object> resp = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&refresh_token=" + refreshToken +
                        "&grant_type=refresh_token")
                .retrieve().bodyToMono(Map.class).block();

        String accessToken = (String) resp.get("access_token");
        Integer expiresIn = (Integer) resp.get("expires_in");
        m.setEncryptedAccessToken(encryptor.encrypt(accessToken));
        m.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
        repo.save(m);
        return m;
    }

    private MailIntegration refreshMicrosoftToken(MailIntegration m) {
        String clientId = System.getenv("MS_CLIENT_ID");
        String clientSecret = System.getenv("MS_CLIENT_SECRET");
        String refreshToken = encryptor.decrypt(m.getEncryptedRefreshToken());
        Map<String,Object> resp = webClient.post()
                .uri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=" + clientId +
                        "&scope=offline_access%20Mail.ReadWrite%20Mail.Send" +
                        "&refresh_token=" + refreshToken +
                        "&grant_type=refresh_token" +
                        "&client_secret=" + clientSecret)
                .retrieve().bodyToMono(Map.class).block();

        String accessToken = (String) resp.get("access_token");
        String newRefresh = (String) resp.get("refresh_token");
        Integer expiresIn = (Integer) resp.get("expires_in");

        m.setEncryptedAccessToken(encryptor.encrypt(accessToken));
        if (newRefresh != null) {
            m.setEncryptedRefreshToken(encryptor.encrypt(newRefresh));
        }
        m.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
        repo.save(m);
        return m;
    }
}

