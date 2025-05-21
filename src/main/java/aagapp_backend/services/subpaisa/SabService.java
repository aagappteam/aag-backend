package aagapp_backend.services.subpaisa;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JToolTip;

import aagapp_backend.utils.Encryptor;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.catalina.core.ApplicationContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
//import com.sabpaisa_pg.util.Encryptor;

@Service
public class SabService {

    @Value("${clientCode}")
    private String clientCode;

    @Value("${transUserName}")
    private String transUserName;

    @Value("${transUserPassword}")
    private String transUserPassword;

/*    @Value("${callbackUrl}")
    private String callbackUrl;*/
    private static final Dotenv dotenv = Dotenv.load();

    private String callbackUrl = dotenv.get("CALLBACK_URL");


    @Value("${authKey}")
    private String authKey;

    @Value("${authIV}")
    private String authIV;



    public ModelAndView getSabPaisaPgService() {
        String spURL = null;

        String payerName = "Vijay";
        String payerEmail = "test@email.in";
        long payerMobile = 1234567890;
        String clientTxnId = RandomStringUtils.randomAlphanumeric(20).toUpperCase();
        System.out.println("clientTxnId :: " + clientTxnId);
        byte amount = 10;
        char channelId = 'W';
        String Class = "AAg test";
		String vpa = "9531863874@ybl";
		String browserDetails = "Browser Language|Browser Color Depth|Browser Screen Height|Browser Screen Width|Browser�Time�Zone";
        Boolean byPassFlag = true;
        String modeTransfer = "UPI_APPS_MODE_TRANSFER";

        String transDate = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        spURL = "payerName=" + payerName.trim() + "&payerEmail=" + payerEmail.trim() + "&payerMobile=" + payerMobile
                + "&clientTxnId=" + clientTxnId.trim() + "&amount=" + amount + "&clientCode=" + clientCode.trim()
                + "&transUserName=" + transUserName.trim() + "&transUserPassword=" + transUserPassword.trim()
                + "&callbackUrl=" + callbackUrl.trim() + "&channelId=" + channelId + "&Class=" + Class + "&vpa=" + vpa.trim()
        + "&browserDetails=" + browserDetails.trim() + "&byPassFlag=" + byPassFlag + "&modeTransfer=" + modeTransfer + "&transDate=" + transDate;;


/*        String payerName = "Vijay";
        String payerEmail = "test@email.in";
        long payerMobile = 1234567890;
        String clientTxnId = RandomStringUtils.randomAlphanumeric(20).toUpperCase();
        System.out.println("clientTxnId :: " + clientTxnId);
        byte amount = 10;
//        String mcc = "00000000000000007399";

        char channelId = 'W';
        String Class = "AAG App";
        String vpa = "9531863874@ybl";
        String browserDetails = "Browser Language|Browser Color Depth|Browser Screen Height|Browser Screen Width|Browser�Time�Zone";

        String paymentMode = "UPI";
        String purpose = "Test Transaction";
        String txnNote = "Transaction for test purpose";
        Boolean byPassFlag = true;
        String modeTransfer = "UPI_APPS_MODE_TRANSFER";
        String seamlessType  = "S2S";
        String transDate = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        spURL = "payerName=" + payerName.trim()
                + "&payerEmail=" + payerEmail.trim()
                + "&payerMobile=" + payerMobile
                + "&clientTxnId=" + clientTxnId.trim()
                + "&amount=" + amount
                + "&clientCode=" + clientCode.trim()
                + "&transUserName=" + transUserName.trim()
                + "&transUserPassword=" + transUserPassword.trim()
                + "&callbackUrl=" + callbackUrl.trim()
                + "&channelId=" + channelId
                + "&Class=" + Class
                + "&vpa=" + vpa.trim()
                + "&paymentMode=" + paymentMode
                + "&purpose=" + purpose
                + "&txnNote=" + txnNote
                + "&transDate=" + transDate
//                + "&mcc=" + mcc
                + "&byPassFlag=" + byPassFlag
                + "&modeTransfer=" + modeTransfer
                + "&seamlessType=" + seamlessType
                + "&browserDetails=" + browserDetails;*/
        System.out.println("spURL :: " + spURL);


        try {
            spURL = Encryptor.encrypt(authKey.trim(), authIV.trim(), spURL.trim());
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ModelAndView view = new ModelAndView("NewFile");

        view.addObject("encData", spURL);
        view.addObject("clientCode", clientCode);
        // view.addObject("URL", spDomain);

        return view;
    }

    public ModelAndView getSabPaisaPgServiceMobile() {

        String spURL = null;

        String payerName = "Vijay";
        String payerEmail = "test@email.in";
        long payerMobile = 1234567890;
        String clientTxnId = RandomStringUtils.randomAlphanumeric(20).toUpperCase();
        System.out.println("clientTxnId :: " + clientTxnId);
        byte amount = 10;
        String mcc = "00000000000000007399";

        char channelId = 'M';
        String Class = "AAG App";
        String vpa = "9531863874@ybl";
        String browserDetails = "Browser Language|Browser Color Depth|Browser Screen Height|Browser Screen Width|Browser�Time�Zone";

        String paymentMode = "UPI";
        String purpose = "Test Transaction";
        String txnNote = "Transaction for test purpose";
        Boolean byPassFlag = true;
        String modeTransfer = "UPI_APPS_MODE_TRANSFER";
        String seamlessType  = "S2S";
        String transDate = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        spURL = "payerName=" + payerName.trim()
                + "&payerEmail=" + payerEmail.trim()
                + "&payerMobile=" + payerMobile
                + "&clientTxnId=" + clientTxnId.trim()
                + "&amount=" + amount
                + "&clientCode=" + clientCode.trim()
                + "&transUserName=" + transUserName.trim()
                + "&transUserPassword=" + transUserPassword.trim()
                + "&callbackUrl=" + callbackUrl.trim()
                + "&channelId=" + channelId
                + "&Class=" + Class
                + "&vpa=" + vpa.trim()
                + "&paymentMode=" + paymentMode
                + "&purpose=" + purpose
                + "&txnNote=" + txnNote
                + "&transDate=" + transDate
                + "&mcc=" + mcc
                + "&byPassFlag=" + byPassFlag
                + "&modeTransfer=" + modeTransfer
                + "&seamlessType=" + seamlessType
                + "&browserDetails=" + browserDetails;





        System.out.println("spURL :: " + spURL);


        try {
            spURL = Encryptor.encrypt(authKey.trim(), authIV.trim(), spURL.trim());
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ModelAndView view = new ModelAndView("NewFile");

        view.addObject("encData", spURL);
        view.addObject("clientCode", clientCode);
        // view.addObject("URL", spDomain);

        return view;
    }

    public String getPgResponseService(String encResponse) {
        String arr[] = null;
        String decText = null;
        try {
            decText = Encryptor.decrypt(authKey, authIV, encResponse);
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(decText);
        System.out.println("\n\n");
        /* response Data */
        arr = decText.split("&");
        for (String str : arr)
            System.out.println(str);
        return decText;
    }

    public String getStatusEnq() {
        String clientCode="LPSD1";
        String clientTxnId="TESTING010323054311453";
//		String clientTxnId="2808280223081849";
//		String clientTxnId="U8lhDcmOJC1680343578";
//		String authIV="BliSEKSVlyzyrAfY";
//		String authKey="wSAuEpNNFHkwWoET";
        String encryptedString=null;
        String query="clientCode="+clientCode.trim()
                +"&clientTxnId="+clientTxnId.trim();

        System.out.println("Parameter : : : " +query);

        try {
            encryptedString=Encryptor.encrypt(authKey.trim(), authIV.trim(), query.trim());

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Encrypted String ::: " + encryptedString);

        JSONObject jsonObject=new JSONObject();

        jsonObject.put("clientCode","LPSD1");
        jsonObject.put("statusTransEncData",encryptedString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request=new HttpEntity<>(jsonObject.toString(),headers);
        RestTemplate restTemplate=new RestTemplate();
        String statusResponseData = restTemplate.postForObject("https://stage-txnenquiry.sabpaisa.in/SPTxtnEnquiry/getTxnStatusByClientxnId", request, String.class);
        System.out.println("statusResponseData : : : :" +statusResponseData);
        String decryptedString=null;
        JSONObject statusResponse=new JSONObject(statusResponseData);
        try {
            decryptedString = Encryptor.decrypt(authKey, authIV,statusResponse.getString("statusResponseData"));

        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                 | UnsupportedEncodingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                 | BadPaddingException e) {

            e.printStackTrace();
        }

        System.out.println("DecryptedString ::::: "+decryptedString);
        return decryptedString;
//
    }


    public String getAck() {

        String clientCode = "LPSD1";
        String clientTxnId = "7RGKYUE1UMIY98RDK4SA";
        String acknowledgeEnc = "clientCode=" + clientCode + "&clientTxnid=" +
                clientTxnId+"&acknowledgeFlag="+true;

        System.out.println("acknowledgeEnc+++++++++++++++"+acknowledgeEnc);
        String encrypt=null;
        try {
            encrypt = Encryptor.encrypt(authKey, authIV, acknowledgeEnc);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                 | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException
                 | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("encrypt__________"+ encrypt);

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("clientCode", clientCode);
        jsonObject.put("acknowledgeEnc", encrypt);

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> httpEntity=new HttpEntity<>(jsonObject.toString() ,httpHeaders);

        System.out.println("httpEntity_______"+httpEntity);

        RestTemplate restTemplate=new RestTemplate();
        String postForObject = restTemplate.postForObject("https://stage-doubleverification.sabpaisa.in/SPTxtnEnquiry/acknowledge", httpEntity, String.class);
        System.out.println("postForObject__________"+postForObject);
        return postForObject;

    }

    public String GetBank() {
        String clientCode = "LPSD1";
        String modeTransfer = "NB_MODE_TRANSFER";
        String acknowledgeEnc = "&modeTransfer=" + modeTransfer;
        String encrypt=null;
        try {
            encrypt = Encryptor.encrypt(authKey, authIV, acknowledgeEnc);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                 | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException
                 | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("encrypt"+encrypt);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("clientCode", clientCode);
        jsonObject.put("modeTransfer", encrypt);

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity=new HttpEntity<>(jsonObject.toString(),httpHeaders);

        System.out.println("httpEntity"+httpEntity);

        RestTemplate restTemplate=new RestTemplate();
        String postForObject = restTemplate.postForObject("https://stage-securepay.sabpaisa.in/SabPaisa/merchantConfiguredBank", httpEntity, String.class);
        System.out.println("postForObject"+postForObject);
        return postForObject;
    }


}














//String clientCode = "LPSD1";
//String clientTxnId = "TESTING010323054311453";
//String parameters = "clientCode=" + clientCode.trim() + "&clientTxnId=" + clientTxnId.trim();
//System.out.println("parameters::::::::" + parameters);
//String statusTransEncData = null;
//
//try {
//	statusTransEncData = Encryptor.encrypt(authKey, authIV, parameters);
//} catch (InvalidKeyException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch (IllegalBlockSizeException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch (BadPaddingException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch (InvalidAlgorithmParameterException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch (NoSuchAlgorithmException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch (NoSuchPaddingException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} catch (UnsupportedEncodingException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//
//String decryptData = null;
//
////try {
////	decryptData = Encryptor.decrypt(authKey, authIV, statusTransEncData);
////} catch (InvalidKeyException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////} catch (NoSuchAlgorithmException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////} catch (NoSuchPaddingException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////} catch (UnsupportedEncodingException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////} catch (InvalidAlgorithmParameterException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////} catch (IllegalBlockSizeException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////} catch (BadPaddingException e) {
////	// TODO Auto-generated catch block
////	e.printStackTrace();
////}
////
////System.out.println("decryptData :: " + decryptData);
//
//JSONObject jsonObject = new JSONObject();
//jsonObject.put("clientCode", clientCode.trim());
//jsonObject.put("statusTransEncData", statusTransEncData.trim());
//
//HttpHeaders httpHeaders = new HttpHeaders();
//httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//HttpEntity<String> HttpRequest = new HttpEntity<String>(jsonObject.toString(), httpHeaders);
//
//RestTemplate restTemplate = new RestTemplate();
//String response = restTemplate.postForObject("https://stage-securepay.sabpaisa.in/SPTxtnEnquiry/getTxnStatusByClientxnId",
//		HttpRequest, String.class);
////restTemplate.getFor
//
//System.out.println(response);
////JSONObject object = new JSONObject(response);
////System.out.println(object);
////String decrypt=null;
////try {
////	 decrypt = Encryptor.decrypt(authKey.trim(), authIV.trim(), object.getString(object.toString()));
////	 System.out.println("decrypt_______________"+decrypt);
////} catch (Exception e) {
////	e.printStackTrace();
////}
////System.out.println("statusTransEncData+++++++++++++++++++++" + statusTransEncData);
////ModelAndView andView = new ModelAndView("index");
////andView.addObject("statusTransEncData", statusTransEncData);
////return andView;
////System.out.println("decrypt"+decrypt);
//JSONObject object=new JSONObject(response);
//String decrypt=null;
//try {
//	decrypt = Encryptor.decrypt(authKey, authIV, object.getString(response));
//} catch (Exception e) {
//	// TODO: handle exception
//	e.printStackTrace();
//}
//
//return  decrypt;
