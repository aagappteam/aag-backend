package aagapp_backend.controller.admin;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.controller.otp.OtpEndpoint;
import aagapp_backend.dto.DashboardResponseAdmin;
import aagapp_backend.dto.MonthlyEarningWithVendorDTO;
import aagapp_backend.dto.NotificationDTO;
import aagapp_backend.dto.WithdrawalRequestHistoryDTO;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.admin.DashboardAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import aagapp_backend.entity.withdrawrequest.WithdrawalRequest;
import aagapp_backend.repository.earning.InfluencerMonthlyEarningRepository;
import aagapp_backend.repository.withdrawrequest.WithdrawalRequestRepository;
import aagapp_backend.services.*;
import aagapp_backend.services.admin.AdminService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")

public class AdminDetailsController {

    @Autowired
    private DashboardAdmin dashboardAdmin;

    @Autowired
    private InfluencerMonthlyEarningRepository earningRepo;

    @Autowired
    private WithdrawalRequestRepository withdrawalRepo;

    @Autowired
    private VendorRepository vendorRepository;

    private ExceptionHandlingImplement exceptionHandling;
    private TwilioService twilioService;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private EntityManager em;
    private CustomCustomerService customCustomerService;
    private RoleService roleService;
    private OtpEndpoint otpEndpoint;
    private VenderService vendorService;
    private ResponseService responseService;

    private AdminService adminService;

