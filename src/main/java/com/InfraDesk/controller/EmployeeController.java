////package com.InfraDesk.controller;
////
////import com.InfraDesk.dto.EmployeeRequestDTO;
////import com.InfraDesk.dto.EmployeeResponseDTO;
////import com.InfraDesk.dto.PaginatedResponse;
////import com.InfraDesk.entity.Employee;
////import com.InfraDesk.service.EmployeeService;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@RestController
////@RequestMapping("/api/employees")
////public class EmployeeController {
////
////    private final EmployeeService employeeService;
////
////    public EmployeeController(EmployeeService employeeService) {
////        this.employeeService = employeeService;
////    }
////
////    @PostMapping
////    public ResponseEntity<String> createEmployee(@RequestBody EmployeeRequestDTO dto) {
////        employeeService.createEmployeeWithUser(dto);
////        return ResponseEntity.ok("Employee and User created successfully");
////    }
////
////    @GetMapping
////    public ResponseEntity<PaginatedResponse<EmployeeResponseDTO>> getAllEmployees(
////            @RequestParam(defaultValue = "0") int page,
////            @RequestParam(defaultValue = "10") int size) {
////
////        PaginatedResponse<EmployeeResponseDTO> paginatedEmployees = employeeService.getAllEmployees(page, size);
////        return ResponseEntity.ok(paginatedEmployees);
////    }
////
////    @GetMapping("/{id}")
////    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
////        return ResponseEntity.ok(employeeService.getEmployeeById(id));
////    }
////
////    @PutMapping("/{id}")
////    public ResponseEntity<String> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
////        employeeService.updateEmployee(id, dto);
////        return ResponseEntity.ok("Employee updated successfully");
////    }
////
////    @DeleteMapping("/{id}")
////    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
////        employeeService.deleteEmployee(id);
////        return ResponseEntity.ok("Employee soft-deleted successfully");
////    }
////}
////
////
//
//
//package com.InfraDesk.controller;
//
//import com.InfraDesk.dto.EmployeeRequestDTO;
//import com.InfraDesk.dto.EmployeeResponseDTO;
//import com.InfraDesk.dto.PaginatedResponse;
//import com.InfraDesk.service.EmployeeService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/employees")
//@RequiredArgsConstructor
//@Validated  // Enables validation on @RequestBody DTOs if annotated with validation constraints
//public class EmployeeController {
//
//    private final EmployeeService employeeService;
//
//    @PostMapping
//    @PreAuthorize("@perm.check(null, 'CAN_MANAGE_USERS')")  // adjust permission check as needed
//    public ResponseEntity<String> createEmployee(@RequestBody @Validated EmployeeRequestDTO dto) {
//        employeeService.createEmployeeWithUser(dto);
//        return ResponseEntity.ok("Employee and User created successfully");
//    }
//
//    @GetMapping
//    @PreAuthorize("@perm.check(null, 'CAN_VIEW_USERS')")  // adjust permission
//    public ResponseEntity<PaginatedResponse<EmployeeResponseDTO>> getAllEmployees(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "id") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir) {
//
//        PaginatedResponse<EmployeeResponseDTO> employees = employeeService.getAllEmployees(page, size, sortBy, sortDir);
//        return ResponseEntity.ok(employees);
//    }
//
//    @GetMapping("/{employeeId}")
//    @PreAuthorize("@perm.check(null, 'CAN_VIEW_USERS')")
//    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long employeeId) {
//        EmployeeResponseDTO employee = employeeService.getEmployeeById(employeeId);
//        return ResponseEntity.ok(employee);
//    }
//
//    @PutMapping("/{employeeId}")
//    @PreAuthorize("@perm.check(null, 'CAN_MANAGE_USERS')")
//    public ResponseEntity<String> updateEmployee(@PathVariable Long employeeId, @RequestBody @Validated EmployeeRequestDTO dto) {
//        employeeService.updateEmployee(employeeId, dto);
//        return ResponseEntity.ok("Employee updated successfully");
//    }
//
//    @DeleteMapping("/{employeeId}")
//    @PreAuthorize("@perm.check(null, 'CAN_MANAGE_USERS')")
//    public ResponseEntity<String> deleteEmployee(@PathVariable Long employeeId) {
//        employeeService.deleteEmployee(employeeId);
//        return ResponseEntity.ok("Employee soft-deleted successfully");
//    }
//}


package com.InfraDesk.controller;

import com.InfraDesk.dto.EmployeeRequestDTO;
import com.InfraDesk.dto.EmployeeResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/{companyId}/employees")
//    @PreAuthorize("@perm.check(#companyId, 'CAN_MANAGE_USERS')")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_ADMIN')")
    public ResponseEntity<String> createEmployee(
            @PathVariable String companyId,
            @RequestBody @Validated EmployeeRequestDTO dto) {
        System.out.println("Request received "+dto);
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
}
