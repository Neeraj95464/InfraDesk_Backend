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

    /**
     * Stores a file in a subfolder based on ticketId.
     *
     * @param file     file to store
     * @param ticketId ticket ID for grouping
     * @return stored file path (absolute) for saving in DB
     */
    public String storeFile(MultipartFile file, String ticketId) throws IOException {
        // Each ticket will have its own folder
        Path folder = Paths.get(basePath, ticketId);
        Files.createDirectories(folder);

        // File name will include ticketId + random UUID + original name
        String safeName = ticketId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = folder.resolve(safeName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString(); // Save this path in DB
    }
}
