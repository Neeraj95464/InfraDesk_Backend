package com.InfraDesk.controller;

import com.InfraDesk.dto.CompanyAssetTypeDTO;
import com.InfraDesk.service.CompanyAssetTypeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies/{companyId}/asset-types")
@RequiredArgsConstructor
public class CompanyAssetTypeController {

    private final CompanyAssetTypeService assetTypeService;

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'ASSET_MANAGE')")
    public ResponseEntity<CompanyAssetTypeDTO> addAssetType(
            @PathVariable String companyId, @RequestBody CompanyAssetTypeDTO dto) {
        return ResponseEntity.ok(assetTypeService.addAssetType(companyId, dto));
    }

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'ASSET_VIEW')")
    public ResponseEntity<List<CompanyAssetTypeDTO>> getAssetTypes(@PathVariable String companyId) {
        return ResponseEntity.ok(assetTypeService.getAssetTypes(companyId));
    }
}
