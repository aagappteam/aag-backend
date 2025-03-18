package aagapp_backend.controller.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.enums.VendorLevelPlan;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PlanService;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/plans")
public class PlanController {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private PlanService planService;

    @Autowired
    private VenderService   vendorService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    // Endpoint to create a new plan
    @PostMapping("/create")
    public ResponseEntity<?> createPlan(@RequestBody PlanEntity planEntity) {
        try{
            PlanEntity planEntityresponse =   planService.createPlan(planEntity);

            return ResponseService.generateSuccessResponse("New Plans added successfully!", planEntityresponse, HttpStatus.OK);

        }catch (Exception e){
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error creating plan: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to retrieve all plans
    @GetMapping("get-allplans")
    public ResponseEntity<?> getAllPlans(@RequestParam(required = false) String planVariant) {
        try {
            List<PlanEntity> planEntities;

            if (planVariant != null && !planVariant.isEmpty()) {
                planEntities = planService.getPlansByVariant(planVariant);
            } else {
                planEntities = planService.getAllPlans();
            }

            return ResponseService.generateSuccessResponse("All Plans fetched successfully!", planEntities, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching plans: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("get-plansbyvendorid")
    public ResponseEntity<?> getAllPlansByVendorId(
            @RequestParam(required = false) String planVariant,
            @RequestParam(required = false) Long vendorId) {

        try {
            List<PlanEntity> planEntities;

            // Fetch plans based on variant (Monthly/Yearly) or all
            if (planVariant != null && !planVariant.isEmpty()) {
                planEntities = planService.getPlansByVariant(planVariant);
            } else {
                planEntities = planService.getAllPlans();
            }

            // Fetch current plan of the vendor if vendorId is provided
            PlanEntity currentPlan = null;
            /*if (vendorId != null) {
                VendorEntity vendor = vendorService.getServiceProviderById(vendorId);
                if (vendor != null) {
                    PaymentEntity payment = vendor.getPayments()
                    currentPlan = vendor.getVendorLevelPlan(); // Assuming this fetches the vendor's current plan
                }
            }*/

            if (vendorId != null) {
                VendorEntity vendor = vendorService.getServiceProviderById(vendorId);
                System.out.println(vendor.getPayments() + " frds");
                if (vendor != null && vendor.getPayments() != null) {
                    List<PaymentEntity> payments = vendor.getPayments();

                    Optional<PaymentEntity> activePayment = payments
                            .stream()
                            .filter(payment -> {
                                String status = String.valueOf(payment.getStatus());
                                System.out.println("Checking Payment ID: " + payment.getId() + ", Status: " + status);
                                return status != null && "ACTIVE".equalsIgnoreCase(status);
                            })
                            .findFirst();


                    if (activePayment.isPresent()) {
                        Long planId = activePayment.get().getPlanId();
                        currentPlan = planService.getPlanById(planId); //
                        // Retrieve plan details using planId
                    }
                }
            }

            // Create response with all plans and current vendor plan
            Map<String, Object> response = new HashMap<>();
            response.put("allPlans", planEntities);
            response.put("currentPlan", currentPlan);

            return ResponseService.generateSuccessResponse("All Plans fetched successfully!", response, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching plans: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to get a plan by its ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        try{
            PlanEntity  planEntity=   planService.getPlanById(id);
            return ResponseService.generateSuccessResponse("Plan fetched successfully!", planEntity, HttpStatus.OK);
        }catch (Exception e){
            exceptionHandling.handleException(e);
            return null;
        }

    }


}
