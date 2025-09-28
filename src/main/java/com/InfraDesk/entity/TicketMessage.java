package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "ticket_messages", indexes = {
        @Index(name = "idx_msg_ticket_created", columnList = "ticket_id, created_at")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 50)
    private String publicId = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private Boolean internalNote = Boolean.FALSE; // visible to agents only

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // email threading metadata (Message-ID, In-Reply-To)
    @Column(name = "email_message_id", length = 512)
    private String emailMessageId;

    @Column(name = "in_reply_to", length = 512)
    private String inReplyTo;

    @OneToMany(mappedBy = "ticketMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketAttachment> attachments = new ArrayList<>();

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
