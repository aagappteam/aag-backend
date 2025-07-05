package aagapp_backend.controller.admin.vendorsubmission;

import aagapp_backend.dto.GameRequest;
import aagapp_backend.dto.TournamentUpdateRequest;
import aagapp_backend.dto.ticketdto.TicketResponseDto;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.enums.VendorStatus;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminReviewService;
import aagapp_backend.services.admin.InvoiceServiceAdmin;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.faqs.FAQService;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.tournamnetservice.TournamentService;
import jakarta.validation.Valid;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/adminreview")
public class AdminReviewController {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private GameService gameService;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private TournamentService tournamentService;

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

    @PutMapping("/close-ticket/{ticketId}")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId) {
        try {
            // Find the ticket by its ID
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            }

            // Update the status to 'Closed'
            ticket.setStatus(TicketEnum.CLOSED);
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

    @GetMapping("/tickets")
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




}
