package aagapp_backend.services.exception;


import com.community.api.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @Autowired
    private static ResponseService responseService;

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {

            return ResponseService.generateErrorResponse("Custom 404 message - Resource not found", HttpStatus.NOT_FOUND);
        }else {
            return ResponseService.generateErrorResponse("Custom error - Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

}
