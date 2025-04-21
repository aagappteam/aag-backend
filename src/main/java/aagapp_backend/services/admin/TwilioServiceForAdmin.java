package aagapp_backend.services.admin;

import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.otp.Otp;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



import jakarta.persistence.*;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

    @Service
    public class TwilioServiceForAdmin {

        private Otp otpservice;
        private ExceptionHandlingImplement exceptionHandling;
        private String accountSid;
        private String authToken;
        private String twilioPhoneNumber;
        private AdminService adminService;
        private ResponseService responseService;
        private EntityManager entityManager;
        @Value("${service.provider.sid}")
        private String serviceProviderSid;


        @Autowired
        public void setOtpservice(Otp otpservice) {
            this.otpservice = otpservice;
        }

        @Autowired
        public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
            this.exceptionHandling = exceptionHandling;
        }

        @Value("${twilio.accountSid}")
        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid;
        }


        @Value("${twilio.authToken}")
        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        @Value("${twilio.phoneNumber}")
        public void setTwilioPhoneNumber(String twilioPhoneNumber) {
            this.twilioPhoneNumber = twilioPhoneNumber;
        }

        @Autowired
        @Lazy

        public void setAdminService(AdminService adminService) {
            this.adminService = adminService;
        }

        @Autowired
        public void setResponseService(ResponseService responseService) {
            this.responseService = responseService;
        }

        @PersistenceContext
        @Autowired
        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @Transactional
        public ResponseEntity<?> sendOtpToMobileForAdmin(String mobileNumber, String countryCode) {

            if (mobileNumber == null || mobileNumber.isEmpty()) {
                return ResponseEntity.badRequest().body("Mobile number cannot be null or empty");
            }

            try {
                Twilio.init(accountSid, authToken);
                String completeMobileNumber = countryCode + mobileNumber;
                String otp = generateOTPForAdmin();
                otpservice.sendOtp(countryCode,mobileNumber,otp);


                CustomAdmin existingAdmin = adminService.findAdminByPhone(mobileNumber,countryCode);

                if (existingAdmin == null) {
                    existingAdmin = new CustomAdmin();
                    existingAdmin.setCountry_code(countryCode);
                    existingAdmin.setMobileNumber(mobileNumber);
                    existingAdmin.setOtp(otp);
                    entityManager.persist(existingAdmin);
                } else {
                    existingAdmin.setOtp(null);
                    existingAdmin.setOtp(otp);
                    entityManager.merge(existingAdmin);
                }
                String maskedNumber = this.genereateMaskednumber(mobileNumber);

              String res =   ApiConstants.OTP_SENT_SUCCESSFULLY + " on " + maskedNumber;
               /* Map<String,Object> details=new HashMap<>();
                details.put("otp",otp);*/
                return responseService.generateSuccessResponse(res,otp, HttpStatus.OK);

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
        private synchronized String generateOTPForAdmin() {
            Random random = new Random();
            int otp = 1000 + random.nextInt(8999);
            return String.valueOf(otp);
        }

        @Transactional
        public boolean setOtpForAdmin(String mobileNumber, String countryCode) {
            CustomAdmin existingAdmin = adminService.findAdminByPhone(mobileNumber,countryCode);

            if (existingAdmin != null) {
                String storedOtp = existingAdmin.getOtp();
                if (storedOtp != null) {
                    existingAdmin.setOtp(null);
                    entityManager.merge(existingAdmin);
                    return true;
                }
            }
            return false;
        }

    }
