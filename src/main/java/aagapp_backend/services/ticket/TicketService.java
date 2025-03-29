package aagapp_backend.services.ticket;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.TicketDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.vendor.VenderService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    private VenderService vendorService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ResponseService responseService;

    public ResponseEntity<?> createTicket(Map<String, Object> ticketDetails, String token) {
        try {
            // Extract User ID and Role from JWT Token
            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.extractId(jwtToken);
            int role = jwtUtil.extractRoleId(jwtToken);

            // Check if vendor or customer exists based on role
            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                if (vendor == null) {
                    return responseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
                }
            } else if (role == Constant.CUSTOMER_ROLE) {
                CustomCustomer customer = customCustomerService.getCustomerById(userId);
                if (customer == null) {
                    return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }
            } else {
                return responseService.generateErrorResponse("Unauthorized role", HttpStatus.UNAUTHORIZED);
            }

            // Extract the ticket details from the request
            String subject = (String) ticketDetails.get("subject");
            String description = (String) ticketDetails.get("description");
            String status = String.valueOf(aagapp_backend.enums.Ticket.OPEN); // Default status set to "OPEN"
            String remark = (String) ticketDetails.get("remark");
            String priority = (String) ticketDetails.get("priority");
            String type = (String) ticketDetails.get("type");
            String email = (String) ticketDetails.get("email");

            // Validate required fields
            if (StringUtils.isEmpty(subject)) {
                return responseService.generateErrorResponse("Subject is required", HttpStatus.BAD_REQUEST);
            }

            if (StringUtils.isEmpty(description)) {
                return responseService.generateErrorResponse("Description is required", HttpStatus.BAD_REQUEST);
            }

            // Create a new ticket
            Ticket ticket = new Ticket();
            ticket.setSubject(subject);
            ticket.setDescription(description);
            ticket.setStatus(status);
            ticket.setRemark(remark);
            ticket.setPriority(priority);
            ticket.setType(type);
            ticket.setEmail(email);

            // Set role-based field and ID
            ticket.setRole(role == Constant.VENDOR_ROLE ? "Vendor" : "Customer");

            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                ticket.setVendorId(vendor.getService_provider_id());
                ticket.setEmail(vendor.getPrimary_email());
            } else if (role == Constant.CUSTOMER_ROLE) {
                CustomCustomer customer = customCustomerService.getCustomerById(userId);
                ticket.setCustomerId(customer.getId());
                ticket.setEmail(customer.getEmail());
            }

            ticket.setCreatedDate(new Date());
            ticket.setUpdatedDate(new Date());

            // Save the ticket to the database
            ticket = ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket raised successfully", ticket, HttpStatus.CREATED);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error raising ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<?> getTicketsByRoleAndId(String role, Long id) {
        try {
            // Your existing logic...

            if ("vendor".equalsIgnoreCase(role)) {
                // Fetch the vendor using vendorService
                VendorEntity vendor = vendorService.getServiceProviderById(id);
                if (vendor == null) {
                    return responseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
                }

                // Fetch tickets based on vendorId, using the updated query in the repository
                List<Ticket> tickets = ticketRepository.findByVendorId(id);

                // Map the tickets to TicketDTO (or a simplified version of the ticket)
                List<TicketDTO> ticketDTOs = tickets.stream()
                        .map(ticket -> new TicketDTO(ticket)) // Convert to TicketDTO
                        .collect(Collectors.toList());

                return responseService.generateSuccessResponse("Tickets retrieved successfully", ticketDTOs, HttpStatus.OK);

            } else if ("customer".equalsIgnoreCase(role)) {
                // Fetch the customer using customCustomerService
                CustomCustomer customer = customCustomerService.getCustomerById(id);
                if (customer == null) {
                    return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }

                // Fetch tickets based on customerId, using the updated query in the repository
                List<Ticket> tickets = ticketRepository.findByCustomerId(id);

                // Map the tickets to TicketDTO
                List<TicketDTO> ticketDTOs = tickets.stream()
                        .map(ticket -> new TicketDTO(ticket)) // Convert to TicketDTO
                        .collect(Collectors.toList());

                return responseService.generateSuccessResponse("Tickets retrieved successfully", ticketDTOs, HttpStatus.OK);

            } else {
                return responseService.generateErrorResponse("Invalid role", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            // Catch any exception and return a generic error response
            return responseService.generateErrorResponse("Error retrieving tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
