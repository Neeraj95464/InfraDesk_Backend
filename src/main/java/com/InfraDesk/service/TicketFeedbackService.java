package com.InfraDesk.service;

import com.InfraDesk.dto.FeedbackFilterRequest;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketFeedbackDTO;
import com.InfraDesk.dto.TicketFeedbackRequest;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketFeedback;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.TicketFeedbackMapper;
import com.InfraDesk.repository.CompanyAssetTypeRepository;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.TicketFeedbackRepository;
import com.InfraDesk.repository.TicketRepository;
import com.InfraDesk.specification.TicketFeedbackSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketFeedbackService {
    private static final Logger logger = LoggerFactory.getLogger(TicketFeedbackService.class);

    private final TicketRepository ticketRepository;
    private final TicketFeedbackRepository feedbackRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public TicketFeedback submitFeedback(String ticketPublicId, TicketFeedbackRequest request) {
        Ticket ticket = ticketRepository.findByPublicId(ticketPublicId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Optionally prevent duplicate feedback per ticket
        feedbackRepository.findByTicket(ticket).ifPresent(existing -> {
            throw new RuntimeException("Feedback already submitted for this ticket");
        });

        TicketFeedback feedback = TicketFeedback.builder()
                .ticket(ticket)
                .stars(request.getStars())
                .feedbackText(request.getFeedbackText())
                .build();
        return feedbackRepository.save(feedback);
    }



    @Transactional
    public void saveOrUpdateFeedbackStarsOnly(String ticketId, Integer stars) {
        Ticket ticket = ticketRepository.findByPublicId(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        // Save as new record or update existing feedback's stars if it exists
        TicketFeedback feedback = feedbackRepository.findByTicket(ticket)
                .orElse(TicketFeedback.builder().ticket(ticket).build());

        feedback.setStars(stars);
        feedbackRepository.save(feedback);
    }

    @Transactional
    public void saveOrUpdateFeedback(String ticketId, Integer stars, String feedbackText) {
        Ticket ticket = ticketRepository.findByPublicId(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        TicketFeedback feedback = feedbackRepository.findByTicket(ticket)
                .orElse(TicketFeedback.builder().ticket(ticket).build());

        feedback.setStars(stars);
        feedback.setFeedbackText(feedbackText);
        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public boolean existsByTicketAndStars(String ticketId, Integer stars) {
        Ticket ticket = ticketRepository.findByPublicId(ticketId).orElse(null);
        if (ticket == null) return false;
        return feedbackRepository.findByTicket(ticket)
                .map(fb -> Objects.equals(fb.getStars(), stars))
                .orElse(false);
    }

//    public Page<TicketFeedback> getFilteredFeedbacks(String companyId, FeedbackFilterRequest filterRequest, int page, int size, String sortBy, boolean descending) {
//        Company company = companyRepository.findByPublicId(
//                companyId
//        ).orElseThrow(() -> new BusinessException("company not found "));
//
//        Pageable pageable;
//        if (descending) {
//            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
//        } else {
//            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
//        }
//
//        return feedbackRepository.findAll(TicketFeedbackSpecification.filterFeedbacks(filterRequest), pageable);
//    }

//    public Page<TicketFeedback> getFilteredFeedbacks(
//            String companyId,
//            FeedbackFilterRequest filterRequest,
//            int page,
//            int size,
//            String sortBy,
//            boolean descending) {
//
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new BusinessException("company not found"));
//
//        Pageable pageable;
//        if (descending) {
//            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
//        } else {
//            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
//        }
//
//        return feedbackRepository.findAll(
//                TicketFeedbackSpecification.filterFeedbacks(companyId, filterRequest),
//                pageable);
//    }




//        public Page<TicketFeedback> getFilteredFeedbacks(
//                String companyId,
//                FeedbackFilterRequest filterRequest,
//                int page,
//                int size,
//                String sortBy,
//                boolean descending) {
//
//            logger.info("getFilteredFeedbacks called with companyId={}, page={}, size={}, sortBy={}, descending={}",
//                    companyId, page, size, sortBy, descending);
//
//            Company company = companyRepository.findByPublicId(companyId)
//                    .orElseThrow(() -> {
//                        logger.info("Company not found for ID: {}", companyId);
//                        return new BusinessException("company not found");
//                    });
//
//            Pageable pageable;
//            if (descending) {
//                pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
//            } else {
//                pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
//            }
//
//            logger.info("Pageable created: {}", pageable);
//
//            Page<TicketFeedback> pageResult = feedbackRepository.findAll(
//                    TicketFeedbackSpecification.filterFeedbacks(companyId, filterRequest),
//                    pageable
//            );
//
//            logger.info("Feedback page fetched: totalElements={}, totalPages={}, contentSize={}",
//                    pageResult.getTotalElements(), pageResult.getTotalPages(), pageResult.getContent().size());
//
//            return pageResult;
//        }


//    public Page<TicketFeedbackDTO> getFilteredFeedbacks(
//            String companyId,
//            FeedbackFilterRequest filterRequest,
//            int page,
//            int size,
//            String sortBy,
//            boolean descending) {
//
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new BusinessException("company not found"));
//
//        Pageable pageable;
//        if (descending) {
//            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
//        } else {
//            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
//        }
//
//        Page<TicketFeedback> pageResult = feedbackRepository.findAll(
//                TicketFeedbackSpecification.filterFeedbacks(companyId, filterRequest),
//                pageable
//        );
//
//        // Map entity page to DTO page
//        return pageResult.map(TicketFeedbackMapper::toDTO);
//    }

    public PaginatedResponse<TicketFeedbackDTO> getFilteredFeedbacks(
            String companyId,
            FeedbackFilterRequest filterRequest,
            int page,
            int size,
            String sortBy,
            boolean descending) {

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new BusinessException("company not found"));

        Pageable pageable;
        if (descending) {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }

        Page<TicketFeedback> pageResult = feedbackRepository.findAll(
                TicketFeedbackSpecification.filterFeedbacks(companyId, filterRequest),
                pageable
        );

        // Map entity page to DTO page
        Page<TicketFeedbackDTO> dtoPage = pageResult.map(TicketFeedbackMapper::toDTO);

        // Convert Page<T> to your PaginatedResponse<T>
        return PaginatedResponse.of(dtoPage);
    }


}

