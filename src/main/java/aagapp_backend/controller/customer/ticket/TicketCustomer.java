package aagapp_backend.controller.customer.ticket;

import aagapp_backend.dto.ticketdto.TicketResponseDto;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.ticket.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/customer/tickets")
@RestController

public class TicketCustomer
{

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;

    @Autowired
    private TicketService ticketService;


    @GetMapping("/tickets-by-user/{userId}")
    public ResponseEntity<?> getTicketsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) TicketEnum status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));

            // ✅ Fetch using native query service method
            Page<Ticket> ticketPage = ticketService.getTicketsByUserIdWithFilters(userId, status, role, pageable);

            if (ticketPage.isEmpty()) {
                return responseService.generateSuccessResponse("No tickets found", null, HttpStatus.OK);
            }

            // ✅ Map to DTO
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

                // ✅ Fetch name based on role
                if ("Customer".equalsIgnoreCase(ticket.getRole())) {
                    customCustomerRepository.findById(ticket.getCustomerOrVendorId())
                            .ifPresent(customer -> dto.setName(customer.getName()));
                } else if ("Vendor".equalsIgnoreCase(ticket.getRole())) {
                    vendorRepository.findById(ticket.getCustomerOrVendorId())
                            .ifPresent(vendor -> {
                                String influencerName = (vendor.getFirst_name() != null ? vendor.getFirst_name() : "") +
                                        (vendor.getLast_name() != null ? " " + vendor.getLast_name() : "");
                                dto.setName(influencerName.trim());
                            });
                }

                return dto;
            }).toList();

            return responseService.generateSuccessResponseWithCount(
                    "Tickets retrieved successfully",
                    dtoList,
                    ticketPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "An error occurred while retrieving tickets: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}
