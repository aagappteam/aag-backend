package aagapp_backend.controller.payment;

import aagapp_backend.dto.PaymentDTO;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @PostMapping("/create/{vendorId}")
    public ResponseEntity<?> createPayment(
            @PathVariable Long vendorId,
            @RequestBody @Valid PaymentEntity paymentRequest) {

        try {
            if (vendorId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vendor ID is required.");
            }

            PaymentEntity payment = paymentService.createPayment(paymentRequest, vendorId);

            return responseService.generateSuccessResponse("Payment created successfully", payment, HttpStatus.CREATED);


        } catch (Exception e) {
            System.out.println(exceptionHandling.handleException(e));
            return responseService.generateErrorResponse("An error occurred while processing the payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/getPaymentsByVendor/{vendorId}")
    public ResponseEntity<?> getPaymentsByVendor(@PathVariable Long vendorId) {
        try {
            List<PaymentEntity> payments = paymentService.findActivePlansByVendorId(vendorId);

            System.out.println("payments " + payments.size());

            if (payments.isEmpty()) {
                return responseService.generateErrorResponse("No payments found for this vendor", HttpStatus.NOT_FOUND);
            }

            List<PaymentDTO> paymentDTOs = payments.stream()
                    .map(payment -> new PaymentDTO(payment))
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponse("Payments retrieved successfully", paymentDTOs, HttpStatus.OK);

        } catch (Exception e) {
            return responseService.generateErrorResponse("An error occurred while fetching payments: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
