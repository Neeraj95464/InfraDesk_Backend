//package com.InfraDesk.mapper;
//
//import com.InfraDesk.dto.TicketMessageDTO;
//import com.InfraDesk.entity.TicketMessage;
//import com.InfraDesk.entity.TicketAttachment;
//
//import java.util.Collections;
//import java.util.stream.Collectors;
//
//public class TicketMessageMapper {
//
//    public static TicketMessageDTO toDto(TicketMessage msg) {
//        if (msg == null) return null;
//
//        return TicketMessageDTO.builder()
//                .publicId(msg.getPublicId())
//                .ticketId(msg.getTicket() != null ? msg.getTicket().getPublicId() : null)
//                .authorId(msg.getAuthor() != null ? msg.getAuthor().getPublicId() : null)
//                .authorName(msg.getAuthor() != null ? msg.getAuthor().getEmployeeProfiles().getFirst().getName() : "System")
//                .body(msg.getBody())
//                .internalNote(msg.getInternalNote())
//                .createdAt(msg.getCreatedAt())
//                .updatedAt(msg.getUpdatedAt())
//                .attachments(
//                        msg.getAttachments() != null
//                                ? msg.getAttachments().stream()
//                                .map(TicketMessageMapper::mapAttachment)
//                                .collect(Collectors.toList())
//                                : Collections.emptyList()
//                )
//                .build();
//    }
//
//
//    private static TicketMessageDTO.AttachmentDTO mapAttachment(TicketAttachment att) {
//        return TicketMessageDTO.AttachmentDTO.builder()
////                .publicId(att.getPublicId())
////                .fileName(att.getFileName())
////                .fileUrl(att.getFileUrl())
//                .build();
//    }
//}
//




package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketMessageDTO;
import com.InfraDesk.entity.TicketAttachment;
import com.InfraDesk.entity.TicketMessage;

import java.util.Collections;
import java.util.stream.Collectors;

public class TicketMessageMapper {

    private static final String ATTACHMENT_DOWNLOAD_BASE_URL = "http://localhost:8080/api/attachments/download/"; // adjust if API path differs

    public static TicketMessageDTO toDto(TicketMessage msg) {
        if (msg == null) return null;

        return TicketMessageDTO.builder()
                .publicId(msg.getPublicId())
                .ticketId(msg.getTicket() != null ? msg.getTicket().getPublicId() : null)
                .authorId(msg.getAuthor() != null ? msg.getAuthor().getPublicId() : null)
                .authorName(msg.getAuthor() != null && !msg.getAuthor().getEmployeeProfiles().isEmpty()
                        ? msg.getAuthor().getEmployeeProfiles().getFirst().getName()
                        : "System")
                .body(msg.getBody())
                .internalNote(msg.getInternalNote())
                .createdAt(msg.getCreatedAt())
                .updatedAt(msg.getUpdatedAt())
                .attachments(
                        msg.getAttachments() != null
                                ? msg.getAttachments().stream()
                                .map(TicketMessageMapper::mapAttachment)
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                )
                .build();
    }

    private static TicketMessageDTO.AttachmentDTO mapAttachment(TicketAttachment att) {
        return TicketMessageDTO.AttachmentDTO.builder()
                .publicId(att.getPublicId())
                .fileName(att.getOriginalFileName())
                .fileUrl(buildFileUrl(att.getPublicId()))
                .build();
    }

    private static String buildFileUrl(String attachmentPublicId) {
        // Construct URL used by client to download the attachment
        return ATTACHMENT_DOWNLOAD_BASE_URL + attachmentPublicId;
    }
}
