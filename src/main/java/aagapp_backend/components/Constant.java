package aagapp_backend.components;



public class Constant {

    public static final long MAX_FILE_SIZE = 1 * 1024 * 1024;
    public static final long MIN_RESIZED_IMAGE_SIZE = 500 * 1024;
    public static final long MAX_SIGNATURE_IMAGE_SIZE= 1 * 1024 * 1024;
    public static final long MIN_SIGNATURE_IMAGE_SIZE= 300 * 1024;
    public static final long MAX_PDF_SIZE =  1 * 1024 * 1024;
    public static final long MIN_PDF_SIZE = 500 * 1024;
    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.otp=:otp";
    public static String ID_QUERY = "SELECT c FROM CustomCustomer c WHERE c.customer_id = :customer_id";
    public static final String FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY= "SELECT q FROM ServiceProviderTestStatus q";
    public static final String FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY= "SELECT q FROM ServiceProviderRank q";
    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String PHONE_QUERY_ADMIN="SELECT c FROM CustomAdmin c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String USERNAME_QUERY_SERVICE_PROVIDER = "SELECT c FROM VendorEntity c WHERE c.user_name = :username";
    public static String USERNAME_QUERY_CUSTOM_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.user_name = :username";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String USER = "USER";
    public static final int INITIAL_STATUS = 1;
    public static final Long INITIAL_TEST_STATUS = 1L;
    public static final Long TEST_COMPLETED_STATUS = 2L;
    public static final Long APPROVED_TEST = 3L;


    public static String STATE_CODE_QUERY = "SELECT s FROM StateCode s WHERE s.state_name = :state_name";
    public static final String SP_USERNAME_QUERY = "SELECT s FROM VendorEntity s WHERE s.user_name LIKE :username";
    public static final String SP_EMAIL_QUERY = "SELECT s FROM VendorEntity s WHERE s.primary_email LIKE :email";
    public static final String jpql = "SELECT a FROM ServiceProviderAddressRef a";

    public static String FETCH_ROLE = "SELECT r.roleName FROM Role r WHERE r.roleId = :role_id";
    public static String roleUser = "CUSTOMER";
    public static String roleAdminServiceProvider="ADMIN_SERVICE_PROVIDER";
    public static String rolevendor = "VENDOR";

    public static String GET_ALL_LANGUAGES = "SELECT s FROM ServiceProviderLanguage s";
    public static String OTP_SERVICE_PROVIDER = "SELECT c.otp FROM VendorEntity c WHERE c.mobileNumber = :mobileNumber";
    public static String serviceProviderRoles = "SELECT c.privilege_id FROM service_provider_privileges c WHERE c.service_provider_id = :serviceProviderId";
    public static String GET_PRIVILEGES_COUNT = "SELECT COUNT(*) FROM Privileges";
    public static String GET_ALL_PRIVILEGES = "SELECT p FROM Privileges s";
    public static String GET_INFRA_COUNT = "SELECT COUNT(*) FROM ServiceProviderInfra";
    public static String GET_INFRA_LIST = "SELECT s FROM ServiceProviderInfra s";
    public static String GET_SERVICE_PROVIDER_DEFAULT_ADDRESS="SELECT a from ServiceProviderAddressRef a where address_name =:address_name";
    public static String GET_COUNT_OF_ROLES="Select COUNT(*) from Role";
    public static String GET_COUNT_OF_STATUS="Select COUNT(*) from ServiceProviderStatus";
    public static String GET_ALL_STATUS="Select s from ServiceProviderStatus s";
    public static String GET_ALL_ROLES="Select r from Role r";
    public static String SOME_EXCEPTION_OCCURRED = "Some exception occurred";
    public static String NUMBER_FORMAT_EXCEPTION = "Number format exception";
    public static String CATALOG_SERVICE_NOT_INITIALIZED = "Catalog service not initialized";
    public static String GET_STATES_LIST="Select s from StateCode s";
    public static String GET_QUALIFICATIONS_COUNT = "SELECT COUNT(*) FROM Qualification";
    public static String GET_TYPING_TEXT_COUNT = "SELECT COUNT(*) FROM TypingText";

