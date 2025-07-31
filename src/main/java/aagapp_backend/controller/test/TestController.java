package aagapp_backend.controller.test;

import aagapp_backend.components.Constant;
import aagapp_backend.components.pricelogic.PriceConstant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import aagapp_backend.entity.players.Player;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.earning.InfluencerMonthlyEarningRepository;
import aagapp_backend.services.*;
import aagapp_backend.services.admin.AdminLogService;
import aagapp_backend.services.download.InfluencerEarningsService;
import aagapp_backend.services.faqs.FAQService;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.tournamnetservice.TournamentService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    private EmailService emailService;
    private CommonService commonService;
    private InfluencerMonthlyEarningRepository earningRepository;
    private ResponseService responseService;
    private TournamentService tournamentService;
    private NotoficationFirebase notoficationFirebase;
    private FAQService faqService;
    private EntityManager entityManager;
    private CustomCustomerService customCustomerService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Autowired
    public void setCommonService(CommonService commonService) {
        this.commonService = commonService;
    }

    @Autowired
    public void setEarningRepository(InfluencerMonthlyEarningRepository earningRepository) {
        this.earningRepository = earningRepository;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setTournamentService(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @Autowired
    public void setNotoficationFirebase(NotoficationFirebase notoficationFirebase) {
        this.notoficationFirebase = notoficationFirebase;
    }

    @Autowired
    public void setFaqService(FAQService faqService) {
        this.faqService = faqService;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }
    // POST method to send onboarding email
    @PostMapping("/sendOnboardingEmail")
    public void sendOnboardingEmail(@RequestParam String email,
                                    @RequestParam String firstname,
                                    @RequestParam String lastname) throws IOException {
        emailService.sendOnboardingEmail(email, firstname, lastname);
    }

    // POST method to send profile verification email
    @PostMapping("/send-profile/{service_provider_id}")
    public void sendProfileVerificationEmail(@PathVariable Long service_provider_id) throws IOException {
        VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
        if (vendorEntity != null) {
            emailService.sendProfileVerificationEmail(vendorEntity, "AAG onboarding");
        }
    }

    // POST method to send profile rejection email
    @PostMapping("/sendProfileRejectionEmail/{service_provider_id}")
    public void sendProfileRejectionEmail(@PathVariable Long service_provider_id) throws IOException {
        VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
        if (vendorEntity != null) {
            emailService.sendProfileRejectionEmail(vendorEntity);
        }
    }


//    set faq data in the database
    @PostMapping("/faquser")
    public void setFaqData() {
        faqService.addFAQIfNeeded();
   }


//   send ntotification to user/vendor
    @PostMapping("/sendnotification/{service_provider_id}/{role}")
    public ResponseEntity<?> sendNotification(@PathVariable Long service_provider_id,@PathVariable String role) throws IOException {


        if ("vendor".equalsIgnoreCase(role)) {
            VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
            if (vendorEntity != null) {
                if (vendorEntity.getFcmToken() != null) {
                    notoficationFirebase.sendNotification(
                            vendorEntity.getFcmToken(),
                            "Tournament starting soon!",
                            "Tournament will start in 3 minutes. Please join now!"
                    );
                    return new ResponseEntity<>("Notification sent successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("No FCM token for player", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Vendor not found", HttpStatus.NOT_FOUND);
            }
        } else if ("user".equalsIgnoreCase(role)) {

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, service_provider_id);
            if (customCustomer != null) {
                if (customCustomer.getFcmToken() != null) {
                    notoficationFirebase.sendNotification(
                            customCustomer.getFcmToken(),
                            "Tournament starting soon!",
                            "Tournament will start in 3 minutes. Please join now!"
                    );
                    return new ResponseEntity<>("Notification sent successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("No FCM token for player", HttpStatus.BAD_REQUEST);
                }


            } else {
                return new ResponseEntity<>("Invalid role", HttpStatus.BAD_REQUEST);
            }


        }
        return new ResponseEntity<>("Invalid role", HttpStatus.BAD_REQUEST);

    }

    @GetMapping("/gender")
    public String getGender(@RequestParam String name) {
        return customCustomerService.getGenderByName(name);
    }


    @GetMapping("/active-players")
    public ResponseEntity<?> getActivePlayers(@RequestParam Long tournamentId) {
        try {
            List<Player> players = tournamentService.getActivePlayers(tournamentId);
            System.out.println("Players: " + players.size());
            return responseService.generateSuccessResponse("Players fetched successfully", players, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createOrUpdateMonthlyPlan")
    public ResponseEntity<?> createOrUpdateMonthlyPlan(@RequestParam Long vendorid, @RequestParam  BigDecimal rechargeAmount, int multiplier) {
        String monthYear = LocalDate.now().toString().substring(0, 7); // "2025-05"
        BigDecimal entryFee = BigDecimal.valueOf(3);
        BigDecimal vendorShareAmount = entryFee.multiply(PriceConstant.VENDOR_REVENUE_PERCENT);
        commonService.addVendorEarningForPayment(vendorid,entryFee, vendorShareAmount,"test");
/*        InfluencerMonthlyEarning existing = earningRepository.findByInfluencerIdAndMonthYear(influencerId, monthYear);

        if (existing == null) {
            // Insert new row
            InfluencerMonthlyEarning newEarning = new InfluencerMonthlyEarning();
            newEarning.setInfluencerId(influencerId);
            newEarning.setMonthYear(monthYear);
            newEarning.setRechargeAmount(rechargeAmount);
            newEarning.setMultiplier(multiplier);
            newEarning.setEarnedAmount(BigDecimal.ZERO); // Start with 0
            earningRepository.save(newEarning);
            return responseService.generateSuccessResponse("Monthly plan created/updated successfully", newEarning, HttpStatus.OK);

        } else {
            // Optional: update recharge or multiplier if needed
            existing.setRechargeAmount(rechargeAmount);
            existing.setMultiplier(multiplier);
            earningRepository.save(existing);
            return responseService.generateSuccessResponse("Monthly plan created/updated successfully", existing, HttpStatus.OK);

        }*/

        return responseService.generateSuccessResponse("Monthly plan created/updated successfully", null, HttpStatus.OK);
    }


   /* @PostMapping("/log-action")
    public ResponseEntity<?> logAdminTestAction(@RequestParam Long targetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String performedBy = authentication.getName();

        String actorRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_UNKNOWN");

        if (actorRole.startsWith("ROLE_")) {
            actorRole = actorRole.substring(5); // e.g., "ADMIN"
        }

        String targetType = "TEST_ENTITY";
        String activity = "Performed test admin action for entity ID " + targetId;

        adminLogService.logAction(activity, actorRole, performedBy, targetId, targetType);

        return ResponseEntity.ok("Admin log created successfully.");
    }*/

    @PutMapping("/update-usernames")
    @Transactional
    public ResponseEntity<String> updateVendorUsernames() {
        List<VendorEntity> vendors = entityManager.createQuery("FROM VendorEntity v WHERE v.user_name IS NULL OR TRIM(v.user_name) = ''", VendorEntity.class)
                .getResultList();

        int updatedCount = 0;

        for (VendorEntity vendor : vendors) {
            String firstName = vendor.getFirst_name() != null
                    ? vendor.getFirst_name().replaceAll("\\s+", "").toLowerCase()
                    : "aagveer";

            String mobile = vendor.getMobileNumber();
            String lastFourDigits = (mobile != null && mobile.length() >= 4)
                    ? mobile.substring(mobile.length() - 4)
                    : "0000";

            String username = firstName + lastFourDigits;

            // Use JPQL/Native query to only update user_name field
            entityManager.createQuery("UPDATE VendorEntity v SET v.user_name = :username WHERE v.service_provider_id = :id")
                    .setParameter("username", username)
                    .setParameter("id", vendor.getService_provider_id())
                    .executeUpdate();

            updatedCount++;
        }

        return ResponseEntity.ok("Updated usernames for " + updatedCount + " vendors.");
    }

    @PutMapping("/update-customer-usernames")
    @Transactional
    public ResponseEntity<String> updateCustomerUsernames() {
        List<CustomCustomer> customers = entityManager.createQuery(
                        "FROM CustomCustomer c WHERE c.user_name IS NULL OR TRIM(c.user_name) = ''", CustomCustomer.class)
                .getResultList();

        int updatedCount = 0;

        for (CustomCustomer customer : customers) {
            String namePart = customer.getName() != null
                    ? customer.getName().replaceAll("\\s+", "").toLowerCase()
                    : "aaguser";

            String mobile = customer.getMobileNumber();




            String lastFourDigits = (mobile != null && mobile.length() >= 4)
                    ? mobile.substring(mobile.length() - 4)
                    : "0000";

            String username = namePart + lastFourDigits;

            // Update using JPQL to bypass validation
            entityManager.createQuery("UPDATE CustomCustomer c SET c.user_name = :username WHERE c.id = :id")
                    .setParameter("username", username)
                    .setParameter("id", customer.getId())
                    .executeUpdate();

            updatedCount++;
        }

        return ResponseEntity.ok("Updated usernames for " + updatedCount + " customers.");
    }


    @GetMapping("/customers/excel")
    public void exportCustomersToExcel(HttpServletResponse response) throws IOException {
        // Set response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customers.xlsx");

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customers");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Email", "Mobile Number", "Created Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Fetch customer data sorted by createdDate DESC
        List<CustomCustomer> customers = customCustomerRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdDate"));

        // Fill data rows
        int rowNum = 1;
        for (CustomCustomer customer : customers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(customer.getName() != null ? customer.getName() : "");
            row.createCell(1).setCellValue(customer.getEmail() != null ? customer.getEmail() : "");
            row.createCell(2).setCellValue(customer.getMobileNumber() != null ? customer.getMobileNumber() : "");

            String createdDateStr = customer.getCreatedDate() != null
                    ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(customer.getCreatedDate())
                    : "";
            row.createCell(3).setCellValue(createdDateStr);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }



}
