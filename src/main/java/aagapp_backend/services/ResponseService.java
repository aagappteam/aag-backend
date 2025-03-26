package aagapp_backend.services;

import aagapp_backend.entity.ErrorResponse;
import aagapp_backend.entity.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResponseService {

    public static ResponseEntity<SuccessResponse> generateSuccessResponse(String message, Object data, HttpStatus status) {
        SuccessResponse successResponse = new SuccessResponse();

        successResponse.setStatus(status);
        successResponse.setStatus_code(status.value());
        successResponse.setMessage(message);

        if (data instanceof Map) {
            successResponse.setData((Map<String, Object>) data);
        } else {
            successResponse.setData(data);
        }

        return new ResponseEntity<>(successResponse, status);
    }

    public static ResponseEntity<ErrorResponse> generateErrorResponse(String message,HttpStatus status)
    {
        ErrorResponse errorResponse=new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setStatus_code(status.value());
        errorResponse.setStatus(status);
        return new ResponseEntity<>(errorResponse,status);
    }

    public static ResponseEntity<?> generateSuccessResponseWithCount(String message, List<?> data, Long count, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", count);
        response.put("status", status);
        response.put("status_code", status.value());
        return new ResponseEntity<>(response, status);
    }



    public  ResponseEntity<Object> generateResponse(HttpStatus httpStatus,String msg,Object responseBody)
    {
        Map<String,Object> map = new HashMap<>();
        try
        {
            map.put("message",msg);
            map.put("data",responseBody);
            map.put("status",httpStatus);
            map.put("status_code",httpStatus.value());


            return new ResponseEntity<>(map,httpStatus);
        }
        catch ( Exception exception)
        {
            map.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            map.put("isSuccess",false);
            map.put("message",exception.getMessage());
            map.put("data",null);
            return new ResponseEntity<>(map,httpStatus);
        }
    }
}
