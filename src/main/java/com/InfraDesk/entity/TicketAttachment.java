package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ticket association
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    // Original file name (for download/display)
    @Column(nullable = false, length = 255)
    private String originalFileName;

    // Storage path on server (relative or absolute)
    @Column(nullable = false, length = 500)
    private String filePath;

    // Who uploaded
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;

    // File size
    private Long sizeInBytes;

    // MIME type
    @Column(length = 100)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_message_id")
    private TicketMessage ticketMessage;

}
