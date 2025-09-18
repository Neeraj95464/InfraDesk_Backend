package com.InfraDesk.security;

import com.InfraDesk.enums.Role;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Data
public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long userId;
    @Getter
    private final Role role;
//    private final boolean isActive;

    public CustomUserDetails(String email, String password, Collection<? extends GrantedAuthority> authorities, Long userId, Role role) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.userId = userId;
//        this.isActive = isActive;
        this.role = role;
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}

