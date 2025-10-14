package com.InfraDesk.service;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.repository.MailIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailAuthService {

    private final MailIntegrationRepository repo;
    private final SimpleEncryptor encryptor;
    private final WebClient webClient = WebClient.create();

    @Value("${gmail.client.id}")
    private String gmailClientId;

    @Value("${gmail.client.secret}")
    private String gmailClientSecret;

    @Value("${ms.client.id}")
    private String msClientId;

    @Value("${ms.client.secret}")
    private String msClientSecret;

    public MailIntegration refreshIfNeeded(MailIntegration m) {
        if (m.getEncryptedRefreshToken() == null) {
//            System.out.println("No refresh token found, skipping refresh");
            return m;
        }

        if (m.getTokenExpiresAt() != null && Instant.now().isBefore(m.getTokenExpiresAt().minusSeconds(60))) {
            // Token is still valid beyond buffer time
//            System.out.println("Access token still valid, skipping refresh "+m.getProvider());
            return m;
        }

//        System.out.println("Token expired or near expiry, refreshing: Provider=" + m.getProvider());

        if ("GMAIL".equalsIgnoreCase(m.getProvider())) {
            return refreshGoogleToken(m);
        } else if ("MICROSOFT".equalsIgnoreCase(m.getProvider())) {
            return refreshMicrosoftToken(m);
        }

        System.err.println("Unsupported provider for refresh: " + m.getProvider());
        return m;
    }

    private MailIntegration refreshGoogleToken(MailIntegration m) {
        try {
            String clientId = gmailClientId;
            String clientSecret = gmailClientSecret;
            String refreshToken = encryptor.decrypt(m.getEncryptedRefreshToken());

            String body = "client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&refresh_token=" + refreshToken +
                    "&grant_type=refresh_token";

            Map<String, Object> resp = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null) {
                throw new RuntimeException("Token refresh response is null");
            }

//            System.out.println("Google token refresh response: " + resp);

            String accessToken = (String) resp.get("access_token");
            Number expiresInNum = (Number) resp.get("expires_in");
            int expiresIn = expiresInNum != null ? expiresInNum.intValue() : 3600;

            if (accessToken == null) {
                throw new RuntimeException("Missing access_token in Google token refresh response");
            }

            m.setEncryptedAccessToken(encryptor.encrypt(accessToken));
            m.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
            repo.save(m);

//            System.out.println("Google token refreshed successfully");
            return m;

        } catch (WebClientResponseException e) {
            System.err.println("Google token refresh failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Exception during Google token refresh: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private MailIntegration refreshMicrosoftToken(MailIntegration m) {
        try {
            String clientId = msClientId;
            String clientSecret = msClientSecret;
            String refreshToken = encryptor.decrypt(m.getEncryptedRefreshToken());

            String body = "client_id=" + clientId +
                    "&scope=offline_access%20Mail.ReadWrite%20Mail.Send" +
                    "&refresh_token=" + refreshToken +
                    "&grant_type=refresh_token" +
                    "&client_secret=" + clientSecret;

            Map<String, Object> resp = webClient.post()
                    .uri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null) {
                throw new RuntimeException("Token refresh response is null");
            }

//            System.out.println("Microsoft token refresh response: " + resp);

            String accessToken = (String) resp.get("access_token");
            String newRefreshToken = (String) resp.get("refresh_token");
            Number expiresInNum = (Number) resp.get("expires_in");
            int expiresIn = expiresInNum != null ? expiresInNum.intValue() : 3600;

            if (accessToken == null) {
                throw new RuntimeException("Missing access_token in Microsoft token refresh response");
            }

            m.setEncryptedAccessToken(encryptor.encrypt(accessToken));

            if (newRefreshToken != null) {
                m.setEncryptedRefreshToken(encryptor.encrypt(newRefreshToken));
            }

            m.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
            repo.save(m);

//            System.out.println("Microsoft token refreshed successfully");
            return m;

        } catch (WebClientResponseException e) {
            System.err.println("Microsoft token refresh failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Exception during Microsoft token refresh: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getDecryptedAccessToken(MailIntegration m) {
        if (m == null || m.getEncryptedAccessToken() == null) {
            return null;
        }
        return encryptor.decrypt(m.getEncryptedAccessToken());
    }
}