    public static String GET_ORDER_ITEM_PRODUCT="Select p.product_id from custom_order_item_product p where p.order_item_id =:orderItemId";
    public static String CANNOT_ADD_MORE_THAN_ONE_FORM="You can only add one of this form. Please choose a different form if you need more";
    public static String GET_ALL_APPLICATION_SCOPE = "SELECT * FROM custom_application_scope";
    public static String GET_ALL_STATES = "SELECT * FROM state_codes";
    public static String GET_ALL_RESERVED_CATEGORY = "SELECT * FROM custom_reserve_category";
    public static String GET_COUNT_OF_JOB_ROLE = "SELECT COUNT(c) FROM CustomJobGroup c";
    public static String GET_ALL_JOB_GROUP = "SELECT s FROM CustomJobGroup s";
    public static String GET_APPLICATION_SCOPE_BY_ID = "SELECT c FROM CustomApplicationScope c WHERE c.applicationScopeId = :applicationScopeId";
    public static Integer DEFAULT_QUANTITY = 100000;
    public static Integer DEFAULT_PRIORITY_LEVEL = 3;
    public static String GET_JOB_GROUP_BY_ID = "SELECT c FROM CustomJobGroup c WHERE c.jobGroupId = :jobGroupId";
    public static String GET_ALL_PRODUCT_STATE = "SELECT c FROM CustomProductState c";
    public static String GET_PRODUCT_STATE_BY_ID = "SELECT c FROM CustomProductState c WHERE c.productStateId = :productStateId";
    public static String GET_PRODUCT_STATE_BY_NAME = "SELECT c FROM CustomProductState c WHERE c.productState = :productStateName";
    public static String PRODUCT_STATE_NEW = "NEW";
    public static String PRODUCT_STATE_DRAFT="DRAFT";
    public static String PRODUCT_STATE_MODIFIED = "MODIFIED";
    public static String PRODUCT_STATE_LIVE = "LIVE";
    public static String PRODUCT_STATE_APPROVED = "APPROVED";
    public static String PRODUCT_STATE_EXPIRED = "EXPIRED";
    public static String PRODUCT_STATE_END = "END";
    public static String PRODUCT_STATE_REJECTED = "REJECTED";
    /*public static String GET_STATES_LIST="SELECT s FROM StateCode s";*/
    public static String SERVICE_PROVIDER_PRIVILEGE = "SELECT privilege_id FROM service_provider_privileges WHERE service_provider_id = :serviceProviderId";
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
    public static final int MAX_REQUEST_SIZE=100;
    public static  final int MAX_NESTED_KEY_SIZE=100;
    public static final String GET_ALL_SERVICE_PROVIDERS="Select s from VendorEntity s";
    public static final String GET_ALL_CUSTOMERS="Select c from CustomCustomer c";

    public static final String GET_ALL_TICKET_STATE = "SELECT c FROM CustomTicketState c";
    public static final String GET_TICKET_STATE_BY_TICKET_STATE_ID = "SELECT c FROM CustomTicketState c WHERE c.ticketStateId = :ticketStateId";
    public static final String GET_ALL_TICKET_TYPE = "SELECT c FROM CustomTicketType c";
    public static final String GET_TICKET_TYPE_BY_TICKET_TYPE_ID = "SELECT c FROM CustomTicketType c WHERE c.ticketTypeId = :ticketTypeId";
    public static final String GET_ALL_TICKET_STATUS = "SELECT c FROM CustomTicketStatus c";
    public static final String GET_TICKET_STATE_BY_TICKET_STATUS_ID = "SELECT c FROM CustomTicketStatus c WHERE c.ticketStatusId = :ticketStatusId";
    public static final String GET_SP_REFERRED_CANDIDATES="Select s.customer_id from customer_referrer s Where s.service_provider_id =:service_provider_id";
    public static final Double DEFAULT_PLATFORM_FEE = 10d;

    public static final String GET_ALL_REJECTION_STATUS = "SELECT c FROM CustomProductRejectionStatus c";
    public static final String GET_REJECTION_STATUS_BY_REJECTION_ID = "SELECT c FROM CustomProductRejectionStatus c WHERE c.rejectionStatusId = :rejectionStatusId";
    public static final String GET_STATE_BY_STATE_ID = "SELECT c FROM StateCode c WHERE c.state_id = :stateId";

