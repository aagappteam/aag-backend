package aagapp_backend.components;

import java.math.BigDecimal;


public class Constant {


    public static final double BONUS_PERCENT = 0.05;
    public static final double USER_WIN_PERCENT = 0.62;


    public static final Double LEAGUE_PASSES_FEE = 7.0;
    public static final BigDecimal LEAGUE_PRIZE_POOL = BigDecimal.valueOf(1000.00);
    public static final int MULTIPLIER = 4;

    public static String ludobaseurl = "http://3.110.44.61:8082";
    public static String snakebaseUrl = "http://3.110.44.61:8092";

    public static final String BEARER_CONST= "Bearer ";
    public static final String REFERRAL_CODE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.referralCode = :referralCode";
    public static final float USER_REFERAL_BALANCE = 50.0f;
    public static final String REFERAL_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final Integer TENMOVES =10;
    public static final Integer SIXTEENMOVES =16;
    public static final BigDecimal USER_PERCENTAGE = new BigDecimal("0.63");
    public static final Double MAX_FEE = 100.0;
    public static final long LEAGUE_SESSION_TIME = 1;
    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.profileStatus=:profileStatus";
    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String ACTIVE_PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code AND c.signedUp=:signedUp";

    public static String PHONE_QUERY_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String USERNAME_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.user_name = :username";
    public static String USERNAME_QUERY_CUSTOM_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.user_name = :username";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SUPPORT = "SUPPORT";
    public static final String USER = "USER";
    public static int SUPPORT_ROLE = 1;
    public static int ADMIN_ROLE = 2;
    public static int ADMIN_VENDOR_PROVIDER_ROLE = 3;
    public static int VENDOR_ROLE = 4;
    public static int CUSTOMER_ROLE=5;

    public static final String SP_USERNAME_QUERY = "SELECT s FROM VendorEntity s WHERE s.user_name LIKE :username";
    public static final String SP_EMAIL_QUERY = "SELECT s FROM VendorEntity s WHERE s.primary_email LIKE :email";
    public static String FETCH_ROLE = "SELECT r.roleName FROM Role r WHERE r.roleId = :role_id";
    public static String roleUser = "CUSTOMER";
    public static String rolesuperadmin = "SUPER_ADMIN";
    public static String roleAdminServiceProvider = "SUPPORT";
    public static String rolevendor = "VENDOR";
    public static String GET_ALL_ROLES = "Select r from Role r";

    public static String GET_ROLE_BY_ROLE_ID = "SELECT r FROM Role r WHERE r.role_id = :roleId";
    public static final String GET_ALL_CUSTOMERS = "Select c from CustomCustomer c ORDER BY c.createdDate DESC";


    public static final String EMAIL_REGEXP = "^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";

    public static String SCHEDULED = "SCHEDULED";
    public static String ACTIVE = "ACTIVE";

    public static String EXPIRED = "EXPIRED";

    public static Integer TOKEN_SIZE = 2;

    public static String PROFILE_IMAGE_URL = "https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg";
    public static final String ONBOARDING_EMAIL_SUBJECT = "Registration Received – Next Steps to Become AAGVEER! ";
    public static final String APPROVED_EMAIL_SUBJECT = "AAG Veer - Vendor Account Approved";
    public static final String REJCTED_EMAIL_SUBJECT = "AAG Veer - Vendor Account Rejected";


}