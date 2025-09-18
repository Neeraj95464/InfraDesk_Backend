package com.InfraDesk.util;

import com.InfraDesk.dto.CurrentUserDTO;
import com.InfraDesk.entity.Employee;
import com.InfraDesk.entity.User;
import com.InfraDesk.repository.EmployeeRepository;
import com.InfraDesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;


    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getAuthenticatedEmail() {
        Authentication auth = getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    public Optional<User> getAuthenticatedUser() {
        String email = getAuthenticatedEmail();
        if (email == null) return Optional.empty();

        return userRepository.findByEmail(email)
                .filter(User::getIsActive)
                .filter(u -> !u.getIsDeleted());
    }

    public Optional<Employee> getAuthenticatedEmployee(Long companyId) {
        return getAuthenticatedUser().flatMap(user ->
                employeeRepository.findByUserIdAndCompanyId(user.getId(), companyId)
        );
    }

    /**
     * ✅ Builds CurrentUserDTO combining global and company-specific details
     */
    public Optional<CurrentUserDTO> getCurrentUser(Long companyId) {
        return getAuthenticatedUser().map(user -> {
            Optional<Employee> employeeOpt = employeeRepository.findByUserIdAndCompanyId(user.getId(), companyId);

            if (employeeOpt.isPresent()) {
                Employee emp = employeeOpt.get();
                return new CurrentUserDTO(
                        user.getId(),
                        emp.getEmployeeId(),
                        emp.getName() != null ? emp.getName() : user.getEmail(),
                        user.getEmail(),
                        emp.getCompany().getId(),
                        null
                );
            } else {
                return new CurrentUserDTO(
                        user.getId(),
                        null,
                        user.getEmail(), // fallback as username
                        user.getEmail(),
                        null,
                        null
                );
            }
        });
    }


}