    @Autowired
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setTwilioService(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setOtpEndpoint(OtpEndpoint otpEndpoint) {
        this.otpEndpoint = otpEndpoint;
    }

    @Autowired
    public void setVendorService(VenderService vendorService) {
        this.vendorService = vendorService;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }



//    dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
    ) {
        try {
            DashboardResponseAdmin response = dashboardAdmin.getDashboard();
            return responseService.generateSuccessResponse("Dashboard retrieved successfully.", response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return responseService.generateErrorResponse("Error retrieving dashboard: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving dashboard: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    get game result records
/*
    @GetMapping("/game-results")
    public ResponseEntity<?> getAllGameResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "playedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<GameResultRecordDTO> resultPage = dashboardAdmin.getAllGameResults(pageable);

        return new ResponseEntity<>(resultPage, HttpStatus.OK);
    }
*/

/*//    get all notifications
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificationDTO> resultPage = dashboardAdmin.getAllNotifications(page, size);
        return new ResponseEntity<>(resultPage, HttpStatus.OK);
    }

//    get all vendor notification shares
    @GetMapping("/vendor-notifications")
    public ResponseEntity<?> getVendorNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificationDTO> resultPage = dashboardAdmin.getAllVendorNotifications(page, size);
        return new ResponseEntity<>(resultPage, HttpStatus.OK);
    }*/



    @Transactional
    @PatchMapping("/update")
    public ResponseEntity<?> updateAdmin(@RequestParam Long userId, @RequestBody Map<String, Object> adminDetails) throws Exception {
        try {
            CustomAdmin customAdmin = em.find(CustomAdmin.class, userId);

            if (customAdmin == null) {
                return ResponseService.generateErrorResponse("Admin with provided Id not found", HttpStatus.NOT_FOUND);
            }

            // Proceed with the update using the adminDetails map
            return adminService.updateDetails(userId, adminDetails);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Summary for current month
    @GetMapping("/monthly-earnings")
    public ResponseEntity<?> getMonthlyEarnings(
            @RequestParam(required = false) Long influencerId,
            @RequestParam(required = false) String monthYear,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "10") Integer size) {
        int pageSize = (limit != null) ? limit : (size != null ? size : 10);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")));
        Page<InfluencerMonthlyEarning> resultPage;

        if (influencerId != null && monthYear != null) {
            InfluencerMonthlyEarning record = earningRepo.findByInfluencerIdAndMonthYear(influencerId, monthYear);
            VendorEntity vendor = vendorRepository.findByServiceProviderId(influencerId);

            if (record != null && vendor != null) {
                MonthlyEarningWithVendorDTO dto = new MonthlyEarningWithVendorDTO(record, vendor);
                return ResponseEntity.ok(List.of(dto));
            }
            return ResponseEntity.ok(List.of());
        } else {
            if (influencerId != null) {
                resultPage = earningRepo.findByInfluencerId(influencerId, pageable);
            } else if (monthYear != null) {
                resultPage = earningRepo.findByMonthYear(monthYear, pageable);
            } else {
                resultPage = earningRepo.findAll(pageable);
            }

            List<MonthlyEarningWithVendorDTO> resultList = resultPage.getContent().stream()
                    .map(e -> {
                        VendorEntity vendor = vendorRepository.findByServiceProviderId(e.getInfluencerId());
                        return new MonthlyEarningWithVendorDTO(e, vendor);
                    })
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponseWithCount(
                    "Monthly earnings fetched successfully",
                    resultList,
                    resultPage.getTotalElements(),
                    HttpStatus.OK
            );
        }
    }



    // Vendor month wise history
    @GetMapping("/vendor-history")
    public ResponseEntity<Map<String, Object>> getVendorHistory(
            @RequestParam Long vendorId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        int pageSize = (limit != null) ? limit : (size != null ? size : 10);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")));
        Page<InfluencerMonthlyEarning> earningsPage = earningRepo.findByInfluencerId(vendorId, pageable);

        List<Map<String, Object>> responseList = earningsPage
                .stream()
                .map(e -> buildSummaryMap(
                        e,
                        withdrawalRepo.findByInfluencerIdAndMonthYear(vendorId, e.getMonthYear())
                ))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Vendor history fetched successfully");
        response.put("data", responseList);
        response.put("totalElements", earningsPage.getTotalElements());
        response.put("totalPages", earningsPage.getTotalPages());
        response.put("currentPage", earningsPage.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/withdrawal-requests")
    public ResponseEntity<?> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long influencerId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            int pageSize = (limit != null) ? limit : (size != null ? size : 10);

            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")));
            Page<WithdrawalRequest> requestsPage;

            // Validate status if provided
            if (status != null && !status.isEmpty()) {
                List<String> validStatuses = Arrays.asList("PENDING", "APPROVED", "REJECTED");
                if (!validStatuses.contains(status.toUpperCase())) {
                    return responseService.generateErrorResponse("Invalid status filter", HttpStatus.BAD_REQUEST);
                }
            }

            // Apply filters based on presence of parameters
            if (status != null && !status.isEmpty() && influencerId != null) {
                requestsPage = withdrawalRepo.findByInfluencerIdAndStatus(influencerId, status.toUpperCase(), pageable);
            } else if (status != null && !status.isEmpty()) {
                requestsPage = withdrawalRepo.findByStatus(status.toUpperCase(), pageable);
            } else if (influencerId != null) {
                requestsPage = withdrawalRepo.findByInfluencerId(influencerId, pageable);
            } else {
                requestsPage = withdrawalRepo.findAll(pageable);
            }

            List<WithdrawalRequestHistoryDTO> dtoList = requestsPage.getContent().stream().map(request -> {
                VendorEntity vendor = vendorRepository.findById(request.getInfluencerId()).orElse(null);
                String name = vendor != null
                        ? (vendor.getFirst_name() != null ? vendor.getFirst_name() : "") +
                        (vendor.getLast_name() != null ? " " + vendor.getLast_name() : "")
                        : "";
                String mobile = vendor.getMobileNumber() != null ? vendor.getMobileNumber() : "N/A";
                String email = vendor.getPrimary_email() != null ? vendor.getPrimary_email() : "N/A";

                return new WithdrawalRequestHistoryDTO(request, name.trim(), mobile, email);
            }).collect(Collectors.toList());

            return responseService.generateSuccessResponseWithCount(
                    "Withdrawal requests fetched successfully",
                    dtoList,
                    requestsPage.getTotalElements(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }



    @PostMapping("/withdrawal/{id}/approve")
    public ResponseEntity<?> approveWithdraw(@PathVariable Long id) {
        try {
            Optional<WithdrawalRequest> wr = withdrawalRepo.findById(id);
            if (wr.isPresent()) {
                WithdrawalRequest request = wr.get();
                request.setStatus("APPROVED");
                withdrawalRepo.save(request);
                return responseService.generateSuccessResponse(
                        "Withdrawal request approved successfully",
                        request,
                        HttpStatus.OK
                );
            }else
            {
                return responseService.generateErrorResponse(
                        "Withdrawal request not found",
                        HttpStatus.BAD_REQUEST
                );
            }
        }catch (Exception e){
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/withdrawal/{id}/reject")
    public ResponseEntity<?> rejectWithdraw(@PathVariable Long id) {
        try {
            Optional<WithdrawalRequest> wr = withdrawalRepo.findById(id);
            if (wr.isPresent()) {
                WithdrawalRequest request = wr.get();
                request.setStatus("REJECTED");
                withdrawalRepo.save(request);
                return responseService.generateSuccessResponse(
                        "Withdrawal request rejected successfully",
                        request,
                        HttpStatus.OK
                );

            }else{
                return responseService.generateErrorResponse(
                        "Withdrawal request not found",
                        HttpStatus.BAD_REQUEST
                );

            }
        }catch (Exception e){
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private Map<String, Object> buildSummaryMap(InfluencerMonthlyEarning e, List<WithdrawalRequest> withdrawals) {
        BigDecimal maxReturn = e.getMaxReturnAmount();
        BigDecimal cappedEarn = e.getEarnedAmount().min(maxReturn);
        VendorEntity vendor = vendorRepository.findByServiceProviderId(e.getInfluencerId());
        BigDecimal percent = cappedEarn.divide(maxReturn, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        BigDecimal withdrawn = withdrawals.stream().filter(w -> w.getStatus().equals("APPROVED"))
                .map(WithdrawalRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pending = withdrawals.stream().filter(w -> w.getStatus().equals("PENDING"))
                .map(WithdrawalRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = cappedEarn.subtract(withdrawn).subtract(pending);
        String firstName = vendor.getFirst_name() != null ? vendor.getFirst_name() : "N/A";
        String lastName = vendor.getLast_name() != null ? vendor.getLast_name() : "N/A";

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("vendorId", e.getInfluencerId());
        map.put("vendorName", (firstName + " " + lastName).trim());
        map.put("vendorEmail", vendor.getPrimary_email()!=null?vendor.getPrimary_email():"N/A");
        map.put("mobileNumber",vendor.getMobileNumber());
        map.put("monthYear", e.getMonthYear());
        map.put("recharge", e.getRechargeAmount());
        map.put("earned", e.getEarnedAmount());
        map.put("maxReturn", maxReturn);
        map.put("returnX", cappedEarn.divide(e.getRechargeAmount(), 1, RoundingMode.HALF_UP) + "x");
        map.put("withdrawn", withdrawn);
        map.put("pendingWithdraw", pending);
        map.put("balance", balance);
        map.put("progressPercent", percent.min(BigDecimal.valueOf(100)).intValue());
        map.put("filledBoxes", Math.min(10, percent.intValue() / 10));
        String status;
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            status = "Withdrawals not allowed";
        } else if (percent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            status = "Can be approved ";
        } else {
            status = "No";
        }
        map.put("status", status);
        return map;
    }


}
