package com.InfraDesk.service;

import com.InfraDesk.dto.CompanyAssetTypeDTO;
import com.InfraDesk.entity.CompanyAssetType;
import com.InfraDesk.repository.CompanyAssetTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyAssetTypeService {

    private final CompanyAssetTypeRepository assetTypeRepository;

    @Transactional
    public CompanyAssetTypeDTO addAssetType(String companyId, CompanyAssetTypeDTO dto) {
        if (assetTypeRepository.existsByCompanyIdAndTypeName(companyId, dto.getTypeName())) {
            throw new IllegalArgumentException("Asset type already exists for this company.");
        }
        CompanyAssetType type = CompanyAssetType.builder()
                .companyId(companyId)
                .typeName(dto.getTypeName())
                .description(dto.getDescription())
                .build();
        assetTypeRepository.save(type);
        return toDTO(type);
    }

    public List<CompanyAssetTypeDTO> getAssetTypes(String companyId) {
        return assetTypeRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CompanyAssetTypeDTO toDTO(CompanyAssetType type) {
        return CompanyAssetTypeDTO.builder()
                .id(type.getId())
                .typeName(type.getTypeName())
                .description(type.getDescription())
                .build();
    }
}

