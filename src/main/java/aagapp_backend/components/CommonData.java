package aagapp_backend.components;

import org.springframework.stereotype.Component;

@Component
public class CommonData {

    public static boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return java.util.regex.Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }
}
