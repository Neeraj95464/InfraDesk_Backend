package com.InfraDesk.service;


import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeAssetFile(String companyPublicId, String assetPublicId, MultipartFile file);
}

