package aagapp_backend.components;


public class Constant {

    public static final long MAX_FILE_SIZE = 1 * 1024 * 1024;
    public static final long MIN_RESIZED_IMAGE_SIZE = 500 * 1024;
    public static final long MAX_SIGNATURE_IMAGE_SIZE = 1 * 1024 * 1024;
    public static final long MIN_SIGNATURE_IMAGE_SIZE = 300 * 1024;
    public static final long MAX_PDF_SIZE = 1 * 1024 * 1024;
    public static final long MIN_PDF_SIZE = 500 * 1024;
    public static final String BEARER_CONST= "Bearer ";
    public static final String REFERRAL_CODE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.referralCode = :referralCode";
    public static final float USER_REFERAL_BALANCE = 50.0f;
    public static final String REFERAL_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final Integer TENMOVES =10;
    public static final Integer SIXTEENMOVES =16;

    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.profileStatus=:profileStatus";
    public static final String FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY = "SELECT q FROM ServiceProviderTestStatus q";
    public static final String FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY = "SELECT q FROM ServiceProviderRank q";
    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
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
    public static final String jpql = "SELECT a FROM ServiceProviderAddressRef a";

    public static String FETCH_ROLE = "SELECT r.roleName FROM Role r WHERE r.roleId = :role_id";
    public static String roleUser = "CUSTOMER";
    public static String rolesuperadmin = "SUPER_ADMIN";

    public static String roleAdminServiceProvider = "SUPPORT";
    public static String rolevendor = "VENDOR";


    public static String GET_ALL_ROLES = "Select r from Role r";

    public static String GET_ROLE_BY_ROLE_ID = "SELECT r FROM Role r WHERE r.role_id = :roleId";
    public static String PRIVILEGE_ADD_PRODUCT = "ADD_PRODUCT";
    public static String PRIVILEGE_ADD_DOCUMENT_TYPE = "ADD_DOCUMENT_TYPE";
    public static String PRIVILEGE_TICKET = "PRIVILEGE_TICKET";
    public static String GET_PRODUCT_RESERVECATEGORY_BORNBEFORE_BORNAFTER = "SELECT c FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE c.customProduct = :customProduct";
    public static String GET_PRODUCT_RESERVECATEGORY_FEE_POST = "SELECT c FROM CustomProductReserveCategoryFeePostRef c WHERE c.customProduct = :customProduct";
    public static String ADD_PRODUCT_RESERVECATEOGRY_BORNBEFORE_BORNAFTER = "INSERT INTO custom_product_reserve_category_born_before_after_reference (product_id, reserve_category_id, born_before, born_after) VALUES (:productId, :reserveCategoryId, :bornBefore, :bornAfter)";
    public static String ADD_PRODUCT_RESERVECATEOGRY_FEE_POST = "INSERT INTO custom_product_reserve_category_fee_post_reference (product_id, reserve_category_id, fee, post) VALUES (:productId, :reserveCategoryId, :fee, :post)";
    public static String GET_RESERVED_CATEGORY_BY_ID = "SELECT c FROM CustomReserveCategory c WHERE c.reserveCategoryId = :reserveCategoryId";
    public static String APPLICATION_SCOPE_STATE = "STATE";
    public static String PRIVILEGE_UPDATE_PRODUCT = "UPDATE_PRODUCT";
    public static String APPLICATION_SCOPE_CENTER = "CENTER";
    public static String PRIVILEGE_APPROVE_PRODUCT = "APPROVE_PRODUCT";
    public static String PRIVILEGE_REJECT_PRODUCT = "REJECT_PRODUCT";
    public static final String PRODUCTNOTFOUND = "Product not Found";
    public static final String CATEGORYNOTFOUND = "Category not Found";
    public static final String PRODUCTTITLENOTGIVEN = "Product MetaTitle not Given";
    public static final int MAX_REQUEST_SIZE = 100;
    public static final int MAX_NESTED_KEY_SIZE = 100;
    public static final String GET_ALL_SERVICE_PROVIDERS = "Select s from VendorEntity s ORDER BY s.createdDate DESC";
    public static final String GET_ALL_CUSTOMERS = "Select c from CustomCustomer c ORDER BY c.createdDate DESC";

