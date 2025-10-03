package com.InfraDesk.util;

import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.enums.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class DefaultRolePermissions {
    private DefaultRolePermissions() {}

    public static final Map<Role, List<PermissionCode>> MAP = Map.of(
            Role.PARENT_ADMIN, List.of(PermissionCode.values()), // Full permission

            Role.COMPANY_ADMIN, List.of(
                    PermissionCode.EMPLOYEE_VIEW, PermissionCode.EMPLOYEE_MANAGE,
                    PermissionCode.TICKET_VIEW, PermissionCode.TICKET_MANAGE,
                    PermissionCode.ASSET_VIEW, PermissionCode.ASSET_MANAGE,
                    PermissionCode.DEPARTMENT_VIEW, PermissionCode.DEPARTMENT_MANAGE
            ),

            Role.USER, List.of(
                    PermissionCode.TICKET_VIEW, PermissionCode.EMPLOYEE_VIEW, PermissionCode.ASSET_VIEW
            ),
            Role.EXTERNAL_USER, List.of(
                    PermissionCode.TICKET_VIEW
            )
    );
}

