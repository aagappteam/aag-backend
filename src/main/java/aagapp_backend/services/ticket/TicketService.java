package aagapp_backend.services.ticket;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.TicketEnum;
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
            String userorvendorrole = "";
            String EmailId = "";

            // Check if vendor or customer exists based on role
            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                if (vendor == null) {
                    return responseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
                }
                userorvendorrole = "Vendor";
                EmailId = vendor.getPrimary_email();

            } else if (role == Constant.CUSTOMER_ROLE) {
                CustomCustomer customer = customCustomerService.getCustomerById(userId);
                if (customer == null) {
                    return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }
                userorvendorrole = "Customer";
                EmailId = customer.getEmail();
            } else {
                return responseService.generateErrorResponse("Unauthorized role", HttpStatus.UNAUTHORIZED);
            }

            // Extract the ticket details from the request
            String subject = (String) ticketDetails.get("subject");
            String description = (String) ticketDetails.get("description");

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
            ticket.setStatus(TicketEnum.OPEN);
            ticket.setCustomerOrVendorId(userId);
            ticket.setRole(userorvendorrole);
            ticket.setEmail(EmailId);
            ticket.setCreatedDate(new Date());
            ticket.setUpdatedDate(new Date());

            // Save the ticket to the database
            ticket = ticketRepository.save(ticket);

            return responseService.generateSuccessResponse("Ticket raised successfully", ticket, HttpStatus.CREATED);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error raising ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getTicketsByRoleAndId(String role, Long id, TicketEnum status) {
        try {
            List<Ticket> tickets = ticketRepository.findByRoleAndCustomerOrVendorIdAndStatus(role, id, status);

            return responseService.generateSuccessResponse("Tickets retrieved successfully", tickets, HttpStatus.OK);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error retrieving tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
