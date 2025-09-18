package com.InfraDesk.service;

import com.InfraDesk.dto.CreateTicketRequest;
import com.InfraDesk.entity.*;
import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final TicketNumberService ticketNumberService;
    private final TicketFileStorageService storageService;
    private final TicketAssignmentService assignmentService;
    private final TicketTypeRepository ticketTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository,
                         TicketMessageRepository messageRepository,
                         TicketAttachmentRepository attachmentRepository,
                         TicketNumberService ticketNumberService,
                         TicketFileStorageService storageService,
                         TicketAssignmentService assignmentService,
                         TicketTypeRepository ticketTypeRepository,
                         DepartmentRepository departmentRepository,
                         LocationRepository locationRepository,
                         CompanyRepository companyRepository,
                         UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.ticketNumberService = ticketNumberService;
        this.storageService = storageService;
        this.assignmentService = assignmentService;
        this.ticketTypeRepository = ticketTypeRepository;
        this.departmentRepository = departmentRepository;
        this.locationRepository = locationRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create ticket with optional files.
     */
    @Transactional
    public Ticket createTicket(CreateTicketRequest req, String companyId) throws Exception {
        // 1) Company reference
        Company company = companyRepository.getReferenceById(companyId);

        // 2) Department & Location references
        Department department = (req.getDepartmentId() != null)
                ? departmentRepository.getReferenceById(req.getDepartmentId())
                : null;

        Location location = (req.getLocationId() != null)
                ? locationRepository.getReferenceById(req.getLocationId())
                : null;

        TicketType ticketType = ticketTypeRepository.findById(req.getTicketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticketType"));

        // 3) Allocate ticket sequence (company + department scoped)
        Long seq = ticketNumberService.nextSeq(companyId, req.getDepartmentId());

        // 4) Build publicId like COMP-HR-00001
        String companyCode = company.getName() != null ? company.getName() : "C" + company.getId();
        String deptCode = (department != null) ? department.getName().toUpperCase() : "GEN";
        String publicId = String.format("%s-%s-%05d", companyCode, deptCode, seq);

        // 5) Create Ticket entity
        Ticket t = Ticket.builder()
                .seq(seq)
                .publicId(publicId)
                .company(company)
                .department(department)
                .location(location)
                .subject(req.getSubject())
                .description(req.getDescription())
                .status(req.getStatus() == null ? TicketStatus.OPEN : req.getStatus())
                .priority(req.getPriority() == null ? TicketPriority.MEDIUM : req.getPriority())
                .ticketType(ticketType)
                .createdBy(currentUserId == null ? null : userRepository.findByEmail(currentUserId.getEmail()
                        ).orElseThrow(() -> new BusinessException("current user not found")))
                .build();

        ticketRepository.save(t);

        // 6) Initial message
        TicketMessage msg = TicketMessage.builder()
                .ticket(t)
                .author(currentUserId == null ? null : userRepository.findByEmail(currentUserId.getEmail()
                ).orElseThrow(() -> new BusinessException("current user not found")))
                .body(req.getDescription())
                .internalNote(false)
                .build();
        messageRepository.save(msg);

        // 7) Handle attachments (stored locally in /opt/infradesk/uploads/<ticketId>/)
        if (req.getAttachments() != null) {
            for (MultipartFile file : req.getAttachments()) {
                // Store file locally
                String storagePath = storageService.storeFile(file, t.getPublicId());

                // Save metadata in DB
                TicketAttachment att = TicketAttachment.builder()
                        .ticket(t)
//                        .message(msg)
                        .originalFileName(file.getOriginalFilename())
                        .sizeInBytes(file.getSize())
                        .contentType(file.getContentType())
                        .filePath(storagePath)
                        .uploadedBy(currentUserId)
                        .build();

                attachmentRepository.save(att);
                msg.getAttachments().add(att);
            }
        }

        // 8) Auto-assign ticket
        assignmentService.assignTicket(t);

        // 9) (Optional) Send notifications (email with subject = ticket.subject)

        return t;
    }
}
