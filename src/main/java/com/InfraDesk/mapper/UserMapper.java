package com.InfraDesk.mapper;

import com.InfraDesk.dto.UserDTO;
import com.InfraDesk.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        return UserDTO.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
    public static List<UserDTO> toDTO(List<User> users) {
        if (users == null) return Collections.emptyList();

        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

}

