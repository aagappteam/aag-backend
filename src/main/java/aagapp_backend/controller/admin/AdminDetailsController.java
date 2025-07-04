package aagapp_backend.controller.admin;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.controller.otp.OtpEndpoint;
import aagapp_backend.dto.*;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.NotificationShare;
import aagapp_backend.repository.NotificationShareRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.admin.DashboardAdmin;
import aagapp_backend.spec.InfluencerMonthlyEarningSpecification;
import aagapp_backend.spec.NotificationShareSpecification;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.escapeCsv;


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

    @Autowired
    private NotificationShareRepository notificationShareRepository;

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
    public void setAdminService(@Lazy AdminService adminService) {
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

    @GetMapping("/vendor-share")
    public ResponseEntity<?> getNotificationShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createdTo
    ) {
        Page<NotificationDTOAdmin> result = dashboardAdmin.getAllNotifications(
                page, size, amount, vendorName, details, createdFrom, createdTo, customerId, vendorId
        );
        return responseService.generateSuccessResponseWithCount(
                "Notifications retrieved successfully.",
                result.getContent(),
                result.getTotalElements(),
                HttpStatus.OK
        );
    }

    @GetMapping("/vendor-share/export")
    public void exportNotificationSharesToCSV(
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createdTo,
            HttpServletResponse response
    ) throws IOException {

        // Set response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=vendor_shares.csv");

        // Create specification
        Specification<NotificationShare> spec = Specification
                .where(NotificationShareSpecification.hasAmount(amount))
                .and(NotificationShareSpecification.vendorNameContains(vendorName))
                .and(NotificationShareSpecification.detailsContains(details))
                .and(NotificationShareSpecification.vendorId(vendorId))
                .and(NotificationShareSpecification.createdBetween(createdFrom, createdTo));

        // Fetch all matching records (no pagination)
        List<NotificationShare> entities = notificationShareRepository.findAll(spec);

        // Convert to DTO
        List<NotificationDTOAdmin> dtos = entities.stream().map(ns -> {
            VendorEntity v = ns.getVendor();
            String name = v != null ? v.getFirst_name() + " " + v.getLast_name() : "N/A";
            String email = v != null ? v.getPrimary_email() : "N/A";
            String formattedDate = ns.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            return new NotificationDTOAdmin(
                    ns.getId(),
                    ns.getVendorId(),
                    "Vendor",
                    ns.getDescription(),
                    ns.getDetails(),
                    formattedDate,
                    ns.getAmount(),
                    name,
                    email
            );
        }).toList();

        // Write CSV
        PrintWriter writer = response.getWriter();
        writer.println("ID,Vendor ID,Type,Description,Details,Created Date,Amount,Vendor Name,Vendor Email");

        for (NotificationDTOAdmin dto : dtos) {
            writer.printf(
                    "%d,%s,%s,%s,%s,%s,%.2f,%s,%s%n",
                    dto.getId(),
                    dto.getVendorId(),
                    dto.getRole(),
                    escapeCsv(dto.getDescription()),
                    escapeCsv(dto.getDetails()),
                    dto.getCreatedDate(),
                    dto.getAmount(),
                    escapeCsv(dto.getVendorName()),
                    escapeCsv(dto.getVendorEmail())
            );
        }

        writer.flush();
        writer.close();
    }


    /*@GetMapping("/all-app-transaction")
    public ResponseEntity<?> allAppTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Double amount
    ) {
        try {
            Page<NotificationDTOAdmin> resultPage = dashboardAdmin.getAllVendorNotifications(page, size,role,vendorName, amount);

            return responseService.generateSuccessResponseWithCount(
                    "Notifications retrieved successfully.",
                    resultPage.getContent(),
                    resultPage.getTotalElements(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error getting: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/


    @GetMapping("/all-app-transaction")
    public ResponseEntity<?> getFilteredNotifications(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String details,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Notification> notifications = dashboardAdmin.getFilteredNotifications(
                    role, vendorId, customerId, amount, minAmount, maxAmount,
                    startDate, endDate, description, details, page, size
            );
            return responseService.generateSuccessResponseWithCount(
                    "Notifications retrieved successfully.",
                    notifications.getContent(),
                    notifications.getTotalElements(),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error getting: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/all-app-transaction/download")
    public void downloadNotificationsAsCsv(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String details,
            HttpServletResponse response
    ) {
        try {
            // Fetch all filtered notifications (no pagination)
            List<Notification> notifications = dashboardAdmin.getFilteredNotifications(
                    role, vendorId, customerId, amount, minAmount, maxAmount,
                    startDate, endDate, description, details, 0, Integer.MAX_VALUE
            ).getContent();

            // Set response headers for file download
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=notifications.csv");

            // Write CSV header
            PrintWriter writer = response.getWriter();
            writer.println("ID,Role,VendorId,CustomerId,Amount,CreatedDate,Description,Details");

            // Write CSV rows
            for (Notification n : notifications) {
                writer.printf("%d,%s,%d,%d,%.2f,%s,%s,%s%n",
                        n.getId(),
                        n.getRole(),
                        n.getVendorId() != null ? n.getVendorId() : 0,
                        n.getCustomerId() != null ? n.getCustomerId() : 0,
                        n.getAmount() != null ? n.getAmount() : 0.0,
                        n.getCreatedDate(),
                        escapeCsv(n.getDescription()),
                        escapeCsv(n.getDetails())
                );
            }

            writer.flush();
        } catch (Exception ex) {
            exceptionHandling.handleException(ex);
            throw new RuntimeException("Error while generating CSV: " + ex.getMessage());
        }
    }

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


    @GetMapping("/monthly-earnings")
    public ResponseEntity<?> getMonthlyEarnings(
            @RequestParam(required = false) Long influencerId,
            @RequestParam(required = false) String monthYear,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minEarning,
            @RequestParam(required = false) BigDecimal maxEarning,
            @RequestParam(required = false) BigDecimal minRecharge,
            @RequestParam(required = false) BigDecimal maxRecharge,
            @RequestParam(required = false) Integer multiplier,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        int pageSize = (limit != null) ? limit : (size != null ? size : 10);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Specification<InfluencerMonthlyEarning> spec = InfluencerMonthlyEarningSpecification.filter(
                influencerId, monthYear, startDate, endDate, minEarning, maxEarning,
                minRecharge, maxRecharge, multiplier
        );

        Page<InfluencerMonthlyEarning> pageResult = earningRepo.findAll(spec, pageable);
        List<MonthlyEarningWithVendorDTO> dtos = pageResult.stream()
                .map(e -> new MonthlyEarningWithVendorDTO(e, vendorRepository.findByServiceProviderId(e.getInfluencerId())))
                .collect(Collectors.toList());

        return responseService.generateSuccessResponseWithCount(
                "Monthly earnings fetched successfully",
                dtos,
                pageResult.getTotalElements(),
                HttpStatus.OK
        );
    }

    @GetMapping("/monthly-earnings/download")
    public void downloadMonthlyEarningsCsv(
            @RequestParam(required = false) Long influencerId,
            @RequestParam(required = false) String monthYear,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minEarning,
            @RequestParam(required = false) BigDecimal maxEarning,
            @RequestParam(required = false) BigDecimal minRecharge,
            @RequestParam(required = false) BigDecimal maxRecharge,
            @RequestParam(required = false) Integer multiplier,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletResponse response
    ) throws IOException {

        // Build sort
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);

        // Use the spec you already have
        Specification<InfluencerMonthlyEarning> spec = InfluencerMonthlyEarningSpecification.filter(
                influencerId, monthYear, startDate, endDate,
                minEarning, maxEarning, minRecharge, maxRecharge, multiplier
        );

        List<InfluencerMonthlyEarning> earnings = earningRepo.findAll(spec, sort);

        // CSV response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=monthly_earnings.csv");

        // Writer setup
        PrintWriter writer = response.getWriter();
        writer.println("ID,Influencer ID,Month,Recharge Amount,Multiplier,Earned Amount,Max Return,Vendor Name,Mobile");

        for (InfluencerMonthlyEarning e : earnings) {
            VendorEntity vendor = vendorRepository.findByServiceProviderId(e.getInfluencerId());

            writer.printf(
                    "%d,%d,%s,%s,%d,%s,%s,%s,%s%n",
                    e.getId(),
                    e.getInfluencerId(),
                    e.getMonthYear(),
                    e.getRechargeAmount(),
                    e.getMultiplier(),
                    e.getEarnedAmount(),
                    e.getMaxReturnAmount(),
                    vendor != null ? vendor.getName() : "",
                    vendor != null ? vendor.getMobileNumber() : ""
            );
        }

        writer.flush();
    }


    // Vendor month wise history
    @GetMapping("/vendor-history")
    public ResponseEntity<Map<String, Object>> getVendorHistory(
            @RequestParam Long vendorId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String monthYear


            ) {
        int pageSize = (limit != null) ? limit : (size != null ? size : 10);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")));
        Page<InfluencerMonthlyEarning> earningsPage = earningRepo.findByInfluencerId(vendorId,monthYear, pageable);

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

            long approvedCount = withdrawalRepo.countByStatus("APPROVED");
            long rejectedCount = withdrawalRepo.countByStatus("REJECTED");
            long pendingCount = withdrawalRepo.countByStatus("PENDING");

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

            return responseService.generateSuccessResponseForWithdrwalRequest(
                    "Withdrawal requests fetched successfully",
                    dtoList,
                    requestsPage.getTotalElements(),
                    approvedCount,
                    rejectedCount,
                    pendingCount,
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
