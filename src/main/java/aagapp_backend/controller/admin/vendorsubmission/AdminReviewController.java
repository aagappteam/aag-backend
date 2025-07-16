package aagapp_backend.controller.admin.vendorsubmission;

import aagapp_backend.components.ZonedDateTimeAdapter;
import aagapp_backend.dto.GameRequest;
import aagapp_backend.dto.NotificationRequest;
import aagapp_backend.dto.TournamentUpdateRequest;
import aagapp_backend.dto.WithdrawalRequestResponseDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.payment.PlanUpgradeRequest;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.*;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.payment.PaymentPlanUpgradeRepository;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.repository.withdrawrequest.CustomerWithdrawalRequestRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminReviewService;
import aagapp_backend.services.admin.InvoiceServiceAdmin;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.faqs.FAQService;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.ticket.TicketService;
import aagapp_backend.services.tournamnetservice.TournamentService;
import aagapp_backend.spec.WithdrawalRequestSpecification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

import java.time.LocalDate;

@RestController
@RequestMapping("/adminreview")
public class AdminReviewController {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private NotoficationFirebase notificationFirebase;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private GameService gameService;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private CustomerWithdrawalRequestRepository customerWithdrawalRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentPlanUpgradeRepository paymentPlanUpgradeRepository;

    @Autowired
    private TicketService ticketService;

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

