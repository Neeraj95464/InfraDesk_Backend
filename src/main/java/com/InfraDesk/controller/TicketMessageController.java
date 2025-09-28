package com.InfraDesk.controller;

import com.InfraDesk.dto.ApiResponse;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketMessageDTO;
import com.InfraDesk.dto.TicketMessageRequest;
import com.InfraDesk.service.TicketMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/tickets/{ticketId}/messages")
@RequiredArgsConstructor
public class TicketMessageController {

    private final TicketMessageService ticketMessageService;

//    @PostMapping
//    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
//    public ResponseEntity<ApiResponse<TicketMessageDTO>> addMessage(
//            @PathVariable String companyId,
//            @PathVariable String ticketId,
//            @Valid @RequestBody TicketMessageRequest req
//    ) {
//        req.setTicketId(ticketId);
//
//        TicketMessageDTO created = ticketMessageService.addMessage(req, companyId);
//
//        return ResponseEntity.ok(
//                ApiResponse.<TicketMessageDTO>builder()
//                        .success(true)
//                        .message("Message added to ticket")
//                        .data(created)
//                        .build()
//        );
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<ApiResponse<TicketMessageDTO>> addMessage(
            @PathVariable String companyId,
            @PathVariable String ticketId,
            @Valid @ModelAttribute TicketMessageRequest req   // <-- must use @ModelAttribute for multipart!
    ) throws Exception {
        req.setTicketId(ticketId);


        TicketMessageDTO created = ticketMessageService.addMessage(req, companyId);

        return ResponseEntity.ok(
                ApiResponse.<TicketMessageDTO>builder()
                        .success(true)
                        .message("Message added to ticket")
                        .data(created)
                        .build()
        );
    }


    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<ApiResponse<PaginatedResponse<TicketMessageDTO>>> getMessages(
            @PathVariable String companyId,
            @PathVariable String ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<TicketMessageDTO> messagesPage =
                ticketMessageService.getMessagesByTicket(companyId, ticketId, PageRequest.of(page, size));

        return ResponseEntity.ok(
                ApiResponse.<PaginatedResponse<TicketMessageDTO>>builder()
                        .success(true)
                        .message("Ticket messages retrieved")
                        .data(messagesPage)
                        .build()
        );
    }
}
