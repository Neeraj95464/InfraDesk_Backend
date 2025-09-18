package com.InfraDesk.util;

import java.util.List;

public class ValidationUtils {
    private ValidationUtils() {} // prevent instantiation

    public static String normalizeDomain(String domain) {
        return domain.toLowerCase().trim();
    }

    public static boolean isPublicEmailDomain(String domain) {
        List<String> blockedDomains = List.of("gmail.com", "yahoo.com", "outlook.com", "hotmail.com");
        return blockedDomains.contains(domain.toLowerCase());
    }
}

