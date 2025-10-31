package com.InfraDesk.controller;

import com.InfraDesk.dto.AssetHistoryDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.AssetHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/assets")
@RequiredArgsConstructor
public class AssetHistoryController {

    private final AssetHistoryService assetHistoryService;

    @GetMapping("/{assetPublicId}/history")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_MANAGE')")
    public ResponseEntity<PaginatedResponse<AssetHistoryDTO>> getAssetHistory(
            @PathVariable String assetPublicId,
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PaginatedResponse<AssetHistoryDTO> result = assetHistoryService.getAssetHistories(companyId,assetPublicId, page, size);
        return ResponseEntity.ok(result);
    }
}

