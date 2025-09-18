package com.InfraDesk.service;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.UserDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Membership;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.UserMapper;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;


    public PaginatedResponse<UserDTO> getAllUsersExcludingUserRole(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> usersPage = userRepository.findByRoleIsNotNullAndRoleNot(Role.USER, pageable);

        List<UserDTO> userDTOs = usersPage.getContent()
                .stream()
                .map(UserMapper::toDTO)
                .toList();

        return new PaginatedResponse<>(
                userDTOs,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
    }


    public Set<String> getCompanyAndSubsidiaryPublicIds(Company rootCompany) {
        Set<String> companyIds = new HashSet<>();
        if (rootCompany == null) return companyIds;

        Queue<String> queue = new LinkedList<>();
        queue.add(rootCompany.getPublicId());
        companyIds.add(rootCompany.getPublicId());

        while (!queue.isEmpty()) {
            String currentPubId = queue.poll();

            // Fetch subsidiaries from repository safely (active and not deleted)
            List<Company> subsidiaries = companyRepository
                    .findByParentCompany_PublicIdAndIsActiveTrueAndIsDeletedFalse(currentPubId);

            for (Company sub : subsidiaries) {
                if (companyIds.add(sub.getPublicId())) {
                    queue.add(sub.getPublicId());
                }
            }
        }
        return companyIds;
    }



//    public PaginatedResponse<UserDTO> searchUsersForCompanyAndSubsidiaries(String rootCompanyPublicId, String keyword, int page, int size) {
//        Company rootCompany = companyRepository.findByPublicId(rootCompanyPublicId)
//                .orElseThrow(() -> new BusinessException("COMPANY_MISSING", "No company found: " + rootCompanyPublicId));
//
//        // Always get root company + subsidiaries starting from the given company
//        Set<String> allowedCompanyPublicIds = getCompanyAndSubsidiaryPublicIds(rootCompany);
//
//        Specification<User> spec = UserSpecification.userHasKeywordAndAnyCompany(allowedCompanyPublicIds, keyword);
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<User> usersPage = userRepository.findAll(spec, pageable);
//
//
//        return new PaginatedResponse<>(
//                UserMapper.toDTO(usersPage),
//                usersPage.getNumber(),
//                usersPage.getSize(),
//                usersPage.getTotalElements(),
//                usersPage.getTotalPages(),
//                usersPage.isLast()
//        );
//    }

    public PaginatedResponse<UserDTO> searchUsersForCompanyAndSubsidiaries(String rootCompanyPublicId, String keyword, int page, int size) {
        Company rootCompany = companyRepository.findByPublicId(rootCompanyPublicId)
                .orElseThrow(() -> new BusinessException("COMPANY_MISSING", "No company found: " + rootCompanyPublicId));

        Set<String> allowedCompanyPublicIds = getCompanyAndSubsidiaryPublicIds(rootCompany);

        Specification<User> spec = UserSpecification.userHasKeywordAndAnyCompany(allowedCompanyPublicIds, keyword);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersPage = userRepository.findAll(spec, pageable);

        List<UserDTO> userDTOs = UserMapper.toDTO(usersPage.getContent());

        return new PaginatedResponse<>(
                userDTOs,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
    }


}
