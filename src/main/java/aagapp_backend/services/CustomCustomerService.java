package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;

import aagapp_backend.enums.ProfileStatus;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import java.util.regex.Pattern;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomCustomerService {

    private EntityManager entityManager;
    public CustomCustomerService(EntityManager em)
    {
        this.entityManager= em;
    }



    public Boolean validateInput(CustomCustomer customer) {
        if ( customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if (!isValidMobileNumber(customer.getMobileNumber()))
            return false;

        return true;
    }

    public boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }
    @Transactional

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber, String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return entityManager.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public CustomCustomer findCustomCustomerById(Long customerId) {
        // Check if customerId is valid
        if (customerId == null) {
            return null;
        }

        return entityManager.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


    @Transactional
    public CustomCustomer findCustomCustomerByPhoneWithOtp(String mobileNumber,String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }
        if (entityManager == null) {
            throw new IllegalStateException("EntityManager is not initialized");
        }
        return entityManager.createQuery(Constant.PHONE_QUERY_OTP, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .setParameter("profileStatus", ProfileStatus.ACTIVE)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional

    public CustomCustomer readCustomerById(Long customerId) {
        return entityManager.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
               .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);

    }


    public CustomCustomer getCustomerById(Long customerId) {
        return entityManager.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
