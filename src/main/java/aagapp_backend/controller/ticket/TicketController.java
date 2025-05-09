package aagapp_backend.controller.ticket;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.ticket.TicketService;
import aagapp_backend.services.vendor.VenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VenderService vendorService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @PostMapping("/create-ticket")
    public ResponseEntity<?> createTicket(@RequestBody Map<String, Object> ticketDetails, @RequestHeader("Authorization") String token) {
        try {
            return ticketService.createTicket(ticketDetails, token);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error while creating ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-role/{role}/{id}")
    public ResponseEntity<?> getTicketsByRoleAndId(@PathVariable String role, @PathVariable Long id, @RequestParam(required = false) TicketEnum status) {
        try {
            return ticketService.getTicketsByRoleAndId(role, id, status);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error while fetching tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
