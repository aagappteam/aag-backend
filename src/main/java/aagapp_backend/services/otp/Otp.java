package aagapp_backend.services.otp;

import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class Otp {

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


    public String sendOtp(String countryCode, String mobileNumber, String otp) {

        Twilio.init(accountSid, authToken);
        String completeMobileNumber = countryCode + mobileNumber;

        String messageBody = "Your OTP for AAG app is: " + otp + ". Please use this code to verify your identity. It will expire in 30 seconds. - AAG Team";

        try {
            Message message = Message.creator(
                    new PhoneNumber(completeMobileNumber),
                    serviceProviderSid,
                    messageBody
            ).create();

            System.out.println("OTP sent successfully to " + completeMobileNumber);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }

        return otp;
    }




}
