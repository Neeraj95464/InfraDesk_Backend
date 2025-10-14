package com.InfraDesk.service;

import com.InfraDesk.dto.*;
import com.InfraDesk.entity.*;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.TicketMapper;
import com.InfraDesk.repository.*;
import com.InfraDesk.specification.TicketSpecification;
import com.InfraDesk.util.AuthUtils;
import com.InfraDesk.util.TicketMessageHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
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
    private final MailIntegrationRepository mailIntegrationRepository;
    private final TicketingDepartmentConfigRepository ticketingDepartmentConfigRepository;
    private final OutboundMailService outboundMailService;
    private final EmployeeService employeeService;
    private final AuthUtils authUtils;
    private final TicketMessageHelper ticketMessageHelper;

    /**
     * Create ticket with optional files.
     */
    @Transactional
    public Ticket createTicket(CreateTicketRequest req, String companyId) throws Exception {
        // 1) Company reference
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(()->new BusinessException("Company not found"));

        User creator;

        if (req.getCreatorEmail() != null && !req.getCreatorEmail().isBlank()) {
            // First try by email
            creator = userRepository.findByEmail(req.getCreatorEmail())
                    .orElseGet(
                            ()->employeeService
                                    .createExternalUserWithMembership(companyId,req.getCreatorEmail(),req.getCreatorEmail())
                    );
        } else {
            // Fallback to authenticated user
            creator = authUtils.getAuthenticatedUser()
                    .orElseThrow(() -> new BusinessException("Authenticated user not found"));
        }

        Department department = departmentRepository
                .findByPublicIdAndCompany_PublicId(req.getDepartmentId(), companyId)
                .orElseThrow(() -> new BusinessException("Department not found "+req.getDepartmentId()));

        Location location = (req.getLocationId() != null)
                ? locationRepository.findByPublicIdAndCompany_PublicId(req.getLocationId(), companyId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"))
                : null;

        TicketType ticketType = ticketTypeRepository
                .findByPublicIdAndCompany_PublicId(req.getTicketTypeId(), companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticketType"));

        // 3) Allocate ticket sequence (company + department scoped)
        Long seq = ticketNumberService.nextSeq(companyId, req.getDepartmentId());

//        String domainSlug = slugifyDomain(company.getDomain(), 10);
        String deptSlug = (department != null) ? slugify(department.getName(), 5) : "GEN";
        String publicId = String.format("%s-%05d", deptSlug, seq);


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
                .createdBy(creator)
                .isDeleted(false)
                .build();

        try{
            ticketRepository.save(t);
        }catch (Exception e){
            System.out.println(e);
        }

        // 6) Initial message
        TicketMessage msg = TicketMessage.builder()
                .ticket(t)
                .author(creator)
                .body(req.getDescription())
                .internalNote(false)
                .emailMessageId(req.getEmailMessageId())
                .inReplyTo(req.getInReplyTo())
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(msg);

        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {

            List<TicketAttachment> attachments = new ArrayList<>();

            for (MultipartFile file : req.getAttachments()) {
                String storedFilePath;
                try {
                    storedFilePath = storageService.storeFile(file, t.getPublicId());

                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }

                TicketAttachment attachment = TicketAttachment.builder()
                        .ticket(t)
                        .originalFileName(file.getOriginalFilename())
                        .filePath(storedFilePath)
                        .sizeInBytes(file.getSize())
                        .contentType(file.getContentType())
                        .uploadedBy(creator)
                        .ticketMessage(msg)
                        .build();

                attachments.add(attachment);
            }

            msg.setAttachments(attachments);
            try{
                messageRepository.save(msg);
            }catch (Exception e){
                System.out.println(e);
            }
        }

        // 8) Auto-assign ticket
        assignmentService.assignTicket(t);

        // After auto-assign ticket
        assignmentService.assignTicket(t);

        sendTicketAckMail(t,company);

        return t;
    }

    public void sendTicketAckMail(Ticket t,Company company){
        try {
            TicketingDepartmentConfig ticketingEmail = ticketingDepartmentConfigRepository
                    .findByCompanyAndDepartment(company,t.getDepartment())
                    .orElseThrow(()->new BusinessException("Ticketing department not found "));
            MailIntegration mailIntegration = mailIntegrationRepository
                    .findByCompanyIdAndMailboxEmail(company.getPublicId(),ticketingEmail.getTicketEmail())
                    .orElseThrow(()->new BusinessException("Associated mail not found for department "+ticketingEmail.getTicketEmail()));

            if (mailIntegration == null || !mailIntegration.getEnabled()) {
                log.warn("No active mail integration found for company {}, skipping notification email", company.getPublicId());

            } else {
                // Prepare recipients
                List<String> toEmails = Collections.singletonList(t.getCreatedBy().getEmail());

                // Collect assigned user emails from ticket assignments if any
                List<String> ccEmails = (t.getAssignee() != null && t.getAssignee().getEmail() != null && !t.getAssignee().getEmail().isBlank())
                        ? Collections.singletonList(t.getAssignee().getEmail())
                        : Collections.emptyList();

                String subject = String.format("Ticket: [%s] %s", t.getPublicId(), t.getSubject());

                // Simple HTML body (customize as needed)
                String htmlBody = String.format(
                        "<p>Dear %s,</p><p>Your ticket <b>%s</b> has been created successfully.</p><p>Subject: %s</p><p>Description: %s</p><p>Status: %s</p>",
                        getUserDisplayName(t.getCreatedBy()), t.getPublicId(), t.getSubject(), t.getDescription(), t.getStatus()
                );

                if(mailIntegration.getProvider().equals("GMAIL")){
                    outboundMailService.sendGmailMessage(mailIntegration,t, toEmails, ccEmails, subject, htmlBody);

                }else {
                    outboundMailService.sendViaGraph(mailIntegration,t,toEmails,ccEmails,subject,htmlBody);
                }

                log.info("Notification email sent for ticket {}", t.getPublicId());
            }
        } catch (Exception mailEx) {
            log.error("Failed to send ticket notification email for ticket {}: {}", t.getPublicId(), mailEx.getMessage(), mailEx);
        }
    }

    public String getUserDisplayName(User user) {
        if (user == null) return "";
        if (user.getEmployeeProfiles() != null && !user.getEmployeeProfiles().isEmpty()) {
            return user.getEmployeeProfiles().get(0).getName();
        }
        return user.getEmail();
    }


    public static String slugify(String input, int maxLen) {
        if (input == null || input.isEmpty()) return "";

        // Uppercase and replace non-alphanumeric sequences with single hyphen
        String cleaned = input.toUpperCase()
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-|-$", ""); // trim starting/ending hyphen

        if (cleaned.length() > maxLen) {
            cleaned = cleaned.substring(0, maxLen);

            // If truncated string ends with hyphen, trim it again
            cleaned = cleaned.replaceAll("-$", "");
        }
        return cleaned;
    }

    public static String slugifyDomain(String domain, int maxLen) {
        if (domain == null) return "";
        // Replace dots with dashes, uppercase only letters, numbers, dashes remain
        String slug = domain.toUpperCase()
                .replaceAll("\\.", "-")
                .replaceAll("[^A-Z0-9\\-]+", "");
        return slug.length() > maxLen ? slug.substring(0, maxLen) : slug;
    }


    @Transactional
    public ImportResult importTicketsFromExcel(InputStream is, String companyId) throws IOException {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int batchSize = 500;
        List<CreateTicketRequest> batch = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Preload reference data for name to ID mapping
            Map<String, Department> departmentsMap = departmentRepository.findByCompany_PublicId(companyId).stream()
                    .collect(Collectors.toMap(d -> d.getName().toLowerCase().trim(), Function.identity()));

            Map<String, TicketType> ticketTypeMap = ticketTypeRepository.findByCompany_PublicId(companyId).stream()
                    .collect(Collectors.toMap(t -> t.getName().toLowerCase().trim(), Function.identity()));

            Map<String, Location> locationMap = locationRepository.findByCompany_PublicId(companyId).stream()
                    .collect(Collectors.toMap(l -> l.getName().toLowerCase().trim(), Function.identity()));

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header
                try {
                    CreateTicketRequest req = new CreateTicketRequest();
                    req.setSubject(getCellValue(row.getCell(0)));
                    req.setCreatorEmail(getCellValue(row.getCell(1)));
                    req.setDescription(getCellValue(row.getCell(2)));

                    // Map ticket type name to ID
                    String ticketTypeName = getCellValue(row.getCell(3)).toLowerCase().trim();
                    TicketType ticketType = ticketTypeMap.get(ticketTypeName);
                    if (ticketType == null) {
                        throw new BusinessException("Ticket Type not found: " + ticketTypeName);
                    }
                    req.setTicketTypeId(ticketType.getPublicId());

                    // Map department name to ID
                    String deptName = getCellValue(row.getCell(4)).toLowerCase().trim();
                    Department department = departmentsMap.get(deptName);
                    if (department == null) {
                        throw new BusinessException("Department not found: " + deptName);
                    }
                    req.setDepartmentId(department.getPublicId());

                    // Map location name to ID (if provided)
                    String locationName = getCellValue(row.getCell(5)).toLowerCase().trim();
                    if (!locationName.isEmpty()) {
                        Location location = locationMap.get(locationName);
                        if (location == null) {
                            throw new BusinessException("Location not found: " + locationName);
                        }
                        req.setLocationId(location.getPublicId());
                    }

                    req.setPriority(parseEnumSafe(TicketPriority.class, getCellValue(row.getCell(6)), TicketPriority.MEDIUM));
                    req.setStatus(parseEnumSafe(TicketStatus.class, getCellValue(row.getCell(7)), TicketStatus.OPEN));

                    batch.add(req);

                    if (batch.size() >= batchSize) {
                        successCount += processBatch(batch, companyId, errors);
                        batch.clear();
                    }
                } catch (Exception e) {
                    errors.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                successCount += processBatch(batch, companyId, errors);
            }
        }

        return new ImportResult(successCount, errors);
    }

    private int processBatch(List<CreateTicketRequest> batch, String companyId, List<String> errors) {
        int count = 0;
        for (int i = 0; i < batch.size(); i++) {
            CreateTicketRequest req = batch.get(i);
            try {
                createTicket(req, companyId);
                count++;
            } catch (Exception e) {
                errors.add("Batch item #" + (i + 1) + ": " + e.getMessage());
            }
        }
        return count;
    }

    private <E extends Enum<E>> E parseEnumSafe(Class<E> enumClass, String value, E defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    public static class ImportResult {
        private int successCount;
        private List<String> errors;

        public ImportResult(int successCount, List<String> errors) {
            this.successCount = successCount;
            this.errors = errors;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    // ✅ Get all tickets with pagination
        public PaginatedResponse<TicketDTO> getAllTickets(String companyId, int page, int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Ticket> tickets = ticketRepository.findByCompany_PublicIdAndIsDeletedFalse(companyId, pageable);

            return toPaginatedResponse(tickets);
        }

        // ✅ Search tickets (by subject, description, or publicId)
        public PaginatedResponse<TicketDTO> searchTickets(String companyId, String search, int page, int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Ticket> tickets;

            if (search == null || search.isBlank()) {
                tickets = ticketRepository.findByCompany_PublicIdAndIsDeletedFalse(companyId, pageable);
            } else {
                // try subject first
                tickets = ticketRepository.findByCompany_PublicIdAndIsDeletedFalseAndSubjectContainingIgnoreCase(companyId, search, pageable);
                if (tickets.isEmpty()) {
                    tickets = ticketRepository.findByCompany_PublicIdAndIsDeletedFalseAndDescriptionContainingIgnoreCase(companyId, search, pageable);
                }
                if (tickets.isEmpty()) {
                    tickets = ticketRepository.findByCompany_PublicIdAndIsDeletedFalseAndPublicIdContainingIgnoreCase(companyId, search, pageable);
                }
            }

            return toPaginatedResponse(tickets);
        }

        // ✅ Get ticket by publicId
        public Optional<TicketDTO> getTicketByPublicId(String companyId, String publicId) {
            return ticketRepository.findByCompany_PublicIdAndIsDeletedFalseAndPublicIdContainingIgnoreCase(companyId, publicId, Pageable.unpaged())
                    .stream()
                    .findFirst()
                    .map(TicketMapper::toDto);
        }

    @Transactional
    public TicketDTO updatePriorityAndStatusForViewUser(String ticketId, String companyId, TicketDTO dto) {
        Ticket ticket = ticketRepository.findByPublicIdAndCompany_PublicId(ticketId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        List<String> changes = new ArrayList<>();

        // Priority update allowed for view users
        if (dto.getPriority() != null) {
            TicketPriority newPriority = dto.getPriority();
            if (!newPriority.equals(ticket.getPriority())) {
                changes.add("Priority changed from " + ticket.getPriority() + " → " + newPriority);
                ticket.setPriority(newPriority);
            }
        }

        // Status update strictly allowed only to change from RESOLVED -> OPEN
        if (dto.getStatus() != null) {
            TicketStatus newStatus = dto.getStatus();
            TicketStatus currentStatus = ticket.getStatus();

            if (newStatus == TicketStatus.OPEN && currentStatus == TicketStatus.RESOLVED) {
                changes.add("Status changed from " + currentStatus + " → " + newStatus);
                ticket.setStatus(newStatus);
            } else if (!newStatus.equals(currentStatus)) {
                throw new BusinessException("Only status change from RESOLVED to OPEN is allowed for your permission");
            }
        }

        Ticket updated = ticketRepository.save(ticket);

        if (!changes.isEmpty()) {
            String body = String.join("\n", changes);
            TicketMessageRequest request = TicketMessageRequest.builder()
                    .ticketId(ticket.getPublicId())
                    .senderEmail(null) // system user
                    .body("Ticket updated:\n" + body)
                    .internalNote(true)
                    .build();
            ticketMessageHelper.addMessage(request, companyId);
        }



        return TicketMapper.toDto(updated);
    }


    @Transactional
    public TicketDTO updateTicket(String ticketId, String companyId, TicketDTO dto) {
        Ticket ticket = ticketRepository.findByPublicIdAndCompany_PublicId(ticketId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));


        List<String> changes = new ArrayList<>();

        // Subject
        if (dto.getSubject() != null && !dto.getSubject().equals(ticket.getSubject())) {
            changes.add("Subject changed from '" + ticket.getSubject() + "' → '" + dto.getSubject() + "'");
            ticket.setSubject(dto.getSubject());
        }

        // Description
        if (dto.getDescription() != null && !dto.getDescription().equals(ticket.getDescription())) {
            changes.add("Description updated");
            ticket.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            TicketStatus newStatus = dto.getStatus();
            TicketStatus currentStatus = ticket.getStatus();

            if (newStatus == TicketStatus.CLOSED) {
                // Disallow direct closing
                throw new BusinessException("Ticket can't be closed directly");
            }
            else if (newStatus == TicketStatus.OPEN && currentStatus == TicketStatus.CLOSED) {
                // Prevent reopening closed ticket
                throw new BusinessException("Closed ticket cannot be reopened");
            }
            else if (!newStatus.equals(currentStatus)) {
                changes.add("Status changed from " + currentStatus + " → " + newStatus);
                ticket.setStatus(newStatus);
                if(newStatus == TicketStatus.RESOLVED){
                    sendTicketResolvedMail(ticket,ticket.getCompany());
                }
            }
        }

        // Priority
        if (dto.getPriority() != null) {
            TicketPriority newPriority = dto.getPriority();
            if (!newPriority.equals(ticket.getPriority())) {
                changes.add("Priority changed from " + ticket.getPriority() + " → " + newPriority);
                ticket.setPriority(newPriority);
            }
        }

        // SLA Due Date
        if (dto.getSlaDueDate() != null && !dto.getSlaDueDate().equals(ticket.getSlaDueDate())) {
            changes.add("SLA Due Date changed from " + ticket.getSlaDueDate() + " → " + dto.getSlaDueDate());
            ticket.setSlaDueDate(dto.getSlaDueDate());
        }

        // Assignee
        if (dto.getAssigneeUserId() != null) {
            User assignee = userRepository.findByPublicId(dto.getAssigneeUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee user not found"));
            if(dto.getStatus()==TicketStatus.CLOSED){
                throw new BusinessException("Closed ticket assignee can't be change");
            }
            else if (ticket.getAssignee() == null || !ticket.getAssignee().getPublicId().equals(assignee.getPublicId())) {
                String oldName = ticket.getAssignee() != null ? ticket.getAssignee().getEmail() : "Unassigned";
                changes.add("Assignee changed from " + oldName + " → " + assignee.getEmail());
                ticket.setAssignee(assignee);
            }
        }

        // Creator
        if (dto.getCreatedByUserId() != null) {
            User creator = userRepository.findByPublicId(dto.getCreatedByUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Creator user not found"));

            if(dto.getStatus()==TicketStatus.CLOSED){
                throw new BusinessException("Closed ticket creator can't be change");
            }
            else if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getPublicId().equals(creator.getPublicId())) {
                String oldName = ticket.getCreatedBy() != null ? ticket.getCreatedBy().getEmail() : "Unknown";
                changes.add("Creator changed from " + oldName + " → " + creator.getEmail());
                ticket.setCreatedBy(creator);
            }
        }

        // Location
        if (dto.getLocationId() != null) {
            Location location = locationRepository
                    .findByPublicIdAndCompany_PublicId(dto.getLocationId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Location not found"));
            if (ticket.getLocation() == null || !ticket.getLocation().getPublicId().equals(location.getPublicId())) {
                String oldName = ticket.getLocation() != null ? ticket.getLocation().getName() : "None";
                changes.add("Location changed from " + oldName + " → " + location.getName());
                ticket.setLocation(location);
            }
        }

        // Department
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository
                    .findByPublicIdAndCompany_PublicId(dto.getDepartmentId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
            if (ticket.getDepartment() == null || !ticket.getDepartment().getPublicId().equals(department.getPublicId())) {
                String oldName = ticket.getDepartment() != null ? ticket.getDepartment().getName() : "None";
                changes.add("Department changed from " + oldName + " → " + department.getName());
                ticket.setDepartment(department);
            }
        }

        // Save updated ticket
        Ticket updated = ticketRepository.save(ticket);

        // Log changes as message
        if (!changes.isEmpty()) {
            String body = String.join("\n", changes);

            TicketMessageRequest request = TicketMessageRequest.builder()
                    .ticketId(ticket.getPublicId())
                    .senderEmail(null) // system
                    .body("Ticket updated:\n" + body)
                    .internalNote(true) // keep as internal note
                    .build();

            ticketMessageHelper.addMessage(request, companyId);
        }

        return TicketMapper.toDto(updated);
    }

    public void sendTicketResolvedMail(Ticket t, Company company) {
        try {
            TicketingDepartmentConfig ticketingEmail = ticketingDepartmentConfigRepository
                    .findByCompanyAndDepartment(company, t.getDepartment())
                    .orElseThrow(() -> new BusinessException("Ticketing department not found "));
            MailIntegration mailIntegration = mailIntegrationRepository
                    .findByCompanyIdAndMailboxEmail(company.getPublicId(), ticketingEmail.getTicketEmail())
                    .orElseThrow(() -> new BusinessException("Associated mail not found for department " + ticketingEmail.getTicketEmail()));

            if (mailIntegration == null || !mailIntegration.getEnabled()) {
                log.warn("No active mail integration found for company {}, skipping notification email", company.getPublicId());
                return;
            }

            // Prepare recipients
            List<String> toEmails = Collections.singletonList(t.getCreatedBy().getEmail());

            // CC assigned user if present
            List<String> ccEmails = (t.getAssignee() != null && t.getAssignee().getEmail() != null && !t.getAssignee().getEmail().isBlank())
                    ? Collections.singletonList(t.getAssignee().getEmail())
                    : Collections.emptyList();

            String subject = String.format("Ticket Resolved: [%s] %s", t.getPublicId(), t.getSubject());

            // Generate rating buttons html (1 to 5 stars linking to rating endpoint)
//            String ratingBaseUrl = "https://yourportal.example.com/ticket/" + t.getPublicId() + "/rate?stars=";

            String ratingBaseUrl = "http://localhost:8080/api/tickets/" + t.getPublicId() + "/feedback?stars=";
//            StringBuilder ratingButtons = new StringBuilder();
//            for (int i = 1; i <= 5; i++) {
//                ratingButtons.append(String.format(
//                        "<a href=\"%s%d\" style=\"text-decoration:none; font-size:24px; margin-right:5px;\">%s</a>",
//                        ratingBaseUrl, i, "&#9733;")); // Unicode star symbol
//            }

            StringBuilder ratingButtons = new StringBuilder();

//            String ratingBaseUrl = "https://yourportal.example.com/tickets/" + t.getPublicId() + "/rate?stars=";

//            StringBuilder ratingButtons = new StringBuilder();
//
//            String ratingBaseUrl = "https://yourportal.example.com/tickets/" + t.getPublicId() + "/rate?stars=";

// Colors from red to green for stars 1 to 5

            String[] colors = {
                    "#ff0000",   // 1 star (red)
                    "#ff4500",   // 2 stars (orange red)
                    "#ffa500",   // 3 stars (orange)
                    "#9acd32",   // 4 stars (yellow green)
                    "#008000"    // 5 stars (green)
            };

            ratingButtons.append("<div style='display:flex; justify-content:center; gap:8px;'>");
            for (int i = 1; i <= 5; i++) {
                ratingButtons.append(String.format(
                        "<a href=\"%s%d\" style=\"text-decoration:none; font-size:32px; color:%s;\">&#9733;</a>",
                        ratingBaseUrl, i, colors[i - 1]));
            }
            ratingButtons.append("</div>");


            // HTML body with an attractive layout and message
            String htmlBody = String.format(
                    "<div style='font-family:sans-serif; color:#333;'>"
                            + "<h2 style='color:#2E86C1;'>Dear %s,</h2>"
                            + "<p>Your ticket <b>%s</b> has been <span style='color:green; font-weight:bold;'>resolved successfully</span>.</p>"
                            + "<p><b>Subject:</b> %s</p>"
                            + "<p><b>Description:</b> %s</p>"
                            + "<p><b>Status:</b> %s</p>"
                            + "<hr style='border:none; border-top:1px solid #ddd;'/>"
                            + "<p>Please rate the service provided by the assignee:</p>"
                            + "<p>%s</p>"
                            + "<hr style='border:none; border-top:1px solid #ddd;'/>"
                            + "<p style='font-style:italic; color:#555;'>If you believe the ticket is not fully resolved, you can reopen it from the portal's ticket chat section. Please do so to ensure your concern is addressed.</p>"
                            + "</div>",
                    getUserDisplayName(t.getCreatedBy()),
                    t.getPublicId(),
                    t.getSubject(),
                    t.getDescription(),
                    t.getStatus(),
                    ratingButtons.toString()
            );

            if (mailIntegration.getProvider().equals("GMAIL")) {
                outboundMailService.sendGmailMessage(mailIntegration, t, toEmails, ccEmails, subject, htmlBody);
            } else {
                outboundMailService.sendViaGraph(mailIntegration, t, toEmails, ccEmails, subject, htmlBody);
            }

            log.info("Ticket resolved notification email sent for ticket {}", t.getPublicId());

        } catch (Exception mailEx) {
            log.error("Failed to send ticket resolved email for ticket {}: {}", t.getPublicId(), mailEx.getMessage(), mailEx);
        }
    }


    // ✅ Delete ticket (soft delete handled by @SQLDelete)
        public void deleteTicket(String id, String companyId) {
            Ticket ticket = ticketRepository.findByPublicIdAndCompany_PublicId(id,companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));
            ticketRepository.delete(ticket); // triggers SQLDelete

            TicketMessageRequest request = TicketMessageRequest.builder()
                    .ticketId(ticket.getPublicId())
                    .senderEmail(null)
                    .body("Ticket "+ticket.getPublicId() +" got deleted")
                    .build();

            ticketMessageHelper.addMessage(request,companyId);
        }

        // Utility to convert Page<Ticket> → PaginatedResponse<TicketDTO>
        private PaginatedResponse<TicketDTO> toPaginatedResponse(Page<Ticket> tickets) {
            return new PaginatedResponse<>(
                    tickets.getContent().stream().map(TicketMapper::toDto).toList(),
                    tickets.getNumber(),
                    tickets.getSize(),
                    tickets.getTotalElements(),
                    tickets.getTotalPages(),
                    tickets.isLast()
            );
        }

    public PaginatedResponse<TicketDTO> filterTickets(TicketFilterRequest req, String companyId) {
        AuthUtils.CurrentUserWithPermissions currentUser =
                authUtils.getCurrentUserWithPermissions(companyId)
                        .orElseThrow(() -> new BusinessException("Auth user not found"));

        Specification<Ticket> spec = TicketSpecification.filterTickets(req, companyId);

        if (currentUser.hasPermission("*") || currentUser.hasPermission(PermissionCode.TICKET_ADMIN.name())) {
            // Admin → no restriction, sees all company tickets
        } else if (currentUser.hasPermission(PermissionCode.TICKET_MANAGE.name())) {
            // Executive → own + assigned
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("createdBy").get("id"), currentUser.getUser().getId()),
                    cb.equal(root.get("assignee").get("id"), currentUser.getUser().getId()) // ensure field matches entity
            ));
        } else if (currentUser.hasPermission(PermissionCode.TICKET_VIEW.name())) {
            // Normal user → only own tickets
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("createdBy").get("id"), currentUser.getUser().getId())
            );
        } else {
            // No ticket access
            return new PaginatedResponse<>(Collections.emptyList(), req.getPage(), req.getSize(), 0, 0, true);
        }

        Page<Ticket> page = ticketRepository.findAll(
                spec,
                PageRequest.of(req.getPage(), req.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return new PaginatedResponse<>(
                page.map(TicketMapper::toDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }


    public List<TicketDTO> filterTicketsNoPaging(TicketFilterRequest req, String companyId) {

        List<Ticket> tickets = ticketRepository.findAll(
                TicketSpecification.filterTickets(req, companyId)
        );

        return tickets.stream()
                .map(TicketMapper::toDto)
                .toList();
    }


    public void writeTicketsToExcel(List<TicketDTO> tickets, OutputStream os) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Tickets");

                // Header style
                CellStyle headerStyle = workbook.createCellStyle();

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {
                        "Ticket Number", "Subject", "Description", "Status", "Priority",
                        "Type", "Created By", "Assignee", "Created At", "Updated At", "SLA Due Date"
                };

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Fill data rows
                int rowIdx = 1;
                for (TicketDTO ticket : tickets) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(ticket.getPublicId()); // Full ticket number
                    row.createCell(1).setCellValue(ticket.getSubject());
                    row.createCell(2).setCellValue(ticket.getDescription() != null ? ticket.getDescription() : "");
                    row.createCell(3).setCellValue(ticket.getStatus().name());
                    row.createCell(4).setCellValue(ticket.getPriority().name());
                    row.createCell(5).setCellValue(ticket.getTicketTypeName() != null ? ticket.getTicketTypeName() : "");
                    row.createCell(6).setCellValue(ticket.getCreatedByUserName() != null ? ticket.getCreatedByUserName() : "");
                    row.createCell(7).setCellValue(ticket.getAssigneeUserName() != null ? ticket.getAssigneeUserName() : "");
                    row.createCell(8).setCellValue(ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "");
                    row.createCell(9).setCellValue(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : "");
                    row.createCell(10).setCellValue(ticket.getSlaDueDate() != null ? ticket.getSlaDueDate().toString() : "");
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(os);
            }
        }

}
