//package com.InfraDesk.controller;
//
//import com.InfraDesk.entity.Department;
//import com.InfraDesk.service.DepartmentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/departments")
//@RequiredArgsConstructor
//public class DepartmentController {
//
//    private final DepartmentService departmentService;
//
//    // Create Department
//    @PostMapping
//    public ResponseEntity<Department> createDepartment(
//            @PathVariable Long companyId,
//            @RequestBody Department department,
//            @RequestHeader("X-User") String createdBy
//    ) {
//        return ResponseEntity.ok(
//                departmentService.createDepartment(companyId, department, createdBy)
//        );
//    }
//
//    // Get All Departments
//    @GetMapping
//    public ResponseEntity<List<Department>> getDepartments(@PathVariable Long companyId) {
//        return ResponseEntity.ok(departmentService.getDepartmentsByCompany(companyId));
//    }
//
//    @PutMapping("/add/default")
//    public ResponseEntity<List<Department>> addDefaultsDepartments(
//            @PathVariable("companyId") Long companyId
//    ) {
//
//        return ResponseEntity.ok(departmentService.addDefaultDepartments(companyId));
//    }
//
//
//    // Get Department by ID
//    @GetMapping("/{deptId}")
//    public ResponseEntity<Department> getDepartment(
//            @PathVariable Long companyId,
//            @PathVariable Long deptId
//    ) {
//        return ResponseEntity.ok(departmentService.getDepartment(companyId, deptId));
//    }
//
//    // Update Department
//    @PutMapping("/{deptId}")
//    public ResponseEntity<Department> updateDepartment(
//            @PathVariable Long companyId,
//            @PathVariable Long deptId,
//            @RequestBody Department updatedDept
//    ) {
//        return ResponseEntity.ok(departmentService.updateDepartment(companyId, deptId, updatedDept));
//    }
//
//    // Soft Delete Department
//    @DeleteMapping("/{deptId}")
//    public ResponseEntity<Void> deleteDepartment(
//            @PathVariable Long companyId,
//            @PathVariable Long deptId
//    ) {
//        departmentService.deleteDepartment(companyId, deptId);
//        return ResponseEntity.noContent().build();
//    }
//}
//


package com.InfraDesk.controller;

import com.InfraDesk.dto.DepartmentRequestDTO;
import com.InfraDesk.dto.DepartmentResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<PaginatedResponse<DepartmentResponseDTO>> getDepartments(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<DepartmentResponseDTO> departments = departmentService.getDepartments(companyId, page, size);
        PaginatedResponse<DepartmentResponseDTO> response = new PaginatedResponse<>(
                departments.getContent(),
                departments.getNumber(),
                departments.getSize(),
                departments.getTotalElements(),
                departments.getTotalPages(),
                departments.isLast());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @PathVariable String companyId,
            @RequestBody @Validated DepartmentRequestDTO dto){

        DepartmentResponseDTO created = departmentService.createDepartment(companyId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{deptId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable String companyId,
            @PathVariable Long deptId,
            @RequestBody @Validated DepartmentRequestDTO dto){

        DepartmentResponseDTO updated = departmentService.updateDepartment(companyId, deptId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{deptId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable String companyId,
            @PathVariable Long deptId) {

        departmentService.deleteDepartment(companyId, deptId);
        return ResponseEntity.noContent().build();
    }
}
