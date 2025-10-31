package com.InfraDesk.service;

import com.InfraDesk.dto.AssetHistoryDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.entity.AssetHistory;
import com.InfraDesk.entity.Company;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.AssetHistoryRepository;
import com.InfraDesk.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetHistoryService {

    private final AssetHistoryRepository assetHistoryRepository;
    private final CompanyRepository companyRepository;

//    public Page<AssetHistoryDTO> getAssetHistories(String companyId, String assetPublicId, int page, int size) {
//        Pageable pageable = PageRequest.of(
//                page,
//                size <= 0 ? 50 : size,
//                Sort.by("modifiedAt").descending()
//        );
//
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new BusinessException("Company Id not found "));
//
//        // Assuming AssetHistoryRepository has method for filtering by asset's publicId
//        Page<AssetHistory> pageResult = assetHistoryRepository.findByAsset_PublicId(assetPublicId, pageable);
//
//        return pageResult.map(AssetHistoryService::toDTO);
//    }

    public PaginatedResponse<AssetHistoryDTO> getAssetHistories(
            String companyId,
            String assetPublicId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size <= 0 ? 50 : size,
                Sort.by("modifiedAt").descending()
        );

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new BusinessException("Company Id not found"));

        Page<AssetHistory> pageResult = assetHistoryRepository.findByAsset_PublicId(assetPublicId, pageable);

        // Map entity page to DTO page
        Page<AssetHistoryDTO> dtoPage = pageResult.map(AssetHistoryService::toDTO);

        // Convert Page<T> to PaginatedResponse<T>
        return PaginatedResponse.of(dtoPage);
    }


    // Mapper static method to convert entity to DTO
    public static AssetHistoryDTO toDTO(AssetHistory history) {
        return AssetHistoryDTO.builder()
                .id(history.getId())
                .assetPublicId(history.getAsset() != null ? history.getAsset().getPublicId() : null)
                .fieldName(history.getFieldName())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .note(history.getNote())
                .modifiedBy(history.getModifiedBy())
                .modifiedAt(history.getModifiedAt())
                .build();
    }
}