    public static final String GET_ALL_GENDER = "SELECT c FROM CustomGender c";
    public static final String GET_GENDER_BY_GENDER_ID = "SELECT c FROM CustomGender c WHERE c.genderId = :genderId";
    public static final Double MAX_HEIGHT = 300d;
    public static final Double MIN_HEIGHT = 50d;
    public static final Double MAX_WEIGHT = 700d;
    public static final Double MIN_WEIGHT = 2d;
    public static final Double MAX_SHOE_SIZE = 12d;
    public static final Double MIN_SHOE_SIZE = 2d;
    public static final Double MAX_WAIST_SIZE = 120d;
    public static final Double MIN_WAIST_SIZE = 10d;
    public static final Double MAX_CHEST_SIZE = 125d;
    public static final Double MIN_CHEST_SIZE = 20d;
    public static final String GET_RESERVE_CATEGORY_BY_ID= "SELECT r FROM CustomReserveCategory r WHERE r.reserveCategoryName = :name";
    public static final String GET_PRODUCT_GENDER_PHYSICAL_REQUIREMENT = "SELECT c FROM CustomProductGenderPhysicalRequirementRef c WHERE c.customProduct = :customProduct";
    public static final String GET_RESERVE_CATEGORY_FEE= "SELECT p.fee FROM custom_product_reserve_category_fee_post_reference p WHERE p.product_id = :pid AND p.reserve_category_id = :reserveCategoryId";
    public static final String GET_ALL_SUBJECT = "SELECT c FROM CustomSubject c";
    public static final String GET_ALL_STREAM = "SELECT c FROM CustomStream c";
    public static final String GET_SUBJECT_BY_SUBJECT_ID = "SELECT c FROM CustomSubject c WHERE c.subjectId = :subjectId";
    public static final String GET_STREAM_BY_STREAM_ID = "SELECT c FROM CustomStream c WHERE c.streamId = :streamId";
    public static final String GET_ALL_SECTOR = "SELECT c FROM CustomSector c";
    public static final String GET_SECTOR_BY_SECTOR_ID = "SELECT c FROM CustomSector c WHERE c.sectorId = :sectorId";
    public static final String GET_QUALIFICATION_BY_ID = "SELECT c FROM Qualification c WHERE c.qualification_id = :qualificationId";
    public static final String PINCODE_REGEXP="^\\d{6}$";
    public static final String CITY_REGEXP="^[A-Za-z\\\\s]+$";
    public static final String EMAIL_REGEXP="^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";
    public static final String GET_ALL_ORDERS_OF_ONE_CUSTOMER="SELECT o from blc_ ";
    public static final String GET_ORDERS_USING_CUSTOMER_ID = "SELECT CAST(o.order_id AS BIGINT) FROM blc_order o WHERE o.order_number LIKE :orderNumber";;
    public static final String CHECK_FOR_REPEATED_REF="SELECT COUNT(*) FROM customer_referrer c WHERE c.customer_id = :customerId AND c.service_provider_id = :spId";
    public static final String GET_ALL_ORDERS="SELECT o.order_id FROM blc_order o WHERE o.order_status <> 'IN_PROCESS'";
    public static final String SEARCH_ORDER_QUERY="SELECT o.order_id FROM order_state o WHERE o.order_state_id =:orderStateId";
    public static final String GET_NEW_ORDERS="SELECT o.order_id FROM order_state o WHERE o.order_state_id = 1";
    public static final String GET_SP_ORDER_REQUEST="SELECT o.order_request_id FROM SP_orders_requests o WHERE o.order_id = :orderId AND o.service_provider_id = :serviceProviderId ";
    public static final String GET_ONE_SP_ORDER_REQUEST="SELECT o.order_request_id FROM SP_orders_requests o WHERE o.service_provider_id = :serviceProviderId AND o.request_Status = :requestStatus";
    public static final String GET_ONE_SP_ALL_ORDER_REQUEST="SELECT o.order_request_id FROM SP_orders_requests o WHERE o.service_provider_id = :serviceProviderId" ;
    public static final String SP_REQUEST_ACTION_ACCEPT="ACCEPT";
    public static final String SP_REQUEST_ACTION_RETURN="RETURN";
    public static final String SP_REQUEST_ACTION_VIEW="VIEW";
    public static final String NOT_ELIGIBLE_SP="SELECT s.service_provider_id FROM sp_orders_requests s WHERE order_id = :orderId AND request_status ='RETURNED'";

}
