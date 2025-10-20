package com.InfraDesk.auth;

import com.InfraDesk.dto.*;
import com.InfraDesk.entity.Employee;
import com.InfraDesk.entity.Membership;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.MembershipRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.security.CustomUserDetails;
import com.InfraDesk.security.JwtService;
import com.InfraDesk.security.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final MembershipRepository membershipRepository;

//    @Value("${jwt.access-token.expiration}")
//    private long accessTokenExpiration;
//
//    @Value("${jwt.refresh-token.expiration}")
//    private long refreshTokenExpiration;

    // ideally from config
    private final long accessTokenExpiration = 15 * 60 * 1000;   // 15 min
    private final long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Helper to create secure HttpOnly cookie for tokens
//    private ResponseCookie createHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
//        return ResponseCookie.from(name, value)
//                .httpOnly(true)
//                .secure(true) // set false for local dev without HTTPS
//                .path("/")
//                .maxAge(maxAgeSeconds)
//                .sameSite("Lax")
//                .build();
//    }

    private ResponseCookie createHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // set false for local dev without HTTPS
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. Validate credentials
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Build JWT claims (platform level)
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getPublicId());
        claims.put("platformRole", user.getRole().name());

        // 3. Spring Security UserDetails
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        CustomUserDetails userDetails =
                new CustomUserDetails(user.getEmail(), user.getPassword(), authorities, user.getId(),user.getRole());

        // 4. Generate JWTs
        String accessToken = jwtService.generateAccessToken(userDetails, claims);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // 5. Set HttpOnly cookies
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(accessTokenExpiration / 1000)
                .build();

//        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
//                .httpOnly(true)
//                .secure(true)
//                .sameSite("Strict")
//                .path("/")
//                .maxAge(refreshTokenExpiration / 1000)
//                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 6. Collect company memberships
        List<CompanyMembershipDto> companies = new ArrayList<>();
        List<Membership> memberships = membershipRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(user.getId());

        memberships.forEach(mbr -> companies.add(
                CompanyMembershipDto.builder()
                        .id( mbr.getCompany().getPublicId())
                        .name(mbr.getCompany().getName())
                        .membershipRole(mbr.getRole()) // company-level role from Membership
                        .build()
        ));

        // If user is Super Admin but not tied to any company
        if (companies.isEmpty() && user.getRole() == Role.SUPER_ADMIN) {
            companies.add(
                    CompanyMembershipDto.builder()
                            .id("comp_placeholder")
                            .name("No Company Assigned")
                            .membershipRole(Role.SUPER_ADMIN)
                            .build()
            );
        }


        // 7. Build user profile (from Employee or fallback to email)
        String fullName = user.getEmail();
        Optional<Employee> empProfile = user.getEmployeeProfiles().stream()
                .filter(emp -> Boolean.TRUE.equals(emp.getIsActive()) && Boolean.FALSE.equals(emp.getIsDeleted()))
                .findFirst();

        if (empProfile.isPresent()) {
            fullName = empProfile.get().getName();
        }

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(user.getPublicId())
                .email(user.getEmail())
                .fullName(fullName)
                .role(user.getRole().name())
                .build();

        // 8. Default preferences
        PreferencesDto preferences = PreferencesDto.builder()
                .defaultCompanyId(companies.isEmpty() ? null : companies.get(0).id())
                .language("en")
                .timezone("Asia/Kolkata")
                .build();

        // 9. Return professional SaaS login response
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .user(userInfoDto)
                        .companies(companies)
                        .preferences(preferences)
                        .build()
        );
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
//        System.out.println("Refresh token being call");
        // 1. Check token existence
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        // 2. Load user
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is not valid");
        }

        // 3. Build claims (platform-level)
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getPublicId());
        claims.put("platformRole", user.getRole().name());

        // 4. Generate new access token
        String newAccessToken = jwtService.generateAccessToken(userDetails, claims);

        // 5. Set new access token cookie
        ResponseCookie newAccessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(accessTokenExpiration / 1000)
                .build();

        response.addHeader("Set-Cookie", newAccessTokenCookie.toString());

        // 6. Collect company memberships
        List<CompanyMembershipDto> companies = new ArrayList<>();
        List<Membership> memberships = membershipRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(user.getId());

        memberships.forEach(mbr -> companies.add(
                CompanyMembershipDto.builder()
                        .id(mbr.getCompany().getPublicId())
                        .name(mbr.getCompany().getName())
                        .membershipRole(mbr.getRole())
                        .build()
        ));

        if (companies.isEmpty() && user.getRole() == Role.SUPER_ADMIN) {
            companies.add(
                    CompanyMembershipDto.builder()
                            .id("comp_placeholder")
                            .name("No Company Assigned")
                            .membershipRole(Role.SUPER_ADMIN)
                            .build()
            );
        }

        // 7. Get user display name (from first active employee profile, fallback to email)
        String fullName = user.getEmail();
        Optional<Employee> empProfile = user.getEmployeeProfiles().stream()
                .filter(emp -> Boolean.TRUE.equals(emp.getIsActive()) && Boolean.FALSE.equals(emp.getIsDeleted()))
                .findFirst();

        if (empProfile.isPresent()) {
            fullName = empProfile.get().getName();
        }

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(user.getPublicId())
                .email(user.getEmail())
                .fullName(fullName)
                .role(user.getRole().name())
                .build();

        // 8. Preferences
        PreferencesDto preferences = PreferencesDto.builder()
                .defaultCompanyId(companies.isEmpty() ? null : companies.get(0).id())
                .language("en")
                .timezone("Asia/Kolkata")
                .build();

        // 9. Return updated auth response
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .user(userInfoDto)
                        .companies(companies)
                        .preferences(preferences)
                        .build()
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
//        System.out.println("logout response was "+response);

        ResponseCookie clearAccessToken = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)          // same as original cookie
                .sameSite("Strict")
                .path("/")
                .maxAge(0)             // delete immediately
                .build();
        response.addHeader("Set-Cookie", clearAccessToken.toString());

        ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", clearRefreshToken.toString());

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @CookieValue(name = "accessToken", required = false) String accessTokenCookie) {

        if (accessTokenCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing access token");
        }

        String token = accessTokenCookie;
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

}


