package com.InfraDesk.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketFileStorageService {

    @Value("${infradesk.file-storage.base-path}")
    private String basePath;

    public String storeFile(MultipartFile file, String ticketId) throws IOException {
        //("Base path: " + basePath);
        Path folder = Paths.get(basePath, ticketId);
        //("Creating folder: " + folder.toAbsolutePath());
        Files.createDirectories(folder);

        String originalFilename = file.getOriginalFilename();
        //("Original filename: " + originalFilename);
        String safeOriginalFilename = originalFilename != null
                ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_")
                : "unknown";
        //("Sanitized filename: " + safeOriginalFilename);

        String safeName = ticketId + "_" + UUID.randomUUID() + "_" + safeOriginalFilename;
        Path targetPath = folder.resolve(safeName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error copying file " + originalFilename + " to " + targetPath);
            e.printStackTrace();
            throw e;
        }

        // Return consistent forward-slashed path for DB
        return ticketId + "/" + safeName;
    }



}