    @Autowired
    private InvoiceServiceAdmin invoiceServiceAdmin;

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
            return responseService.generateErrorResponse("An error occurred while processing the approval request." + e, HttpStatus.INTERNAL_SERVER_ERROR);
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

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable Long ticketId) {
        try {
            return ticketService.getTicketById(ticketId);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while retrieving the ticket: " + e.getMessage()));
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
            ticket.setStatus(TicketEnum.RESOLVED);
            ticket.setUpdatedDate(new java.util.Date());

            // Save the closed ticket back to the database
            ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket closed successfully", ticket, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while closing the ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
/*    @PutMapping("/close-ticket/{ticketId}")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId, @RequestBody Map<String, Object> remark) {
        try {
            // Find the ticket by its ID
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            }

            String remarkString = (String) remark.get("remark");
            // Update the status to 'Closed'
            ticket.setStatus(TicketEnum.CLOSED);
            ticket.setRemark(remarkString);
            ticket.setUpdatedDate(new java.util.Date());

            // Save the closed ticket back to the database
            ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket closed successfully", ticket, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while closing the ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    /*@GetMapping("/tickets")
    public ResponseEntity<?> getTicketsByFilters(
            @RequestParam(required = false) TicketEnum status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
            int pageSize = (limit != null) ? limit : (size != null ? size : 10);

            long openCount = ticketRepository.countByStatus(TicketEnum.OPEN);
            long closedCount = ticketRepository.countByStatus(TicketEnum.CLOSED);
            long totalCount = ticketRepository.count();

            Date start = (startDate != null) ? Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
            Date end = (endDate != null) ? Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

            // Apply filters in memory (for now)
            List<Ticket> filteredTickets = ticketRepository.findAll().stream()
                    .filter(t -> status == null || t.getStatus() == status)
                    .filter(t -> role == null || t.getRole().equalsIgnoreCase(role))
                    .filter(t -> email == null || (t.getEmail() != null && t.getEmail().toLowerCase().contains(email.toLowerCase())))
                    .filter(t -> {
                        if (start != null && end != null) {
                            return !t.getCreatedDate().before(start) && t.getCreatedDate().before(end);
                        } else if (start != null) {
                            return !t.getCreatedDate().before(start);
                        } else if (end != null) {
                            return t.getCreatedDate().before(end);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // Map to DTO with name filter
            List<TicketResponseDto> dtoList = filteredTickets.stream()
                    .map(ticket -> {
                        TicketResponseDto dto = new TicketResponseDto();
                        dto.setId(ticket.getId());
                        dto.setSubject(ticket.getSubject());
                        dto.setDescription(ticket.getDescription());
                        dto.setStatus(ticket.getStatus());
                        dto.setRemark(ticket.getRemark());
                        dto.setCustomerOrVendorId(ticket.getCustomerOrVendorId());
                        dto.setRole(ticket.getRole());
                        dto.setEmail(ticket.getEmail());
                        dto.setCreatedDate(ticket.getCreatedDate());
                        dto.setUpdatedDate(ticket.getUpdatedDate());

                        // Set name depending on role
                        if ("Customer".equalsIgnoreCase(ticket.getRole())) {
                            Long id = ticket.getCustomerOrVendorId();
                            if (id != null) {
                                Optional<CustomCustomer> customerOpt = customCustomerRepository.findById(id);
                                customerOpt.ifPresent(customer -> dto.setName(customer.getName()));
                            } else {
                                dto.setName("Unknown Customer");
                            }
                        } else if ("Vendor".equalsIgnoreCase(ticket.getRole())) {
                            Long id = ticket.getCustomerOrVendorId();
                            if (id != null) {
                                Optional<VendorEntity> vendorOpt = vendorRepository.findById(id);
                                vendorOpt.ifPresent(vendor -> {
                                    String fullName = (vendor.getFirst_name() != null ? vendor.getFirst_name() : "") +
                                            (vendor.getLast_name() != null ? " " + vendor.getLast_name() : "");
                                    dto.setName(fullName.trim());
                                });
                            } else {
                                dto.setName("Unknown Vendor");
                            }
                        }


                        return dto;
                    })
                    .filter(dto -> name == null || (dto.getName() != null && dto.getName().toLowerCase().contains(name.toLowerCase())))
                    .collect(Collectors.toList());

            // Sort by ID in descending order
            dtoList = dtoList.stream()
                    .sorted(Comparator.comparing(TicketResponseDto::getId).reversed())
                    .collect(Collectors.toList());

            // Paginate manually
            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, dtoList.size());
            List<TicketResponseDto> paginatedList = (startIndex < endIndex) ? dtoList.subList(startIndex, endIndex) : Collections.emptyList();


            return responseService.generateSuccessResponseForTicket(
                    "Tickets retrieved successfully",
                    paginatedList,
                    totalCount,
                    openCount,
                    closedCount,
                    HttpStatus.OK
            );

        } catch (DateTimeParseException e) {
            return responseService.generateErrorResponse("Invalid date format. Use YYYY-MM-DD", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while retrieving tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/



    @GetMapping("/tickets")
    public ResponseEntity<?> getFilteredTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) TicketEnum status,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) AssignedTeam assignedTeam,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date updatedDate
    ) {
        try {
            return ticketService.getFilteredTickets(page, size, name, email, mobile, search, subject, description, status, remark, role, priority, assignedTeam, createdDate, updatedDate);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error fetching tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/assign/{ticketId}")
    public ResponseEntity<?> assignTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> payload
    ) {
        try {
            String team = payload.get("assignedTeam");
            String priority = payload.get("priority");


            return ticketService.assignTicket(ticketId, team, priority);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error assigning ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Mark ticket as resolved
    @PutMapping("/resolve/{ticketId}")
    public ResponseEntity<?> resolveTicket(
            @PathVariable Long ticketId
    ) {
        try {
            return ticketService.markTicketAsResolved(ticketId);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error resolving ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/by-assigned-team")
    public ResponseEntity<?> getTicketsByAssignedTeam(
            @RequestParam AssignedTeam assignedTeam,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return ticketService.getTicketsByAssignedTeam(assignedTeam, page, size);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error fetching tickets by assigned team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // 2. Create a new FAQ
    @PostMapping("/createFaqs")
    public ResponseEntity<?> createFAQ(@RequestBody @Valid FAQs faq) {
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


    @GetMapping("/invoices-details")
    public ResponseEntity<?> getInvoices(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String gstn,
            @RequestParam(required = false) String pan,
            @RequestParam(required = false) String panTypeCheck,
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) String invoiceDate,
            @RequestParam(required = false) String recipientState,
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) String placeOfSupply,
            @RequestParam(required = false) String serviceType,
            Pageable pageable
    ) {
        try {
            Page<InvoiceAdmin> invoicePage = invoiceServiceAdmin.getInvoices(
                    id, name, email, mobile, gstn, pan, panTypeCheck, invoiceNo, invoiceDate,
                    recipientState, recipientType, placeOfSupply, serviceType, pageable
            );



            if (invoicePage.isEmpty()) {
                return ResponseService.generateSuccessResponseWithCount("No invoices found", invoicePage.getContent(),invoicePage.getTotalElements(), HttpStatus.OK);
            } else {
                return ResponseService.generateSuccessResponseWithCount("Invoices fetched successfully", invoicePage.getContent(),invoicePage.getTotalElements(),  HttpStatus.OK);
            }
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error fetching invoices: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        try {
            byte[] pdfData = invoiceServiceAdmin.generateInvoicePdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }


    @GetMapping("/previewInvoice/{id}")
    public ResponseEntity<byte[]> previewInvoice(@PathVariable Long id) {
        try {
            byte[] pdfData = invoiceServiceAdmin.generateInvoicePdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice-" + id + ".pdf")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }


    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportInvoicesToExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            byte[] excelData = invoiceServiceAdmin.generateInvoicesExcel(startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoices.xlsx")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/preview-excel")
    public ResponseEntity<String> previewInvoicesExcelAsHtml(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            String htmlContent = invoiceServiceAdmin.generateInvoicesExcelAsHtml(startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                    .body(htmlContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<p>Error generating Excel preview</p>");
        }
    }

    @PostMapping("/provide-customer-bonus")
    public ResponseEntity<?> provideBonus(@RequestParam Long userId, @RequestParam BigDecimal bonusAmount) {
        try {
            // Find user by ID
            CustomCustomer user = customCustomerRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
            }

            // Provide bonus and get updated user
            CustomCustomer updatedUser = customCustomerService.provideBonus(user, bonusAmount);

            if(updatedUser==null){
                return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
            }


            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", updatedUser.getId());
            responseData.put("name", updatedUser.getName());
            responseData.put("newBonusBalance", updatedUser.getBonusBalance());

            return ResponseService.generateSuccessResponse("Bonus provided successfully", responseData, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error providing bonus: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/vendor/status/{id}")
    public ResponseEntity<?> updateVendorStatus(@PathVariable Long id, @RequestParam VendorStatus newStatus) {
        VendorEntity vendor = vendorRepository.findById(id).orElseThrow(() -> new BusinessException("Vendor not found with this id: " + id, HttpStatus.BAD_REQUEST));
        vendor.setStatus(newStatus);
        vendorRepository.save(vendor);
//        return ResponseEntity.ok("Vendor status updated");
        return ResponseService.generateSuccessResponse("Vendor status updated","vendor is "+ newStatus, HttpStatus.OK);
    }

    //user status updated
    @PutMapping("/user/status/{id}")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam VendorStatus newStatus) {
        CustomCustomer user = customCustomerRepository.findById(id).orElseThrow(() -> new BusinessException("User not found with this id: " + id, HttpStatus.BAD_REQUEST));
        user.setStatus(newStatus);
        customCustomerRepository.save(user);
        return ResponseService.generateSuccessResponse("User status updated","user is "+ newStatus, HttpStatus.OK);
    }



    @PutMapping("/game/update/{gameId}")
    public ResponseEntity<?> updateGameByAdmin(@PathVariable Long gameId, @RequestBody GameRequest gameRequest) {
        try {

            Game response = gameService.updateGameByAdmin(gameId, gameRequest);

            return ResponseService.generateSuccessResponse("Game updated successfully", response, HttpStatus.OK);

        } catch (BusinessException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating game details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/league/update/{leagueId}")
    public ResponseEntity<?> updateLeagueByAdmin(@PathVariable Long leagueId, @RequestBody GameRequest gameRequest) {
        try {
            League updatedLeague = leagueService.updateLeagueByAdmin(leagueId, gameRequest);

            return ResponseService.generateSuccessResponse("League updated successfully", updatedLeague, HttpStatus.OK);

        } catch (BusinessException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating league details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/tournament/update/{tournamentId}")
    public ResponseEntity<?> updateTournamentByAdmin(@PathVariable Long tournamentId, @RequestBody TournamentUpdateRequest request) {
        try {
            Tournament updatedTournament = tournamentService.updateTournamentByAdmin(tournamentId, request);
            return ResponseService.generateSuccessResponse("Tournament updated successfully", updatedTournament, HttpStatus.OK);
        } catch (BusinessException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating tournament: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PutMapping("/process-withdrawal/{id}")
    public ResponseEntity<?> updateWithdrawalStatus(@PathVariable Long id,
                                                    @RequestParam WithdrawalStatus status,
                                                    @RequestParam(required = false) String comment) {
        try {
            CustomerWithdrawalRequest request = customerWithdrawalRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));

            if (request.getStatus() != WithdrawalStatus.PENDING) {
                return ResponseService.generateErrorResponse("Request already processed", HttpStatus.BAD_REQUEST);
            }

            request.setStatus(status);
            request.setAdminComment(comment);

            if (status == WithdrawalStatus.PAID) {

                // Notify customer: Request PAID
                Notification notification = new Notification();
                notification.setCustomerId(request.getCustomer().getId());
                notification.setRole("Customer");
                notification.setDescription("Withdrawal Request Paid");
                notification.setDetails("Your withdrawal of Rs." + request.getAmount() + " has been paid successfully.");
                notification.setAmount(request.getAmount().doubleValue());
                notificationRepository.save(notification);
            }

            if (status == WithdrawalStatus.REJECTED) {
                Wallet wallet = walletRepository.findByCustomCustomer_Id(request.getCustomer().getId());
                if (wallet != null) {
                    wallet.setWinningAmount(wallet.getWinningAmount().add(request.getAmount()));
                    walletRepository.save(wallet);
                }

                // Notify customer: Request REJECTED
                Notification notification = new Notification();
                notification.setCustomerId(request.getCustomer().getId());
                notification.setRole("Customer");
                notification.setDescription("Withdrawal Request Rejected");
                notification.setDetails("Your withdrawal of Rs." + request.getAmount() + " has been rejected.");
                notification.setAmount(request.getAmount().doubleValue());
                notificationRepository.save(notification);
            }

            customerWithdrawalRequestRepository.save(request);
            return ResponseService.generateSuccessResponse("Status updated and customer notified successfully","" ,HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating withdrawal status: " + e.getMessage());
        }
    }


    @GetMapping("/view-withdrawal-requests")
    public ResponseEntity<?> filterWithdrawalRequests(


            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(required = false) WithdrawalType withdrawalType,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String customerMobileNumber,
            @RequestParam(required = false) String customerState,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Specification<CustomerWithdrawalRequest> spec = Specification
                .where(WithdrawalRequestSpecification.hasStatus(status))
                .and(WithdrawalRequestSpecification.hasWithdrawalType(withdrawalType))
                .and(WithdrawalRequestSpecification.hasCustomerId(customerId))
                .and(WithdrawalRequestSpecification.requestDateBetween(startDate, endDate))
                .and(WithdrawalRequestSpecification.customerNameContains(customerName))
                .and(WithdrawalRequestSpecification.customerEmailContains(customerEmail))
                .and(WithdrawalRequestSpecification.customerMobileNumberContains(customerMobileNumber))
                .and(WithdrawalRequestSpecification.commonSearch(search))
                .and(WithdrawalRequestSpecification.customerStateEquals(customerState));

        Long totalCount = customerWithdrawalRequestRepository.count(spec);
        Long pendingCount = customerWithdrawalRequestRepository.countByStatus(WithdrawalStatus.PENDING);
        Long rejectedCount = customerWithdrawalRequestRepository.countByStatus(WithdrawalStatus.REJECTED);
        Long paidCount = customerWithdrawalRequestRepository.countByStatus(WithdrawalStatus.PAID);

        Page<CustomerWithdrawalRequest> resultPage = customerWithdrawalRequestRepository.findAll(spec, pageable);

        List<WithdrawalRequestResponseDTO> dtoList = resultPage
                .stream()
                .map(WithdrawalRequestResponseDTO::new)
                .toList();

        return ResponseService.generateSuccessResponseForWithdrwalRequest("Withdrawal requests fetched successfully", dtoList, totalCount ,paidCount,rejectedCount,pendingCount, HttpStatus.OK);

    }


    @GetMapping("/download-withdrawal-requests")
    public ResponseEntity<?> exportWithdrawalsToCsv(
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(required = false) WithdrawalType withdrawalType,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String customerMobileNumber,
            @RequestParam(required = false) String customerState,
            @RequestParam(required = false) String search
    ) {
        try {
            Specification<CustomerWithdrawalRequest> spec = Specification
                    .where(WithdrawalRequestSpecification.hasStatus(status))
                    .and(WithdrawalRequestSpecification.hasWithdrawalType(withdrawalType))
                    .and(WithdrawalRequestSpecification.hasCustomerId(customerId))
                    .and(WithdrawalRequestSpecification.requestDateBetween(startDate, endDate))
                    .and(WithdrawalRequestSpecification.customerNameContains(customerName))
                    .and(WithdrawalRequestSpecification.customerEmailContains(customerEmail))
                    .and(WithdrawalRequestSpecification.customerMobileNumberContains(customerMobileNumber))
                    .and(WithdrawalRequestSpecification.commonSearch(search))
                    .and(WithdrawalRequestSpecification.customerStateEquals(customerState));

            List<CustomerWithdrawalRequest> results = customerWithdrawalRequestRepository.findAll(spec);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(out);

            // CSV Header
            writer.println("ID,CustomerID,Name,Email,Mobile,State,UPI ID,Account Number,Bank Name,Account Holder Name,ifscCode,Amount,Status,Type,Fee,Final Payout,Requested At,Updated At");

            // CSV Rows
            for (CustomerWithdrawalRequest req : results) {
                CustomCustomer customer = req.getCustomer();

                writer.printf("%d,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%s,%s,%.2f,%.2f,%s,%s%n",
                        req.getId(),
                        customer.getId(),
                        safe(customer.getName()),
                        safe(customer.getEmail()),
                        safe(customer.getMobileNumber()),
                        safe(customer.getState()),
                        safe(req.getUpiId()),
                        safe(req.getAccountNumber()),
                        safe(req.getBankName()),
                        safe(req.getAccountHolderName()),
                        safe(req.getIfscCode()),
                        req.getAmount(),
                        req.getStatus(),
                        req.getWithdrawalType(),
//                        req.getProcessingFee(),
//                        req.getFinalPayoutAmount(),
                        req.getRequestDate(),
                        req.getUpdatedAt()
                );
            }

            writer.flush();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=withdrawals.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating CSV: " + e.getMessage());
        }
    }
    private String safe(String val) {
        return val == null ? "" : val.replace(",", " "); // avoid breaking CSV
    }
    @GetMapping("/get-all-plan-upgrade-requests")
    public ResponseEntity<?> getAllPlanUpgradeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String requestedPlanName,

            @RequestParam(required = false) Long requestedPlanId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestDate
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));

            Specification<PlanUpgradeRequest> spec = Specification.where(null);

            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                spec = spec.and((root, query, cb) -> {
                    Expression<String> vendorIdStr = cb.concat("", root.get("vendorId").as(String.class));
                    return cb.or(
                            cb.like(cb.lower(root.get("name")), keyword),
                            cb.like(cb.lower(root.get("email")), keyword),
                            cb.like(cb.lower(vendorIdStr), keyword)
                    );
                });
            }


            if (vendorId != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("vendorId"), vendorId));
            }

            if (requestedPlanId != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("requestedPlanId"), requestedPlanId));
            }

            if (status != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
            }
            if (requestedPlanName != null && !requestedPlanName.isBlank()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("requestedPlanName")), "%" + requestedPlanName.toLowerCase() + "%")
                );
            }


            if (requestDate != null) {
                spec = spec.and((root, query, cb) ->
                        cb.between(root.get("requestDate"),
                                requestDate.atStartOfDay(),
                                requestDate.plusDays(1).atStartOfDay()));
            }

            Page<PlanUpgradeRequest> pagedResult = paymentPlanUpgradeRepository.findAll(spec, pageable);

            // Count totals (for ALL, not just paginated)
            long totalCount = pagedResult.getTotalElements(); // this is your current filtered list
            long totalApproved = paymentPlanUpgradeRepository.countByStatus(RequestStatus.APPROVED);
            long totalRejected = paymentPlanUpgradeRepository.countByStatus(RequestStatus.REJECTED);
            long totalPending = paymentPlanUpgradeRepository.countByStatus(RequestStatus.PENDING);

            List<Map<String, Object>> resultList = pagedResult.getContent().stream().map(r -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", r.getId());
                map.put("vendorId", r.getVendorId());
                map.put("name", r.getName());
                map.put("email", r.getEmail());
                map.put("requestedPlanId", r.getRequestedPlanId());
                map.put("requestedPlanName", r.getRequestedPlanName());
                map.put("status", r.getStatus());
                map.put("requestDate", r.getRequestDate());
                map.put("approvedDate", r.getApprovedDate());
                map.put("adminRemarks", r.getAdminRemarks());
                return map;
            }).collect(Collectors.toList());

            return responseService.generateSuccessResponseForVendorUpgrade(
                    "Upgrade requests fetched",
                    resultList,
                    totalCount,
                    totalApproved,
                    totalRejected,
                    totalPending,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Failed to fetch upgrade requests", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/approve-upgrade")
    public ResponseEntity<?> handleUpgradeRequest(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("requestId") || payload.get("requestId") == null) {
                return responseService.generateErrorResponse("Request ID is required", HttpStatus.BAD_REQUEST);
            }
            if (!payload.containsKey("approve") || payload.get("approve") == null) {
                return responseService.generateErrorResponse("Approve flag is required", HttpStatus.BAD_REQUEST);
            }

            Long requestId = Long.parseLong(payload.get("requestId").toString());
            boolean approve = Boolean.parseBoolean(payload.get("approve").toString());
            String remarks = payload.getOrDefault("remarks", null) != null ? payload.get("remarks").toString() : null;

            PlanUpgradeRequest request = paymentPlanUpgradeRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Upgrade request not found"));

            request.setApprovedDate(LocalDateTime.now());
            request.setAdminRemarks(remarks);
            request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);

            VendorEntity vendorEntity = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new BusinessException("Vendor not found", HttpStatus.BAD_REQUEST));

            String fcmToken = vendorEntity.getFcmToken();
            String notificationTitle;
            String notificationBody;
            String planName = request.getRequestedPlanName();

            if (approve) {
                notificationTitle = planName + " plan approved";
                notificationBody = "Congratulations! Your " + planName + " upgrade has been approved.";
            } else {
                notificationTitle = planName + " plan rejected";
                notificationBody = "We regret to inform you that your " + planName + " upgrade has been rejected.";
            }




            if (vendorEntity.getService_provider_id() != null) {
                Notification notification = new Notification();
                notification.setRole("Vendor");
                notification.setVendorId(vendorEntity.getService_provider_id());
                notification.setName(notificationTitle);
                notification.setDescription(notificationTitle);
                notification.setDetails(notificationBody);
                notificationRepository.save(notification);
            }
            paymentPlanUpgradeRepository.save(request);

            if (fcmToken != null && !fcmToken.isEmpty()) {
                try {
                    notificationFirebase.sendNotification(fcmToken, notificationTitle, notificationBody);
                } catch (Exception e) {
                    throw new BusinessException("Error sending push notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }


            return responseService.generateSuccessResponse("Request handled successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Failed to process upgrade", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



/*    @PostMapping("/approve-upgrade")
    public ResponseEntity<?> handleUpgradeRequest(@RequestBody Map<String, Object> payload) {

        try {

            if (!payload.containsKey("requestId") || payload.get("requestId") == null) {
                return responseService.generateErrorResponse("Request ID is required", HttpStatus.BAD_REQUEST);
            }
            if (!payload.containsKey("approve") || payload.get("approve") == null) {
                return responseService.generateErrorResponse("Approve flag is required", HttpStatus.BAD_REQUEST);
            }

            Long requestId = Long.valueOf(payload.get("requestId").toString());
            boolean approve = Boolean.parseBoolean(payload.get("approve").toString());
            String remarks = payload.containsKey("remarks") && payload.get("remarks") != null
                    ? payload.get("remarks").toString()
                    : null;
            PlanUpgradeRequest request = paymentPlanUpgradeRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Upgrade request not found"));

            if (approve) {
                request.setStatus(RequestStatus.APPROVED);
                request.setApprovedDate(LocalDateTime.now());
                request.setAdminRemarks(remarks);


            } else {
                request.setStatus(RequestStatus.REJECTED);
                request.setApprovedDate(LocalDateTime.now());
                request.setAdminRemarks(remarks);

            }

            VendorEntity vendorEntity = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new BusinessException("Opponent Vendor not found", HttpStatus.BAD_REQUEST));
            String fcmToken = vendorEntity.getFcmToken();

            if(vendorEntity.getService_provider_id()!= null){
                Notification notification = new Notification();
                notification.setRole("Vendor");

                notification.setVendorId(vendorEntity.getService_provider_id());
                notification.setName("Plan Upgrade Approved");

                notification.setDescription("Plan Upgrade Approved");
                notification.setDetails("Your plan upgrade has been approved");
                notificationRepository.save(notification);
            }



            if (fcmToken != null && !fcmToken.isEmpty()) {


                try {
                    String title = "Your plan upgrade has been approved";
                    String body = "Congratulations! Your plan upgrade has been approved.";



                    notificationFirebase.sendNotification(fcmToken, title, body);

                } catch (Exception e) {
                    throw new BusinessException("Error sending notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            paymentPlanUpgradeRepository.save(request);
            return responseService.generateSuccessResponse("Request handled successfully", null ,HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e
            );
            return responseService.generateErrorResponse("Failed to process upgrade: ", HttpStatus.OK);


        }
    }*/

}




