package com.InfraDesk.mapper;

import com.InfraDesk.dto.GroupDTO;
import com.InfraDesk.entity.Group;

import java.util.stream.Collectors;

public class GroupMapper {

    private GroupMapper() {
        // Utility class
    }

    public static GroupDTO toDTO(Group entity) {
        if (entity == null) return null;

        return GroupDTO.builder()
                .publicId(entity.getPublicId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .userIds(entity.getUsers() != null ?
                        entity.getUsers().stream().map(u -> u.getId()).collect(Collectors.toSet())
                        : null)
                .userNames(entity.getUsers() != null ?
                        entity.getUsers().stream().map(u -> u.getEmployeeProfiles().getFirst().getName()).collect(Collectors.toSet())
                        : null)
                .build();
    }
}
