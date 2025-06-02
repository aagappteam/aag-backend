package aagapp_backend.controller.admin.vendorsubmission;

import aagapp_backend.dto.ticketdto.TicketResponseDto;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminReviewService;
import aagapp_backend.services.admin.InvoiceServiceAdmin;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.faqs.FAQService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adminreview")
public class AdminReviewController {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private VendorRepository vendorRepository;

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
    public ResponseEntity<?> getTicketsByStatusAndRole(
            @RequestParam(required = false) TicketEnum status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
//           Pageable pageable = PageRequest.of(page, size);

            int pageSize = (limit != null) ? limit : (size != null ? size : 10);

           Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")));

            Page<Ticket> ticketPage;

            // Filter by status and role
            if (status == null && (role == null || role.isEmpty())) {
                ticketPage = ticketRepository.findAll(pageable);
            } else if (status != null && (role == null || role.isEmpty())) {
                ticketPage = ticketRepository.findByStatus(status, pageable);
            } else if ((status == null) && role != null && !role.isEmpty()) {
                ticketPage = ticketRepository.findByRole(role, pageable);
            } else {
                ticketPage = ticketRepository.findByStatusAndRole(status, role, pageable);
            }

            if (ticketPage.isEmpty()) {
                return responseService.generateSuccessResponse("No tickets found", null, HttpStatus.OK);
            }

            List<TicketResponseDto> dtoList = ticketPage.stream().map(ticket -> {
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

                // Set name depending on the role
                if ("Customer".equalsIgnoreCase(ticket.getRole())) {
                    Optional<CustomCustomer> customerOpt = customCustomerRepository.findById(ticket.getCustomerOrVendorId());
                    customerOpt.ifPresent(customer -> dto.setName(customer.getName()));
                }
                else if ("Vendor".equalsIgnoreCase(ticket.getRole())) {
                    Long id = ticket.getCustomerOrVendorId();
                    if (id != null) {
                        Optional<VendorEntity> vendorOpt = vendorRepository.findById(id);
                        vendorOpt.ifPresent(vendor -> {
                            String influencerName = (vendor.getFirst_name() != null ? vendor.getFirst_name() : "") +
                                    (vendor.getLast_name() != null ? " " + vendor.getLast_name() : "");
                            dto.setName(influencerName.trim());
                        });
                    } else {
                        dto.setName("Unknown Vendor"); // or some other fallback
                    }
                }

                return dto;
            }).collect(Collectors.toList());
            return responseService.generateSuccessResponseWithCount(
                    "Tickets retrieved successfully", dtoList, ticketPage.getTotalElements(), HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while retrieving tickets: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
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
            List<InvoiceAdmin> invoiceList = invoiceServiceAdmin.getInvoices(
                    id, name, email, mobile, gstn, pan, panTypeCheck, invoiceNo, invoiceDate,
                    recipientState, recipientType, placeOfSupply, serviceType, pageable
            );

            if (invoiceList.isEmpty()) {
                return ResponseService.generateSuccessResponse("No invoices found", invoiceList, HttpStatus.OK);
            } else {
                return ResponseService.generateSuccessResponse("Invoices fetched successfully", invoiceList, HttpStatus.OK);
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

}
