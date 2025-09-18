package com.InfraDesk.security;

import com.InfraDesk.entity.Membership;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import com.InfraDesk.repository.DepartmentRepository;
import com.InfraDesk.repository.MembershipRepository;
import com.InfraDesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
//
//        List<SimpleGrantedAuthority> authorities;
//
//        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.COMPANY_CONFIGURE) {
//            // global roles → SUPER_ADMIN, COMPANY_CONFIGURE
//            authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
//
//        } else {
//            // tenant-specific roles via Memberships
//            List<Membership> activeMemberships = membershipRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(user.getId());
//
//            if (activeMemberships.isEmpty()) {
//                throw new UsernameNotFoundException("No active memberships found for user: " + email);
//            }
//
//            authorities = activeMemberships.stream()
//                    .map(m -> new SimpleGrantedAuthority("ROLE_" + m.getRole().name()))
//                    .collect(Collectors.toList());
//        }
//
//        return new CustomUserDetails(
//                user.getEmail(),
//                user.getPassword(),
//                authorities,
//                user.getId(),
//                user.getRole()
//        );
//    }
//
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

    // Start with global user role authority
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

    // Fetch active membership roles
    List<Membership> activeMemberships = membershipRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(user.getId());

    if (!activeMemberships.isEmpty()) {
        // Add membership roles to authorities (typically tenant scoped)
        List<SimpleGrantedAuthority> membershipAuthorities = activeMemberships.stream()
                .map(m -> new SimpleGrantedAuthority("ROLE_" + m.getRole().name()))
                .collect(Collectors.toList());

        // Combine global role with membership roles
        authorities = new java.util.ArrayList<>(authorities);
        authorities.addAll(membershipAuthorities);
    }

    return new CustomUserDetails(
            user.getEmail(),
            user.getPassword(),
            authorities,
            user.getId(),
            user.getRole()
    );
}




}
