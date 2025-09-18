package com.InfraDesk.dto;

import com.InfraDesk.enums.Role;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Example payload:
 * {
 *   "rolePermissions": {
 *     "IT_ADMIN": ["CAN_MANAGE_USERS","CAN_MANAGE_ASSETS"],
 *     "IT_MANAGER": ["CAN_VIEW_TICKETS"]
 *   }
 * }
 */
@Data
public class RolePermissionsDTO {
    private Map<Role, List<String>> rolePermissions;
}

