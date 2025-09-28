package com.InfraDesk.component;

import com.InfraDesk.entity.Permission;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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


