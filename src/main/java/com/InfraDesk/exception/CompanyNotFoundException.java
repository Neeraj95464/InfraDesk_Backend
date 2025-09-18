package com.InfraDesk.exception;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String publicId) {
        super("Company not found with publicId: " + publicId);
    }
}

