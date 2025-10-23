package com.InfraDesk.controller;

import com.InfraDesk.dto.EmployeeFilterRequest;
import com.InfraDesk.dto.EmployeeRequestDTO;
import com.InfraDesk.dto.EmployeeResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;

    @PostMapping("/{companyId}/employees")
//    @PreAuthorize("@perm.check(#companyId, 'CAN_MANAGE_USERS')")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_ADMIN')")
    public ResponseEntity<String> createEmployee(
            @PathVariable String companyId,
            @RequestBody @Validated EmployeeRequestDTO dto) {
//        System.out.println("Request received "+dto);
        employeeService.createEmployeeWithUser(companyId, dto);
        return ResponseEntity.ok("Employee and User created successfully");
    }

    @GetMapping("/{companyId}/employees")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_VIEW')")
    public ResponseEntity<PaginatedResponse<EmployeeResponseDTO>> getAllEmployees(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<EmployeeResponseDTO> employees = employeeService.getAllEmployees(companyId, page, size);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{companyId}/employees/{employeeId}")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_VIEW')")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @PathVariable String companyId,
            @PathVariable Long employeeId) {
        EmployeeResponseDTO employee = employeeService.getEmployeeByIdAndCompany(companyId, employeeId);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{companyId}/employees/{employeeId}")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_MANAGE')")
    public ResponseEntity<String> updateEmployee(
            @PathVariable String companyId,
            @PathVariable String employeeId,
            @RequestBody @Validated EmployeeRequestDTO dto) {
        employeeService.updateEmployee(companyId, employeeId, dto);
        return ResponseEntity.ok("Employee updated successfully");
    }

    @DeleteMapping("/{companyId}/employees/{employeeId}")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_ADMIN')")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable String companyId,
            @PathVariable Long employeeId) {
        employeeService.deleteEmployee(companyId, employeeId);
        return ResponseEntity.ok("Employee soft-deleted successfully");
    }

    @PostMapping("/import-employees/{companyId}")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_ADMIN')")
    public ResponseEntity<EmployeeService.ImportResult> importEmployees(
            @PathVariable String companyId,
            @RequestParam("file") MultipartFile file) throws Exception {

        EmployeeService.ImportResult importResult = employeeService.importEmployeesFromExcel(file.getInputStream(), companyId);
        return ResponseEntity.ok(importResult);
    }

//    @PostMapping("{companyId}/filter")
//    public ResponseEntity<Page<EmployeeResponseDTO>> filterEmployees(
//            @PathVariable String companyId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestBody(required = false) EmployeeFilterRequest request) {
//
//        Page<EmployeeResponseDTO> result = employeeService.filterEmployees(request != null ? request : new EmployeeFilterRequest(), companyId, page, size);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("{companyId}/filter")
    public ResponseEntity<PaginatedResponse<EmployeeResponseDTO>> filterEmployees(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody(required = false) EmployeeFilterRequest request) {

//        log.info("request was {} ",request);

        Page<EmployeeResponseDTO> result = employeeService.filterEmployees(
                request != null ? request : new EmployeeFilterRequest(),
                companyId,
                page,
                size
        );

        // Wrap into PaginatedResponse
        return ResponseEntity.ok(PaginatedResponse.of(result));
    }


}
