package aagapp_backend.components;

import java.math.BigDecimal;
import java.util.List;

public class Constant {

    //  Bonus & Referral
    public static final double BONUS_PERCENT = 0.05;
    public static final float DOWNLOAD_BONUS = 100.0f;
    public static final float USER_REFERAL_BALANCE = 50.0f;
    public static final BigDecimal KYC_VERIFICATION_BONUS = BigDecimal.valueOf(50);
    public static final String DOWNLOAD_BONUS_DESCRIPTION = "You received Rs. " + (int) DOWNLOAD_BONUS + " bonus on your first add cash!";

    //  League & Tournament
//    public static final BigDecimal LEAGUE_PRIZE_POOL = BigDecimal.valueOf(1000.00);
    public static final int MULTIPLIER = 4;
    public static final int LEAGUE_PASS_COUNT = 3;
    public static final long LEAGUE_SESSION_TIME = 4;
    public static final long LEAGUE_SESSION_TIME_2 = 4;
    public static final long TOURNAMENT_START_TIME = 1;
    public static final long TOURNAMENT_END_TIME = 4;
    public static final BigDecimal LEAGUE_PRIZE_POOL = BigDecimal.valueOf(100.00);
    public static final BigDecimal TOURNAMENT_PRIZE_POOL = BigDecimal.valueOf(100.00);

    public static final BigDecimal TOURNAMENT_PRIZE_POOL_SENT_TO_USER = BigDecimal.valueOf(0.80);
    public static final BigDecimal TOURNAMENT_PRIZE_POOL_SENT_AS_BONUS= BigDecimal.valueOf(0.20);

    public static final Double TOURNAMENT_PRIZE_POOL_new = 100.00;

    //  Game URLs
    public static String ludobaseurl = "https://gamebackend.aagapp.com/game-api/ludo";
    public static String snakebaseUrl = "https://gamebackend.aagapp.com/game-api/snake";

    //  Test Numbers
    public static final String MOBILE_6306470701 = "6306470701";
    public static final String MOBILE_9628577197 = "9628577197";
    public static final List<String> TEST_MOBILE_NUMBERS = List.of(MOBILE_6306470701, MOBILE_9628577197);

    //  Fee & Percentages
    public static final Double MAX_FEE = 100.0;
    public static final double USER_WIN_PERCENT = 0.62;
    public static final BigDecimal USER_PERCENTAGE = new BigDecimal("0.62");

    //  Bearer
    public static final String BEARER_CONST = "Bearer ";

    //  Referral Code
    public static final String REFERRAL_CODE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.referralCode = :referralCode";
    public static final String REFERAL_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    //  Game Moves
    public static final Integer TENMOVES = 10;
    public static final Integer SIXTEENMOVES = 16;

    //  Phone & Queries
    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.profileStatus=:profileStatus";
    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String ACTIVE_PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code AND c.signedUp=:signedUp";
    public static String ROLE_QUERY_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String PHONE_QUERY_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";

    //  Username Queries
    public static String USERNAME_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.user_name = :username";
    public static String USERNAME_QUERY_CUSTOM_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.user_name = :username";

    //  Roles
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SUPPORT = "SUPPORT";
    public static final String VENDOR = "VENDOR";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String FINANCE = "FINANCE";

    public static int SUPPORT_ROLE = 1;
    public static int ADMIN_ROLE = 2;
    public static int SUPER_ADMIN_ROLE = 3;
    public static int VENDOR_ROLE = 4;
    public static int CUSTOMER_ROLE = 5;
    public static int FINANCE_ROLE = 6;

    public static final String ROLE_SUPPORT = "SUPPORT";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ADMIN_VENDOR_PROVIDER = "ADMIN_VENDOR_PROVIDER";
    public static final String ROLE_VENDOR = "VENDOR";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_FINANCE = "FINANCE";

    // Role Management
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

    //  Email
    public static final String EMAIL_REGEXP = "^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";

    // Status Constants
    public static String SCHEDULED = "SCHEDULED";
    public static String ACTIVE = "ACTIVE";
    public static String EXPIRED = "EXPIRED";


    //  Default Image
    public static String PROFILE_IMAGE_URL = "https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg";

    // Email Subjects
    public static final String ONBOARDING_EMAIL_SUBJECT = "Registration Received – Next Steps to Become AAGVEER! ";
    public static final String APPROVED_EMAIL_SUBJECT = "AAG Veer - Vendor Account Approved";
    public static final String REJCTED_EMAIL_SUBJECT = "AAG Veer - Vendor Account Rejected";
    public static final String APPLYING_KYC_EMAIL_SUBJECT = "AAG Veer - Customer Kyc Submitted";
    public static final String KYC_APPROVED_EMAIL_SUBJECT = "AAG Veer - Customer Kyc Approved";
    public static final String KYC_REJECTED_EMAIL_SUBJECT = "AAG Veer - Customer Kyc Rejected";
    public static final String PLAN_PURCHASED_EMAIL_SUBJECT = "AAG Veer - Subscription Plan Purchased";
    public static final String PLAN_EXPIREDEMAIL_SUBJECT = "AAG Veer - Subscription Plan is Expired";
    public static final String PLAN_PURCHASED_AGAIN_EMAIL_SUBJECT = "AAG Veer - Subscription Plan has been Renewed";

    //  KwickPay Config
    public static final String KwickPayUrl = "https://ubi.kwicpay.com/api/smartpay/transaction";
    public static final String KwickPayToken = "K7CAkgS5bm5cX5zqdJ4uJ65Ekp9BwD";
    public static final String KwickPayTransactionType = "spayout";
    public static final String KwickPayTransactionMode = "imps";
    public static final String UNIQUE_TXN_ID = "AAG" + System.currentTimeMillis();
}