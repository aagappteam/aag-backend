package aagapp_backend.services;

import aagapp_backend.dto.game.LeaderboardResponseDTOTournamentAdmin;
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

    public static ResponseEntity<?> generateSuccessResponseWithOuterCount(String message, Object data, Long count, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", count); // Moved to outer level
        response.put("status", status);
        response.put("status_code", status.value());
        return new ResponseEntity<>(response, status);
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

    public static ResponseEntity<?> generateSuccessResponseWithCountAndStatus(
            String message,
            List<?> data,
            Long totalCount,
            Long activeCount,
            Long inactiveCount,
            HttpStatus status
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", totalCount);
        response.put("activeCount", activeCount);
        response.put("inactiveCount", inactiveCount);
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


    public static ResponseEntity<?> generateSuccessResponseForVendorUpgrade(String message, List<?> data, Long count, Long openTicketCount, Long closedTicketCount,Long pendingCount, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", count);
        response.put("approvedCount", openTicketCount);
        response.put("rejectedCount", closedTicketCount);
        response.put("pendingCount", pendingCount);

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


    public static ResponseEntity<SuccessResponse> generateVendorSuccessResponse(
            String message,
            Map<String, Object> flatData,
            HttpStatus status
    ) {
        SuccessResponse response = new SuccessResponse();
        response.setMessage(message);
        response.setStatus(HttpStatus.valueOf(status.getReasonPhrase()));
        response.setStatus_code(status.value());
        response.setData(flatData);  // You want entire map with keys like "data", "page", etc.

        return new ResponseEntity<>(response, status);
    }



    public static ResponseEntity<?> generateSuccessResponseForTicket(String message, List<?> data, Long count, Long pendingCount, Long processingCount,Long resoledCount, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("totalCount", count);
        response.put("pendingCount", pendingCount);
        response.put("processingCount", processingCount);
        response.put("resolvedCount", resoledCount);
        response.put("status", status);
        response.put("status_code", status.value());
        return new ResponseEntity<>(response, status);
    }

    public ResponseEntity<?> generateResponsewithCount(HttpStatus httpStatus, String leaderboardFetchedSuccessfully, LeaderboardResponseDTOTournamentAdmin leaderboard, long totalItems) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("message", leaderboardFetchedSuccessfully);
            map.put("data", leaderboard);
            map.put("totalCount", totalItems);
            map.put("status", httpStatus);
            map.put("status_code", httpStatus.value());
            return new ResponseEntity<>(map, httpStatus);
        } catch (Exception exception) {
            map.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            map.put("isSuccess", false);
            map.put("message", exception.getMessage());
            map.put("data", null);
            return new ResponseEntity<>(map, httpStatus);
        }
    }
}
