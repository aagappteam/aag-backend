package aagapp_backend.controller.customer.ticket;

import aagapp_backend.dto.ticketdto.TicketResponseDto;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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


/*    @GetMapping("/tickets-by-user/{userId}")
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

//            long openCount = ticketRepository.countByStatus(TicketEnum.OPEN);
//            long closedCount = ticketRepository.countByStatus(TicketEnum.CLOSED);
//            long totalCount = ticketRepository.count();

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

}
