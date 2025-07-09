package aagapp_backend.controller.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.entity.payment.PlanUpgradeRequest;
import aagapp_backend.enums.RequestStatus;
import aagapp_backend.enums.VendorLevelPlan;
import aagapp_backend.repository.payment.PaymentPlanUpgradeRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PlanService;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/plans")
public class PlanController {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private PaymentPlanUpgradeRepository paymentPlanUpgradeRepository;

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

    @PutMapping("/update/{planId}")
    public ResponseEntity<?> updatePlan(@PathVariable Long planId, @RequestBody PlanEntity planEntity) {
        try {
            // Fetch the existing plan by ID
            PlanEntity existingPlan = planService.getPlanById(planId);

            if (existingPlan == null) {
                return ResponseService.generateErrorResponse("Plan with ID " + planId + " not found", HttpStatus.NOT_FOUND);
            }

            // Update the fields of the existing plan with the new data from the request body
            existingPlan.setPlanName(planEntity.getPlanName());
            existingPlan.setPlanVariant(planEntity.getPlanVariant());
            existingPlan.setFollowersRequirement(planEntity.getFollowersRequirement());
            existingPlan.setPrice(planEntity.getPrice());
            existingPlan.setSubtitle(planEntity.getSubtitle());
            existingPlan.setFeatures(planEntity.getFeatures());

            // Save the updated plan
            PlanEntity updatedPlan = planService.updatePlan(existingPlan);

            return ResponseService.generateSuccessResponse("Plan updated successfully!", updatedPlan, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating plan: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("get-plansbyvendorid")
    public ResponseEntity<?> getAllPlansByVendorId(
            @RequestParam(required = false) String planVariant,
            @RequestParam(required = false) Long vendorId) {

        try {
            Map<String, Object> activePlanMap = null;

            List<PlanEntity> planEntities;
            PlanUpgradeRequest activePlanUpgradeRequest = null;

            // Fetch plans based on variant (Monthly/Yearly) or all
            if (planVariant != null && !planVariant.isEmpty()) {
                planEntities = planService.getPlansByVariant(planVariant);
            } else {
                planEntities = planService.getAllPlans();
            }

            // Fetch current plan of the vendor if vendorId is provided
            PlanEntity currentPlan = null;

            if (vendorId != null) {
                VendorEntity vendor = vendorService.getServiceProviderById(vendorId);
                System.out.println(vendor.getPayments() + " frds");
                if (vendor != null && vendor.getPayments() != null) {
                    List<PaymentEntity> payments = vendor.getPayments();

                    Optional<PaymentEntity> activePayment = payments
                            .stream()
                            .filter(payment -> {
                                String status = String.valueOf(payment.getStatus());
                                return status != null && "ACTIVE".equalsIgnoreCase(status);
                            })
                            .findFirst();


                    if (activePayment.isPresent()) {
                        Long planId = activePayment.get().getPlanId();
                        currentPlan = planService.getPlanById(planId); //
                        // Retrieve plan details using planId
                    }


                    Optional<PlanUpgradeRequest> approvedUpgrade = paymentPlanUpgradeRepository
                            .findTopByVendorIdAndStatusOrderByApprovedDateDesc(vendorId, RequestStatus.APPROVED);


                    if (approvedUpgrade.isPresent()) {
                        PlanUpgradeRequest approvedRequest = approvedUpgrade.get();

                        activePlanMap = new HashMap<>();
                        activePlanMap.put("requestedPlanId", approvedRequest.getRequestedPlanId());
                        activePlanMap.put("requestedPlanName", approvedRequest.getRequestedPlanName());

                    } else if (vendor.getPayments() != null && !vendor.getPayments().isEmpty()) {
                        // Fallback to latest payment if no approved request exists
                        Optional<PaymentEntity> latestPayment = vendor.getPayments().stream()
                                .sorted(Comparator.comparing(PaymentEntity::getCreatedAt).reversed())
                                .findFirst();

                        if (latestPayment.isPresent()) {
                            PaymentEntity payment = latestPayment.get();
                            PlanEntity fallbackPlan = planService.getPlanById(payment.getPlanId());

                            activePlanMap = new HashMap<>();
                            activePlanMap.put("requestedPlanId", payment.getPlanId());
                            activePlanMap.put("requestedPlanName", fallbackPlan != null ? fallbackPlan.getPlanName() : null);
                        }
                    }
                }



            }

            // Create response with all plans and current vendor plan
            Map<String, Object> response = new HashMap<>();
            response.put("allPlans", planEntities);
            response.put("currentPlan", currentPlan);
            response.put("activePlanUpgradeRequest", activePlanMap);

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

//    send payment  request to admin
    @PostMapping("/request-plan-upgrade")
    public ResponseEntity<?> requestPlanUpgrade(@RequestBody Map<String, Object> payload) {

        try {

            if (!payload.containsKey("vendorId") || payload.get("vendorId") == null) {
                return ResponseService.generateErrorResponse("vendorId is required", HttpStatus.BAD_REQUEST);

            }
            if (!payload.containsKey("requestedPlanId") || payload.get("requestedPlanId") == null) {
                return ResponseService.generateErrorResponse("requestedPlanId is required", HttpStatus.BAD_REQUEST);

            }

            Long vendorId = Long.parseLong(payload.get("vendorId").toString());
            Long requestedPlanId = Long.parseLong(payload.get("requestedPlanId").toString());
            VendorEntity vendor = vendorService.getServiceProviderById(vendorId);
            if (vendor == null) {
                return ResponseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
            }

            boolean hasPending = paymentPlanUpgradeRepository.existsByVendorIdAndStatus(vendorId, RequestStatus.PENDING);
            if (hasPending) {
                return ResponseService.generateErrorResponse("An upgrade request is already pending.", HttpStatus.CONFLICT);

            }

            PlanEntity requestedPlan = planService.getPlanById(requestedPlanId);
            if (requestedPlan == null) {
                return ResponseService.generateErrorResponse("Requested plan not found", HttpStatus.NOT_FOUND);
            }

            PlanUpgradeRequest request = new PlanUpgradeRequest();
            request.setVendorId(vendorId);
            request.setRequestedPlanId(requestedPlanId);
            request.setName(vendor.getFirst_name()!=null?vendor.getFirst_name():"N/A" + " " + (vendor.getLast_name()!=null?vendor.getLast_name():"N/A"));
            request.setEmail(vendor.getPrimary_email());
            request.setRequestedPlanName(requestedPlan.getPlanName());
            request.setStatus(RequestStatus.PENDING);
            request.setRequestDate(LocalDateTime.now());

            paymentPlanUpgradeRepository.save(request);

            return ResponseService.generateSuccessResponse("Plan upgrade request submitted successfully!", request, HttpStatus.OK);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while submitting upgrade request: " + e.getMessage());
        }
    }



}
