package com.InfraDesk.controller;

import com.InfraDesk.entity.TicketAttachment;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.TicketAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/companies/{companyId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final TicketAttachmentRepository attachmentRepository;

    @Value("${infradesk.file-storage.base-path}")
    private String basePath;

    @GetMapping("/download/{publicId}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String companyId,
            @PathVariable String publicId) {

        TicketAttachment attachment = attachmentRepository
                .findByPublicIdAndTicket_Company_PublicId(publicId,companyId)
                .orElseThrow(() -> new BusinessException("Attachment not found with id: " + publicId));

        if (!attachment.getTicketMessage().getTicket().getCompany().getPublicId().equals(companyId)) {
            // Optional: extra security check to ensure attachment belongs to company
            throw new BusinessException("Attachment does not belong to company: " + companyId);
        }

        Path filePath = Paths.get(basePath).resolve(attachment.getFilePath()).normalize();

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("File not found or not readable: " + attachment.getFilePath());
            }
        } catch (MalformedURLException e) {
            throw new BusinessException("File path is invalid."+ e);
        }

        String contentDisposition = "attachment; filename=\"" + attachment.getOriginalFileName() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getContentType() != null
                                ? attachment.getContentType()
                                : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
