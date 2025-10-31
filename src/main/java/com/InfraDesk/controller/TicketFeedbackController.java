package com.InfraDesk.controller;

import com.InfraDesk.dto.FeedbackFilterRequest;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketFeedbackDTO;
import com.InfraDesk.entity.TicketFeedback;
import com.InfraDesk.service.TicketFeedbackService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketFeedbackController {
    private static final Logger log = LoggerFactory.getLogger(TicketFeedbackController.class);
    private final TicketFeedbackService feedbackService;

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Your request received ");
    }

    // Accept GET for star rating (from email link)
    @GetMapping("/{ticketId}/feedback")
    public ResponseEntity<?> submitStarFeedback(
            @PathVariable String ticketId,
            @RequestParam Integer stars
    ) {
        if (stars == null || stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().body("Stars must be between 1 and 5");
        }

        // First, check if feedback already submitted
        if (feedbackService.existsByTicketAndStars(ticketId, stars)) {
            // Optionally, you can update the rating if you want to allow changes, or just inform user
            return ResponseEntity.ok().body("<html><body><p>Thank you, feedback already received.</p></body></html>");
        }

        // Save the star feedback
        feedbackService.saveOrUpdateFeedbackStarsOnly(ticketId, stars);

        // Return simple HTML with a form for text feedback
        String html = "<html><body>"
                + "<h3>Rating received: " + stars + " star(s)</h3>"
                + "<form method='post' action='/api/tickets/" + ticketId + "/feedback'>"
                + "<input type='hidden' name='stars' value='" + stars + "'/>"
                + "<label for='feedbackText'>Additional feedback (optional):</label><br/>"
                + "<textarea name='feedbackText' rows='4' cols='40'></textarea><br/>"
                + "<button type='submit'>Submit Feedback</button>"
                + "</form>"
                + "</body></html>";
        return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
    }

    // Accept POST for star+text feedback from the HTML form
    @PostMapping("/{ticketId}/feedback")
    public ResponseEntity<String> submitFullFeedback(
            @PathVariable String ticketId,
            @RequestParam Integer stars,
            @RequestParam(required = false) String feedbackText
    ) {
        if (stars == null || stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().body("Stars must be between 1 and 5");
        }
        feedbackService.saveOrUpdateFeedback(ticketId, stars, feedbackText);
        return ResponseEntity.ok("<html><body><h3>Thank you for your feedback!</h3></body></html>");
    }

    @GetMapping("{companyId}/filter")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_MANAGE')")
    public ResponseEntity<?> getFilteredFeedbacks(
            @PathVariable String companyId,
            @RequestParam(required = false) String assigneeUserId,
            @RequestParam(required = false) Integer stars,
            @RequestParam(required = false) String fromDate,   // ISO datetime string expected, parse in code
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "true") boolean descending
    ) {

//        log.info("Request received for feedback {}",companyId);
        FeedbackFilterRequest filterRequest = FeedbackFilterRequest.builder()
                .assigneeUserId(assigneeUserId)
                .stars(stars)
                .keyword(keyword)
                .build();

        // You will need to parse fromDate and toDate strings to LocalDateTime if provided
        if (fromDate != null && !fromDate.isEmpty()) {
            filterRequest.setFromDate(java.time.LocalDateTime.parse(fromDate));
        }
        if (toDate != null && !toDate.isEmpty()) {
            filterRequest.setToDate(java.time.LocalDateTime.parse(toDate));
        }

        PaginatedResponse<TicketFeedbackDTO> pageResult = feedbackService.getFilteredFeedbacks(companyId, filterRequest, page, size, sortBy, descending);

        return ResponseEntity.ok(pageResult);
    }
}

