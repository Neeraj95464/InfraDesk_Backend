package com.InfraDesk.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path root = Paths.get("uploads");

    public LocalFileStorageService() {
        try { Files.createDirectories(root); } catch (IOException ignored) {}
    }

    @Override
    public String storeAssetFile(String companyPublicId, String assetPublicId, MultipartFile file) {
        try {
            Path dir = root.resolve(companyPublicId + "/" + assetPublicId);
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            // Return a relative URL; in prod map to CDN/S3 path or controller to serve files
            return "/files/" + companyPublicId + "/" + assetPublicId + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}

