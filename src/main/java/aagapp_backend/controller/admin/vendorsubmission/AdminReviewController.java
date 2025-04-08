package aagapp_backend.controller.admin.vendorsubmission;

import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminReviewService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.faqs.FAQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/adminreview")
public class AdminReviewController {

    private AdminReviewService reviewService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private TicketRepository ticketRepository;
    private FAQService faqService;

    @Autowired
    public void setFAQService(FAQService faqService) {
        this.faqService = faqService;
    }

    @Autowired
    public void setTicketRepository(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }


    @Autowired
    public void setReviewService(AdminReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveSubmission(@PathVariable Long id) {
        try {
            AdminReviewService.SubmissionResponse response = (AdminReviewService.SubmissionResponse) reviewService.reviewSubmission(id, true); // true for approval

            if (response.getMessage().contains("already")) {
                return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);
            } else if (response.getMessage().contains("not found")) {
                return responseService.generateErrorResponse(response.getMessage(), HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while processing the approval request." +e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectSubmission(@PathVariable Long id) {
        try {
            AdminReviewService.SubmissionResponse response = (AdminReviewService.SubmissionResponse) reviewService.reviewSubmission(id, false); // false for rejection

            if (response.getMessage().contains("already")) {
                return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);
            } else if (response.getMessage().contains("not found")) {
                return responseService.generateErrorResponse(response.getMessage(), HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while processing the rejection request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-ticket/{ticketId}")
    public ResponseEntity<?> updateTicket(@PathVariable Long ticketId, @RequestBody Ticket updatedTicket) {
        try {
            // Find the existing ticket by its ID
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            }

            // Update the ticket fields
            ticket.setSubject(updatedTicket.getSubject());
            ticket.setDescription(updatedTicket.getDescription());
            ticket.setStatus(updatedTicket.getStatus());
            ticket.setUpdatedDate(new java.util.Date());

            // Save the updated ticket back to the database
            ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket updated successfully", ticket, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while updating the ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-ticket/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId) {
        try {
            // Find the ticket by its ID
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            }

            // Delete the ticket
            ticketRepository.delete(ticket);

            return responseService.generateSuccessResponse("Ticket deleted successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while deleting the ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/resolve-ticket/{ticketId}")
    public ResponseEntity<?> resolveTicket(@PathVariable Long ticketId) {
        try {
            // Find the ticket by its ID
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            }

            // Update the status to 'Resolved'
            ticket.setStatus("Resolved");
            ticket.setUpdatedDate(new java.util.Date());

            // Save the resolved ticket back to the database
            ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket resolved successfully", ticket, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while resolving the ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/close-ticket/{ticketId}")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId) {
        try {
            // Find the ticket by its ID
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            }

            // Update the status to 'Closed'
            ticket.setStatus("Closed");
            ticket.setUpdatedDate(new java.util.Date());

            // Save the closed ticket back to the database
            ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket closed successfully", ticket, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while closing the ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getTicketsByStatusAndRole(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<Ticket> ticketPage;

            // Filter by status and role
            if ((status == null || status.isEmpty()) && (role == null || role.isEmpty())) {
                ticketPage = ticketRepository.findAll(pageable);
            } else if (status != null && !status.isEmpty() && (role == null || role.isEmpty())) {
                ticketPage = ticketRepository.findByStatus(status, pageable);
            } else if ((status == null || status.isEmpty()) && role != null && !role.isEmpty()) {
                ticketPage = ticketRepository.findByRole(role, pageable);
            } else {
                ticketPage = ticketRepository.findByStatusAndRole(status, role, pageable);
            }

            // Check if tickets were found
            if (ticketPage.isEmpty()) {
                return responseService.generateErrorResponse("No tickets found with the given filters", HttpStatus.NOT_FOUND);
            }

            return responseService.generateSuccessResponseWithCount("Tickets retrieved successfully", ticketPage.getContent(), ticketPage.getTotalElements(),HttpStatus.OK);        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while retrieving tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 2. Create a new FAQ
    @PostMapping("/createFaqs")
    public ResponseEntity<?> createFAQ(@RequestBody FAQs faq) {
        try {
            FAQs createdFAQ = faqService.createFAQ(faq);
            return ResponseService.generateSuccessResponse("FAQ created successfully", createdFAQ, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error creating FAQ: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateFAQ(@PathVariable Long id, @RequestBody FAQs faqDetails) {
        try {
            FAQs updatedFAQ = faqService.updateFAQ(id, faqDetails);

            if (updatedFAQ != null) {
                return ResponseService.generateSuccessResponse("FAQ updated successfully", updatedFAQ, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("FAQ not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error updating FAQ: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFAQ(@PathVariable Long id) {
        try {
            boolean isDeleted = faqService.deleteFAQ(id);

            if (isDeleted) {
                return ResponseService.generateSuccessResponse("FAQ deleted successfully", null, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("FAQ not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error deleting FAQ: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
