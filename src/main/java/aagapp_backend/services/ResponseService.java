package aagapp_backend.services;

import aagapp_backend.entity.ErrorResponse;
import aagapp_backend.entity.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public ResponseEntity<Object> generateResponseForGame(String message, List<?> data, Long count, Long scheduledCount, Long activeCount, Long ExpiredCount, HttpStatus status){
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("message", message);
            response.put("data", data);
            response.put("totalCount", count);
            response.put("scheduledCount", scheduledCount);
            response.put("activeCount", activeCount);
            response.put("ExpiredCount", ExpiredCount);
            response.put("status", status);
            response.put("status_code", status.value());
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            response.put("isSuccess", false);
            response.put("message", e.getMessage());
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static ResponseEntity<?> generateSuccessResponseForTicket(String message, List<?> data, Long count, Long openTicketCount, Long closedTicketCount, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", count);
        response.put("openTicketCount", openTicketCount);
        response.put("closedTicketCount", closedTicketCount);
        response.put("status", status);
        response.put("status_code", status.value());
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<?> generateSuccessResponseForWithdrwalRequest(String message, List<?> data, Long count, Long approvedCount,Long rejectedCount,Long pendingCount, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", count);
        response.put("approvedCount", approvedCount);
        response.put("rejectedCount", rejectedCount);
        response.put("pendingCount", pendingCount);
        response.put("status", status);
        response.put("status_code", status.value());
        return new ResponseEntity<>(response, status);
    }

}
