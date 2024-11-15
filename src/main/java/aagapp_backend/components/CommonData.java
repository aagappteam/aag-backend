package aagapp_backend.components;

import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class CommonData {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public static boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return java.util.regex.Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }
    public static Map<String,Object> trimStringValues(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                String trimmedValue = ((String) entry.getValue()).trim();
                entry.setValue(trimmedValue);
            }
        }
        return map;
    }
    public static boolean isFutureDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date inputDate = sdf.parse(dateStr);
            Date currentDate = new Date();
            return inputDate.after(currentDate);
        }  catch (Exception e) {
            return false;
        }
    }
}
