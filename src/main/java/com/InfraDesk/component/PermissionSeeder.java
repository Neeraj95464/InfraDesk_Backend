package com.InfraDesk.component;

import com.InfraDesk.entity.Permission;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

//@Component
//@RequiredArgsConstructor
//public class PermissionSeeder implements CommandLineRunner {
//
//    private final PermissionRepository permissionRepository;
//
//    @Override
//    public void run(String... args) {
//        // Add/merge standard permissions here
//        List<Permission> standard = List.of(
//                new Permission("CAN_VIEW_DASHBOARD", "Access dashboards"),
//                new Permission("CAN_VIEW_CREATE_TICKETS", "View tickets"),
//                new Permission("CAN_MANAGE_TICKETS", "Configure company settings"),
////                new Permission("CAN_CREATE_TICKETS", "Create tickets"),
//                new Permission("CAN_MANAGE_USERS", "Create/Update users"),
//                new Permission("CAN_VIEW_USERS", "Can only view users"),
//                new Permission("CAN_MANAGE_ASSETS", "Manage assets"),
//                new Permission("CAN_VIEW_ASSETS", "Can only view assets"),
//                new Permission("CAN_REQUEST", "Can only view users"),
//                new Permission("CAN_APPROVE_REQUESTS", "Approve requests"),
//                new Permission("CAN_CONFIGURE_COMPANY", "Configure company settings"),
//                new Permission("CAN_MANAGE_DEPARTMENTS", "Can manage departments"),
//                new Permission("CAN_VIEW_DEPARTMENTS", "Can only view users")
//
//        );
//
//        standard.forEach(p -> {
//            permissionRepository.findById(p.getCode()).orElseGet(() -> permissionRepository.save(p));
//        });
//    }
//}



@Component
@RequiredArgsConstructor
public class PermissionSeeder implements CommandLineRunner {
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        Arrays.stream(PermissionCode.values()).forEach(code ->
                permissionRepository.findById(code).orElseGet(() ->
                        permissionRepository.save(new Permission(code, "Permission for " + code.name()))
                )
        );
    }
}