    public static final String GET_ALL_TICKET_STATE = "SELECT c FROM CustomTicketState c";
    public static final String GET_TICKET_STATE_BY_TICKET_STATE_ID = "SELECT c FROM CustomTicketState c WHERE c.ticketStateId = :ticketStateId";
    public static final String GET_ALL_TICKET_TYPE = "SELECT c FROM CustomTicketType c";
    public static final String GET_TICKET_TYPE_BY_TICKET_TYPE_ID = "SELECT c FROM CustomTicketType c WHERE c.ticketTypeId = :ticketTypeId";
    public static final String GET_ALL_TICKET_STATUS = "SELECT c FROM CustomTicketStatus c";
    public static final String GET_TICKET_STATE_BY_TICKET_STATUS_ID = "SELECT c FROM CustomTicketStatus c WHERE c.ticketStatusId = :ticketStatusId";
    public static final String GET_SP_REFERRED_CANDIDATES = "Select s.customer_id from customer_referrer s Where s.service_provider_id =:service_provider_id";
    public static final Double DEFAULT_PLATFORM_FEE = 10d;

    public static final String EMAIL_REGEXP = "^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";
    public static final String GET_ALL_ORDERS_OF_ONE_CUSTOMER = "SELECT o from blc_ ";
    public static final String GET_ORDERS_USING_CUSTOMER_ID = "SELECT CAST(o.order_id AS BIGINT) FROM blc_order o WHERE o.order_number LIKE :orderNumber";
    ;
    public static final String CHECK_FOR_REPEATED_REF = "SELECT COUNT(*) FROM customer_referrer c WHERE c.customer_id = :customerId AND c.service_provider_id = :spId";
    public static final String GET_ALL_ORDERS = "SELECT o.order_id FROM blc_order o WHERE o.order_status <> 'IN_PROCESS'";
    public static final String SEARCH_ORDER_QUERY = "SELECT o.order_id FROM order_state o WHERE o.order_state_id =:orderStateId";
    public static final String GET_NEW_ORDERS = "SELECT o.order_id FROM order_state o WHERE o.order_state_id = 1";
    public static final String GET_SP_ORDER_REQUEST = "SELECT o.order_request_id FROM SP_orders_requests o WHERE o.order_id = :orderId AND o.service_provider_id = :serviceProviderId ";
    public static final String GET_ONE_SP_ORDER_REQUEST = "SELECT o.order_request_id FROM SP_orders_requests o WHERE o.service_provider_id = :serviceProviderId AND o.request_Status = :requestStatus";
    public static final String GET_ONE_SP_ALL_ORDER_REQUEST = "SELECT o.order_request_id FROM SP_orders_requests o WHERE o.service_provider_id = :serviceProviderId";
    public static final String SP_REQUEST_ACTION_ACCEPT = "ACCEPT";
    public static final String SP_REQUEST_ACTION_RETURN = "RETURN";
    public static final String SP_REQUEST_ACTION_VIEW = "VIEW";
    public static final String NOT_ELIGIBLE_SP = "SELECT s.service_provider_id FROM sp_orders_requests s WHERE order_id = :orderId AND request_status ='RETURNED'";


    public static String SCHEDULED = "SCHEDULED";
    public static String ACTIVE = "ACTIVE";

    public static String EXPIRED = "EXPIRED";

    public static Integer TOKEN_SIZE = 2;

    public static String PROFILE_IMAGE_URL = "https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg";
    public static final String ONBOARDING_EMAIL_SUBJECT = "Welcome to AAg Application – Profile Verification in Progress";

}