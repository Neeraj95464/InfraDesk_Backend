package com.InfraDesk.controller;

import com.InfraDesk.dto.TicketFeedbackRequest;
import com.InfraDesk.dto.TicketFeedbackResponse;
import com.InfraDesk.entity.TicketFeedback;
import com.InfraDesk.service.TicketFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketFeedbackController {
    private final TicketFeedbackService feedbackService;

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Your request received ");
    }

//    @PostMapping("/{ticketId}/feedback")
//    public ResponseEntity<TicketFeedbackResponse> submitFeedback(
//            @PathVariable String ticketId,
//            @RequestBody TicketFeedbackRequest request
//    ) {
//        // Simple validations
//        if (request.getStars() == null || request.getStars() < 1 || request.getStars() > 5) {
//            throw new IllegalArgumentException("Stars must be between 1 and 5");
//        }
//        TicketFeedback feedback = feedbackService.submitFeedback(ticketId, request);
//        TicketFeedbackResponse response = new TicketFeedbackResponse();
//        response.setId(feedback.getId());
//        response.setStars(feedback.getStars());
//        response.setFeedbackText(feedback.getFeedbackText());
//        response.setSubmittedAt(feedback.getSubmittedAt());
//        return ResponseEntity.ok(response);
//    }

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
}

