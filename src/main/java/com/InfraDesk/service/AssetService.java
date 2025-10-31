package com.InfraDesk.service;

import com.InfraDesk.dto.AssetResponseDTO;
import com.InfraDesk.dto.CreateAssetDTO;
import com.InfraDesk.entity.Asset;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Employee;
import com.InfraDesk.enums.AssetStatus;
import com.InfraDesk.enums.AssetType;
import com.InfraDesk.enums.MediaType;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.*;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;//package com.InfraDesk.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import com.InfraDesk.dto.*;
import com.InfraDesk.entity.*;
import com.InfraDesk.mapper.AssetMapper;
import com.InfraDesk.specification.AssetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);
    private final AssetRepository assetRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetPeripheralRepository peripheralRepository;
    private final AssetMediaRepository mediaRepository;
    private final AssetHistoryRepository historyRepository;
    private final CompanyService companyService;
    private final AssetMapper assetMapper;
    private final AuthUtils authUtils;
    private final CompanyAssetTypeRepository companyAssetTypeRepository;
    private final LocationRepository locationRepository;
    private final SiteRepository siteRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public AssetResponseDTO createAsset(String companyId, CreateAssetDTO dto, List<MultipartFile> files) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // asset tag uniqueness check & generation
        String tag = dto.getAssetTag();
        if (tag == null || tag.isBlank()) {
            tag = companyService.generateNextAssetTag(companyId); // locks company row
        } else {
            if (assetRepository.existsByAssetTagAndCompany_PublicIdAndIsDeletedFalse(tag, companyId)) {
                throw new IllegalArgumentException("asset tag already exists for company");
            }
        }

        Location location = locationRepository.findByPublicIdAndCompany_PublicId(
                dto.getLocationId(),companyId
        ).orElseThrow(()->new BusinessException("Location not found "));

        Site site = siteRepository.findByPublicIdAndCompany_PublicId(
                dto.getSiteId(),companyId
        ).orElseThrow(()->new BusinessException("Site not found "));

        Asset parent = null;
        if (dto.getParentAssetId() != null) {
            parent = assetRepository.findById(dto.getParentAssetId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent asset not found"));
        }
        Employee assignee = null;
        if (dto.getAssigneeEmployeeId() != null) {
            assignee = employeeRepository.findById(dto.getAssigneeEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
        }
        Optional<CompanyAssetType> assetTypeOpt = companyAssetTypeRepository.findByCompanyIdAndTypeName(companyId, dto.getAssetType());

        CompanyAssetType assetType = assetTypeOpt.orElseThrow(() ->
                new BusinessException("Asset type '" + dto.getAssetType() + "' not found for company " + companyId)
        );

        Asset asset = Asset.builder()
                .name(dto.getName())
                .serialNumber(dto.getSerialNumber())
                .assetTag(tag)
                .brand(dto.getBrand())
                .model(dto.getModel())
                .note(dto.getNote())
//                .assetType(dto.getAssetType())
                .assetType(assetType)
                .cost(dto.getCost())
                .description(dto.getDescription())
                .company(company)
                .assignee(assignee)
                .location(location)
                .site(site)
                .reservationStartDate(dto.getReservationStartDate())
                .reservationEndDate(dto.getReservationEndDate())
                .isAssignedToLocation(dto.getIsAssignedToLocation())
                .parentAsset(parent)
                .isDeleted(false)
                .status(dto.getStatus())
                .purchaseDate(dto.getPurchaseDate())
                .purchasedFrom(dto.getPurchasedFrom())
                .warrantyUntil(dto.getWarrantyUntil())
                .createdAt(LocalDateTime.now())
                .build();

        asset = assetRepository.save(asset);

        // handle file uploads
        if (files != null && !files.isEmpty()) {
            if (asset.getMedia() == null) {
                asset.setMedia(new ArrayList<>());
            }
            for (MultipartFile f : files) {
                String url = fileStorageService.storeAssetFile(asset.getCompany().getPublicId(), asset.getPublicId(), f);
                AssetMedia media = AssetMedia.builder()
                        .asset(asset)
                        .fileName(f.getOriginalFilename())
                        .fileUrl(url)
                        .mediaType(f.getContentType() != null && f.getContentType().startsWith("image") ? MediaType.PHOTO : MediaType.DOCUMENT)
                        .uploadedAt(LocalDateTime.now())
                        .build();
                mediaRepository.save(media);
                asset.getMedia().add(media);
            }
        }


        // history entry (created)
        AssetHistory hist = AssetHistory.builder()
                .asset(asset)
                .fieldName("CREATED")
                .oldValue(null)
                .newValue("Asset created with tag " + asset.getAssetTag())
                .modifiedBy("system")
                .modifiedAt(LocalDateTime.now())
                .build();
        historyRepository.save(hist);

        // map to response DTO
        AssetResponseDTO resp = AssetResponseDTO.builder()
                .publicId(asset.getPublicId())
                .name(asset.getName())
                .serialNumber(asset.getSerialNumber())
                .assetTag(asset.getAssetTag())
                .description(asset.getDescription())
                .status(AssetStatus.valueOf(asset.getStatus().name()))
                .companyPublicId(company.getPublicId())
                .assigneePublicId(assignee != null ? assignee.getPublicId() : null)
                .purchaseDate(asset.getPurchaseDate())
                .warrantyUntil(asset.getWarrantyUntil())
                .mediaUrls(asset.getMedia().stream().map(AssetMedia::getFileUrl).collect(Collectors.toList()))
                .build();

        return resp;
    }

    @Transactional
    public AssetResponseDTO updateAsset(String companyId, String assetPublicId,  AssetResponseDTO dto) {
//        log.info("dto was {}",dto.getAssigneeEmployeeId());

        // 1. Find and validate asset and company
        String actorPublicId = authUtils.getAuthenticatedUser()
                .map(User::getPublicId)  // get email if present
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Asset asset = assetRepository.findByPublicId(assetPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        if (!asset.getCompany().getPublicId().equals(companyId)) {
            throw new IllegalArgumentException("Asset does not belong to company");
        }

        List<AssetHistory> changes = new ArrayList<>();

        // 2. For each editable field, compare and update with history tracking

        // name
        if (dto.getName() != null && !dto.getName().equals(asset.getName())) {
            changes.add(buildHistory(asset, "name", asset.getName(), dto.getName(), actorPublicId));
            asset.setName(dto.getName());
        }
        // serialNumber
        if (dto.getSerialNumber() != null && !dto.getSerialNumber().equals(asset.getSerialNumber())) {
            changes.add(buildHistory(asset, "serialNumber", asset.getSerialNumber(), dto.getSerialNumber(), actorPublicId));
            asset.setSerialNumber(dto.getSerialNumber());
        }
        // assetTag (unique per company)
        if (dto.getAssetTag() != null && !dto.getAssetTag().equals(asset.getAssetTag())) {
            if (assetRepository.existsByAssetTagAndCompany_PublicIdAndIsDeletedFalse(String.valueOf(dto.getAssetTag()), companyId)) {
                throw new IllegalArgumentException("Asset tag already exists for company");
            }
            changes.add(buildHistory(asset, "assetTag", asset.getAssetTag(), dto.getAssetTag(), actorPublicId));
            asset.setAssetTag(String.valueOf(dto.getAssetTag()));
        }
        // description
        if (dto.getDescription() != null && !dto.getDescription().equals(asset.getDescription())) {
            changes.add(buildHistory(asset, "description", asset.getDescription(), dto.getDescription(), actorPublicId));
            asset.setDescription(dto.getDescription());
        }
        // note
        if (dto.getNote() != null && !dto.getNote().equals(asset.getNote())) {
            changes.add(buildHistory(asset, "note", asset.getNote(), dto.getNote(), actorPublicId));
            asset.setNote(dto.getNote());
        }
        // brand
        if (dto.getBrand() != null && !dto.getBrand().equals(asset.getBrand())) {
            changes.add(buildHistory(asset, "brand", asset.getBrand(), dto.getBrand(), actorPublicId));
            asset.setBrand(dto.getBrand());
        }
        // model
        if (dto.getModel() != null && !dto.getModel().equals(asset.getModel())) {
            changes.add(buildHistory(asset, "model", asset.getModel(), dto.getModel(), actorPublicId));
            asset.setModel(dto.getModel());
        }
        // cost
        if (dto.getCost() != null && (asset.getCost() == null || dto.getCost().compareTo(asset.getCost()) != 0)) {
            changes.add(buildHistory(asset, "cost", asset.getCost(), dto.getCost(), actorPublicId));
            asset.setCost(dto.getCost());
        }
        // site
        if (dto.getSiteId() != null) {
            String old = asset.getSite() != null ? asset.getSite().getPublicId() : null;
            if (!dto.getSiteId().equals(old)) {
                Site newSite = siteRepository.findByPublicId(dto.getSiteId())
                        .orElseThrow(() -> new IllegalArgumentException("Site not found"));
                changes.add(buildHistory(asset, "siteId", old, dto.getSiteId(), actorPublicId));
                asset.setSite(newSite);
            }
        }
        // location
        if (dto.getLocationId() != null) {
            String old = asset.getLocation() != null ? asset.getLocation().getPublicId() : null;
            if (!dto.getLocationId().equals(old)) {
                Location newLoc = locationRepository.findByPublicId(dto.getLocationId())
                        .orElseThrow(() -> new IllegalArgumentException("Location not found"));
                changes.add(buildHistory(asset, "locationId", old, dto.getLocationId(), actorPublicId));
                asset.setLocation(newLoc);
            }
        }
        // reservation dates
        if (dto.getReservationStartDate() != null && !dto.getReservationStartDate().equals(asset.getReservationStartDate())) {
            changes.add(buildHistory(asset, "reservationStartDate", asset.getReservationStartDate(), dto.getReservationStartDate(), actorPublicId));
            asset.setReservationStartDate(dto.getReservationStartDate());
        }
        if (dto.getReservationEndDate() != null && !dto.getReservationEndDate().equals(asset.getReservationEndDate())) {
            changes.add(buildHistory(asset, "reservationEndDate", asset.getReservationEndDate(), dto.getReservationEndDate(), actorPublicId));
            asset.setReservationEndDate(dto.getReservationEndDate());
        }
        // assetType
        if (dto.getAssetType() != null && !dto.getAssetType().equals(asset.getAssetType())) {
            changes.add(buildHistory(asset, "assetType", asset.getAssetType(), dto.getAssetType(), actorPublicId));
            asset.setAssetType(asset.getAssetType());
        }
        // status
//        if (dto.getStatus() != null && !dto.getStatus().equals(asset.getStatus())) {
//            changes.add(buildHistory(asset, "status", asset.getStatus(), dto.getStatus(), actorPublicId));
//            asset.setStatus(dto.getStatus());
//        }

        // --- STATUS SPECIAL LOGIC ---
        if (dto.getStatus() != null && !dto.getStatus().equals(asset.getStatus())) {
            // Validate note present for status change
            if (dto.getNote() == null || dto.getNote().trim().isEmpty()) {
                throw new BusinessException("Note is required for status changes");
            }

            AssetStatus newStatus = dto.getStatus();

            // Business rules per status
            switch (newStatus) {
                case AVAILABLE:
                    // When making available:
                    // - Assignee must be cleared
                    // - Note must be present (already checked above)
                    if (asset.getAssignee() != null) {
                        changes.add(buildHistory(asset, "assigneeEmployeeId",
                                asset.getAssignee().getPublicId(), null, actorPublicId));
                        asset.setAssignee(null);
                    }
                    break;

                case CHECKED_OUT:
                    // When checking out:
                    // - Assignee must be set (required)
                    if (dto.getAssigneeEmployeeId() == null) {
                        throw new BusinessException("Assignee is required when checking out");
                    }
                    break;

                case IN_REPAIR:
                case DAMAGED:
                case DISPOSED:
                case LOST:
                    // For these statuses, note is mandatory (checked above)
                    // No additional assignee logic here
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported asset status: " + newStatus);
            }

            changes.add(buildHistory(asset, "status", asset.getStatus(), newStatus, actorPublicId));
            changes.add(buildHistory(asset, "note", asset.getNote(), dto.getNote(), actorPublicId));
            asset.setStatus(newStatus);
        }

        // parent asset
        if (dto.getParentAssetId() != null) {
            String old = asset.getParentAsset() != null ? asset.getParentAsset().getPublicId() : null;
            if (!dto.getParentAssetId().equals(old)) {
                Asset newParent = assetRepository.findByPublicId(dto.getParentAssetId())
                        .orElseThrow(() -> new IllegalArgumentException("Parent Asset not found"));
                changes.add(buildHistory(asset, "parentAssetId", old, dto.getParentAssetId(), actorPublicId));
                asset.setParentAsset(newParent);
            }
        }
        // assignee
        if (dto.getAssigneeEmployeeId() != null) {
            String old = asset.getAssignee() != null ? asset.getAssignee().getPublicId() : null;
            if (!dto.getAssigneeEmployeeId().equals(old)) {
                Employee newAssignee = employeeRepository
                        .findByEmployeeIdAndCompany_PublicId(dto.getAssigneeEmployeeId(),companyId)
                        .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
                changes.add(buildHistory(asset, "assigneeEmployeeId", old, dto.getAssigneeEmployeeId(), actorPublicId));
                asset.setAssignee(newAssignee);
            }
        }
        // isAssignedToLocation
        if (dto.getIsAssignedToLocation() != null && !dto.getIsAssignedToLocation().equals(asset.getIsAssignedToLocation())) {
            changes.add(buildHistory(asset, "isAssignedToLocation", asset.getIsAssignedToLocation(), dto.getIsAssignedToLocation(), actorPublicId));
            asset.setIsAssignedToLocation(dto.getIsAssignedToLocation());
        }
        // purchaseDate, purchasedFrom, warrantyUntil
        if (dto.getPurchaseDate() != null && !dto.getPurchaseDate().equals(asset.getPurchaseDate())) {
            changes.add(buildHistory(asset, "purchaseDate", asset.getPurchaseDate(), dto.getPurchaseDate(), actorPublicId));
            asset.setPurchaseDate(dto.getPurchaseDate());
        }
        if (dto.getPurchasedFrom() != null && !dto.getPurchasedFrom().equals(asset.getPurchasedFrom())) {
            changes.add(buildHistory(asset, "purchasedFrom", asset.getPurchasedFrom(), dto.getPurchasedFrom(), actorPublicId));
            asset.setPurchasedFrom(dto.getPurchasedFrom());
        }
        if (dto.getWarrantyUntil() != null && !dto.getWarrantyUntil().equals(asset.getWarrantyUntil())) {
            changes.add(buildHistory(asset, "warrantyUntil", asset.getWarrantyUntil(), dto.getWarrantyUntil(), actorPublicId));
            asset.setWarrantyUntil(dto.getWarrantyUntil());
        }

        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);

        if (!changes.isEmpty())
            historyRepository.saveAll(changes);

        return assetMapper.toDto(asset);
    }


    private AssetHistory buildHistory(Asset asset, String attr, Object oldV, Object newV, String who) {
        return AssetHistory.builder()
                .asset(asset)
                .fieldName(attr)
                .oldValue(oldV == null ? null : oldV.toString())
                .newValue(newV == null ? null : newV.toString())
                .modifiedBy(who)
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    // LIST WITH FILTER AND PAGINATION
    public PaginatedResponse<AssetResponseDTO> filterAssets(AssetFilterRequest req, String companyId) {
        Specification<Asset> spec = AssetSpecification.filterAssets(req, companyId);

        Page<Asset> page = assetRepository.findAll(
                spec,
                PageRequest.of(req.getPage(), req.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<AssetResponseDTO> dtoList = page.stream()
                .map(assetMapper::toDto)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                dtoList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    // DELETE ASSET (soft delete)
    @Transactional
    public void deleteAsset(String assetPublicId) {
        Asset asset = assetRepository.findByPublicId(assetPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        asset.setIsDeleted(true);
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);
    }
}


