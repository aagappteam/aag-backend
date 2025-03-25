package aagapp_backend.controller.payment;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.PaymentDTO;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.repository.payment.PaymentRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    // Create payment with authorization check and input validation
    @PostMapping("/create/{vendorId}")
    public ResponseEntity<?> createPayment(
            @PathVariable Long vendorId,
            @RequestBody PaymentEntity paymentRequest,
            @RequestHeader(value = "Authorization") String authorization) {

        try {
            // Ensure vendorId is not null and valid
            if (vendorId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vendor ID is required.");
            }

            // Validate Authorization header
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }
            String token = authorization.substring(7);
            Long venderId = jwtUtil.extractId(token);

            if (venderId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            if (!venderId.equals(vendorId)) {
                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
            }

            // Payment validation: Ensure positive and reasonable amount
            if (paymentRequest.getAmount() <= 0) {
                return responseService.generateErrorResponse("Invalid payment amount", HttpStatus.BAD_REQUEST);
            }


            PaymentEntity payment = paymentService.createPayment(paymentRequest, vendorId);
            return responseService.generateSuccessResponse("Payment created successfully", payment, HttpStatus.CREATED);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while processing the payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get active payments by vendor, with JWT validation
    @GetMapping("/getPaymentsByVendor/{vendorId}")
    public ResponseEntity<?> getPaymentsByVendor(@PathVariable Long vendorId, @RequestHeader(value = "Authorization") String authorization) {
        try {
            // Validate Authorization header
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }
            String token = authorization.substring(7);
            Long venderId = jwtUtil.extractId(token);

            if (venderId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            if (!venderId.equals(vendorId)) {
                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
            }

            // Retrieve payments and check for empty list
            List<PaymentEntity> payments = paymentService.findActivePlansByVendorId(vendorId);
            if (payments.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK,"No transactions found for this vendor", null);
            }

            List<PaymentDTO> paymentDTOs = payments.stream()
                    .map(payment -> {
                        // Fetch the PlanEntity using the planId
                        PlanEntity planEntity = entityManager.find(PlanEntity.class, payment.getPlanId());

                        if (planEntity == null) {
                            throw new RuntimeException("Plan not found with ID: " + payment.getPlanId());
                        }

                        // Map the PaymentEntity to PaymentDTO, passing the PlanEntity to get the planName
                        return new PaymentDTO(payment, planEntity);
                    })
                    .collect(Collectors.toList());


            return responseService.generateSuccessResponse("Payments retrieved successfully", paymentDTOs, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while fetching payments: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get transactions by vendor ID with optional transaction reference filter and JWT validation
    @GetMapping("/getTransactionsByVendorId/{vendorId}")
    public ResponseEntity<?> getTransactionsByVendorId(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String transactionReference,
            @RequestHeader(value = "Authorization") String authorization) {

        try {

            // Retrieve transactions with pagination and optional reference filter
            List<PaymentEntity> transactions = paymentService.getTransactionsByVendorId(vendorId, page, size, transactionReference);
            if (transactions.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK,"No transactions found for this vendor", null);
            }

            List<PaymentDTO> paymentDTOs = transactions.stream()
                    .map(payment -> {
                        // Fetch the PlanEntity using the planId
                        PlanEntity planEntity = entityManager.find(PlanEntity.class, payment.getPlanId());

                        if (planEntity == null) {
                            throw new RuntimeException("Plan not found with ID: " + payment.getPlanId());
                        }

                        // Map the PaymentEntity to PaymentDTO, passing the PlanEntity to get the planName
                        return new PaymentDTO(payment, planEntity);
                    })
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponse("Transactions retrieved successfully", paymentDTOs, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while fetching transactions: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get transactions by vendor name with optional transaction reference filter and JWT validation
    @GetMapping("/getTransactionsByVendorName/{vendorName}")
    public ResponseEntity<?> getTransactionsByVendorName(
            @PathVariable String vendorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String transactionReference,
            @RequestHeader(value = "Authorization") String authorization) {

        try {
            // Validate Authorization header
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }
            String token = authorization.substring(7);
            Long venderId = jwtUtil.extractId(token);

            if (venderId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Retrieve transactions and check for empty list
            List<PaymentEntity> transactions = paymentService.getAllTransactionsByVendorName(vendorName, page, size, transactionReference);
            if (transactions.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "No transactions found for this vendor" ,null);

            }

            // Map to DTO for response
/*            List<PaymentDTO> paymentDTOs = transactions.stream()
                    .map(payment -> new PaymentDTO(payment))
                    .collect(Collectors.toList());*/

            List<PaymentDTO> paymentDTOs = transactions.stream()
                    .map(payment -> {
                        // Fetch the PlanEntity using the planId
                        PlanEntity planEntity = entityManager.find(PlanEntity.class, payment.getPlanId());

                        if (planEntity == null) {
                            throw new RuntimeException("Plan not found with ID: " + payment.getPlanId());
                        }

                        // Map the PaymentEntity to PaymentDTO, passing the PlanEntity to get the planName
                        return new PaymentDTO(payment, planEntity);
                    })
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponse("Transactions retrieved successfully", paymentDTOs, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while fetching transactions: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/send-invoice-email")
    public ResponseEntity<?> sendInvoiceByEmail(@RequestParam String transactionId) {
        try {
            // Find the payment using the transaction ID
            PaymentEntity paymentEntity = paymentRepository.findByTransactionId(transactionId);

            // If payment is not found, return a bad request response
            if (paymentEntity == null) {
                return responseService.generateErrorResponse("Payment or transaction not found.", HttpStatus.BAD_REQUEST);
            }

            // Retrieve the invoice URL from the payment
            String invoiceUrl = paymentEntity.getDownloadInvoice();

            // If no invoice URL is available, return an error
            if (invoiceUrl == null || invoiceUrl.isEmpty()) {
                return responseService.generateErrorResponse("Invoice link is not available.", HttpStatus.BAD_REQUEST);
            }

            // Send the invoice URL to the vendor's email
            try {
                // Assuming paymentEntity.getVendorEntity().getPrimary_email() returns the vendor's email
                paymentService.sendEmail(paymentEntity.getVendorEntity().getPrimary_email(), invoiceUrl);
            } catch (MessagingException e) {
                // Catch and handle any email sending exceptions
                return responseService.generateErrorResponse("An error occurred while sending the invoice: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Using generateSuccessResponse for a successful response
            return responseService.generateSuccessResponse("Invoice link sent on " + paymentEntity.getVendorEntity().getPrimary_email() + " successfully.", null, HttpStatus.OK);

        } catch (Exception e) {
            // Catch any unexpected exceptions
            return responseService.generateErrorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}
