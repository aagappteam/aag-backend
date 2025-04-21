package aagapp_backend.services;

import org.springframework.stereotype.Service;

@Service
public class ApiConstants {
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_ERROR = "Error";

    public static final String MOBILE_NUMBER_NULL_OR_EMPTY = "Mobile number cannot be null or empty";
    public static final String OTP_SENT_SUCCESSFULLY = "OTP has been sent to ";

    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access: Please check your API key";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error occurred";
    public static final String ERROR_SENDING_OTP = "Error sending OTP: ";

    // Exception Messages
    public static final String SOME_EXCEPTION_OCCURRED = "Some exception occurred while ";
    public static final String NUMBER_FORMAT_EXCEPTION = "Number format exception";
    public static final String CATALOG_SERVICE_NOT_INITIALIZED = "Catalog service not initialized";
    public static final String MOBILE_NUMBER_REGISTERED = "User already exists Please log in.";
    public static final String INVALID_MOBILE_NUMBER = "Invalid mobile number";
    public static final String CUSTOMER_ALREADY_EXISTS = "User already exists Please log in.";

    public static final String RATE_LIMIT_EXCEEDED = "Rate limit exceeded. Please try after some time ";

    public static final String INVALID_DATA = "Invalid data provided";
    public static final String NUMBER_REGISTERED_AS_CUSTOMER = "Number already registered as customer";
    public static final String NO_RECORDS_FOUND = "No records found";
    public static final String NO_EXISTING_RECORDS_FOUND = "No records found. Sign up first to create an account.";
    public static final String INVALID_ROLE = "Invalid role";
    public static final String ROLE_EMPTY = "Role cannot be empty";
    public static final String INVALID_REFERRAL_CODE= "You have provided wrong or expired referral code";
}

