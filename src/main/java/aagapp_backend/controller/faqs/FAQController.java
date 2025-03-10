package aagapp_backend.controller.faqs;

import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.services.faqs.FAQService;
import aagapp_backend.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faqs")
public class FAQController {

    @Autowired
    private FAQService faqService;

    // 1. Get all FAQs with optional filters for question and answer
    @GetMapping("/allFaqs")
    public ResponseEntity<?> getFAQs(
            @RequestParam(value = "question", required = false) String questionFilter,
            @RequestParam(value = "answer", required = false) String answerFilter
    ) {
        try {
            List<FAQs> faqs = faqService.getFAQs(questionFilter, answerFilter);

            if (faqs.isEmpty()) {
                return ResponseService.generateErrorResponse("No FAQs found", HttpStatus.NO_CONTENT);
            }

            return ResponseService.generateSuccessResponse("FAQs retrieved successfully", faqs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error retrieving FAQs: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
