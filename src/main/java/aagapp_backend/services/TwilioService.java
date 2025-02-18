package aagapp_backend.services;


import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.PlayerStatus;
import aagapp_backend.enums.ProfileStatus;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class TwilioService {

    private ExceptionHandlingImplement exceptionHandling;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    private CustomCustomerService customCustomerService;
    private EntityManager entityManager;
    private VenderServiceImpl venderService;
    @Autowired
    private ResponseService responseService;

    @Autowired
    public void setVenderService(@Lazy
                                 VenderServiceImpl venderService) {
        this.venderService = venderService;
    }

    @Autowired
    public TwilioService(ExceptionHandlingImplement exceptionHandlingImplement, CustomCustomerService customCustomerService) {
        this.exceptionHandling = exceptionHandlingImplement;
        this.customCustomerService = customCustomerService;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> sendOtpToMobile(String mobileNumber, String countryCode) {

        try {
            String otp = generateOTP();

//           this.sendOtp(countryCode,mobileNumber,otp);

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
            VendorEntity existingvendor = venderService.findServiceProviderByPhone(mobileNumber, countryCode);
            String maskedNumber = this.genereateMaskednumber(mobileNumber);
            if (existingCustomer == null && existingvendor == null) {
                CustomCustomer customerDetails = new CustomCustomer();
                customerDetails.setCountryCode(countryCode);
                customerDetails.setMobileNumber(mobileNumber);
                customerDetails.setOtp(otp);
                customerDetails.setProfileStatus(ProfileStatus.PENDING);
                entityManager.persist(customerDetails);
                Player player = new Player();
                player.setPlayerId(customerDetails.getId());
                player.setPlayerStatus(PlayerStatus.READY_TO_PLAY);
                entityManager.persist(player);
                return ResponseEntity.ok(Map.of(
                        "otp", otp,
                        "message", ApiConstants.OTP_SENT_SUCCESSFULLY + maskedNumber
                ));
            } else if (existingvendor != null) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST,
                        "message", ApiConstants.NUMBER_ALREADY_REGISTERED_SERVICE_PROVIDER
                ));
            } else {
                existingCustomer.setOtp(otp);
                entityManager.merge(existingCustomer);
                return ResponseEntity.ok(Map.of(

                        "otp", otp,
                        "message", ApiConstants.OTP_SENT_SUCCESSFULLY + maskedNumber
                ));
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "message", ApiConstants.UNAUTHORIZED_ACCESS,
                        "status_code", HttpStatus.UNAUTHORIZED
                ));
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "message", ApiConstants.INTERNAL_SERVER_ERROR,
                        "status_code", HttpStatus.INTERNAL_SERVER_ERROR
                ));
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", ApiConstants.ERROR_SENDING_OTP + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR
            ));
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", ApiConstants.ERROR_SENDING_OTP + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR
            ));
        }
    }

    public String sendOtp(String countryCode, String mobileNumber, String otp) {

        Twilio.init(accountSid, authToken);
        String completeMobileNumber = countryCode + mobileNumber;

        String messageBody = "Your OTP is: " + otp;

        try {
            Message message = Message.creator(
                    new PhoneNumber(completeMobileNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            System.out.println("OTP sent successfully to " + completeMobileNumber);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }

        return otp;
    }

    @Transactional
    public ResponseEntity<?> sendOtpToMobileVendor(String mobileNumber, String countryCode) {

        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number cannot be null or empty");
        }

        try {
            String otp = generateOTP();
//            this.sendOtp(countryCode,mobileNumber,otp);

            VendorEntity existingServiceProvider = venderService.findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                existingServiceProvider = new VendorEntity();
                existingServiceProvider.setCountry_code(countryCode);
                existingServiceProvider.setMobileNumber(mobileNumber);
                existingServiceProvider.setOtp(otp);
                existingServiceProvider.setIsVerified(0);
                entityManager.persist(existingServiceProvider);
            } else {
                existingServiceProvider.setOtp(null);
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }

            String maskedNumber = this.genereateMaskednumber(mobileNumber);
            return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + maskedNumber, otp, HttpStatus.OK);


        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }

    public synchronized String genereateMaskednumber(String mobileNumber) {
        String lastFourDigits = mobileNumber.substring(mobileNumber.length() - 4);

        int numXs = mobileNumber.length() - 4;

        StringBuilder maskBuilder = new StringBuilder();
        for (int i = 0; i < numXs; i++) {
            maskBuilder.append('x');
        }
        String mask = maskBuilder.toString();

        String maskedNumber = mask + lastFourDigits;
        return maskedNumber;
    }


    public synchronized String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(8999);
        return String.valueOf(otp);
    }

}

