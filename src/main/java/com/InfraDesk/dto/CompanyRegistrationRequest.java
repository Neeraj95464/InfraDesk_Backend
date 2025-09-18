package com.InfraDesk.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRegistrationRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Legal name is required")
    private String legalName;

    @NotBlank(message = "Industry is required")
    private String industry;

    private String gstNumber; // optional

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10-15 digits")
    private String contactPhone;

    @NotBlank(message = "Address is required")
    private String address;

    private String logoUrl; // optional

    @NotBlank(message = "Domain is required")
    private String domain;

    // 👤 Admin password for first login
    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String adminPassword;

    private String parentCompanyId;
}
