package com.InfraDesk.util;

import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.enums.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//public final class DefaultRolePermissions {
//
//    private DefaultRolePermissions() {} // prevent instantiation
//
//    public static final Map<Role, List<String>> MAP = Map.of(
//            Role.PARENT_ADMIN, List.of(
//                    "CAN_VIEW_DASHBOARD",
//                    "CAN_VIEW_TICKETS",
//                    "CAN_CREATE_TICKETS",
//                    "CAN_MANAGE_USERS",
//                    "CAN_MANAGE_ASSETS",
//                    "CAN_APPROVE_REQUESTS",
//                    "CAN_CONFIGURE_COMPANY",
//                    "CAN_VIEW_DEPARTMENTS"
//            ),
//            Role.COMPANY_CONFIGURE, List.of(
//                    "CAN_VIEW_DASHBOARD",
//                    "CAN_VIEW_TICKETS",
//                    "CAN_CREATE_TICKETS",
//                    "CAN_MANAGE_USERS",
//                    "CAN_MANAGE_ASSETS"
//            ),
//            Role.IT_ADMIN, List.of(
//                    "CAN_VIEW_DASHBOARD",
//                    "CAN_VIEW_TICKETS",
//                    "CAN_CREATE_TICKETS",
//                    "CAN_APPROVE_REQUESTS"
//            ),
//            Role.HR_ADMIN, List.of(
//                    "CAN_VIEW_DASHBOARD",
//                    "CAN_VIEW_TICKETS",
//                    "CAN_CREATE_TICKETS"
//            ),
//            Role.USER, List.of(
//                    "CAN_VIEW_DASHBOARD",
//                    "CAN_VIEW_TICKETS",
//                    "CAN_CREATE_TICKETS"
//            )
//    );
//}

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
            )
    );
}

