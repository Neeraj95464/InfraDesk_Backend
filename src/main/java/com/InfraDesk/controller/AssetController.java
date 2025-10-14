//package com.InfraDesk.controller;
//
//import com.InfraDesk.dto.AssetResponseDTO;
//import com.InfraDesk.dto.CreateAssetDTO;
//import com.InfraDesk.service.AssetService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/companies/{companyId}/assets")
//@RequiredArgsConstructor
//public class AssetController {
//
//    private final AssetService assetService;
//
//    @PostMapping
//    @PreAuthorize("@perm.check(#companyId, 'ASSET_MANAGE')")
//    public ResponseEntity<AssetResponseDTO> create(
//            @PathVariable String companyId,
//            @RequestPart("payload") CreateAssetDTO payload,
//            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
//        AssetResponseDTO dto = assetService.createAsset(companyId, payload, files);
//        return ResponseEntity.ok(dto);
//    }
//
//    @PutMapping("/{assetPublicId}")
//    @PreAuthorize("@perm.check(#companyId, 'ASSET_MANAGE')")
//    public ResponseEntity<AssetResponseDTO> update(
//            @PathVariable String companyId,
//            @PathVariable String assetPublicId,
//            @RequestBody AssetResponseDTO payload) {
//        AssetResponseDTO dto = assetService.updateAsset(companyId, assetPublicId, payload);
//        return ResponseEntity.ok(dto);
//    }
//
//
//}
//


package com.InfraDesk.controller;

import com.InfraDesk.dto.AssetFilterRequest;
import com.InfraDesk.dto.AssetResponseDTO;
import com.InfraDesk.dto.CreateAssetDTO;
import com.InfraDesk.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    // CREATE asset
    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'ASSET_MANAGE')")
    public ResponseEntity<AssetResponseDTO> create(
            @PathVariable String companyId,
            @RequestPart("payload") CreateAssetDTO payload,      // expect DTO as 'payload'
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        AssetResponseDTO dto = assetService.createAsset(companyId, payload, files);
        return ResponseEntity.ok(dto);
    }

    // UPDATE asset
    @PutMapping("/{assetPublicId}")
    @PreAuthorize("@perm.check(#companyId, 'ASSET_MANAGE')")
    public ResponseEntity<AssetResponseDTO> update(
            @PathVariable String companyId,
            @PathVariable String assetPublicId,
            @RequestBody AssetResponseDTO payload           // use dedicated update DTO; don't use response DTO for edits
    ) {
        // Authenticated user is handled inside service
        AssetResponseDTO dto = assetService.updateAsset(companyId, assetPublicId, payload);
        return ResponseEntity.ok(dto);
    }

    // LIST assets with filter and pagination
    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'ASSET_VIEW')")
    public ResponseEntity<?> list(
            @PathVariable String companyId,
            @ModelAttribute AssetFilterRequest filter
    ) {
        return ResponseEntity.ok(assetService.filterAssets(filter, companyId));
    }

    // SOFT DELETE
    @DeleteMapping("/{assetPublicId}")
    @PreAuthorize("@perm.check(#companyId, 'ASSET_MANAGE')")
    public ResponseEntity<?> delete(
            @PathVariable String assetPublicId
    ) {
        assetService.deleteAsset(assetPublicId);
        return ResponseEntity.noContent().build();
    }

    // GET single asset by publicId
//    @GetMapping("/{assetPublicId}")
//    @PreAuthorize("@perm.check(#companyId, 'ASSET_VIEW')")
//    public ResponseEntity<AssetResponseDTO> getOne(
//            @PathVariable String companyId,
//            @PathVariable String assetPublicId
//    ) {
//        AssetResponseDTO dto = assetService.getAssetByPublicId(companyId, assetPublicId);
//        return ResponseEntity.ok(dto);
//    }
}

