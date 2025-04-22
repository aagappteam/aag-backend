package aagapp_backend.controller.faqs;

import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.services.faqs.FAQService;
import aagapp_backend.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/faqs")
public class FAQController {

    @Autowired
    private FAQService faqService;

    // 1. Get all FAQs for user or vendor with optional filters for question and answer
    @GetMapping("/allFaqs/{createdFor}")
    public ResponseEntity<?> getFAQsByCreatedFor(@PathVariable String createdFor,
                                                 @RequestParam(required = false) String questionFilter,
                                                 @RequestParam(required = false) String answerFilter) {
        try {
            // Fetch FAQs for the specified createdFor (user or vendor), grouped by category
            Map<String, List<FAQs>> faqsGroupedByCategory = faqService.getFAQsGroupedByCategoryForCreatedFor(createdFor, questionFilter, answerFilter);

            // Build the response format
            List<Map<String, Object>> responseList = new ArrayList<>();

            // Iterate over the grouped FAQs and build the response
            for (Map.Entry<String, List<FAQs>> entry : faqsGroupedByCategory.entrySet()) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("category", entry.getKey());

                // List of FAQs under the current category
                List<Map<String, Object>> faqList = new ArrayList<>();
                for (FAQs faq : entry.getValue()) {
                    Map<String, Object> faqMap = new HashMap<>();
                    faqMap.put("id", faq.getId());
                    faqMap.put("question", faq.getQuestion());
                    faqMap.put("answer", faq.getAnswer());
                    faqList.add(faqMap);
                }

                categoryMap.put("faqs", faqList);
                responseList.add(categoryMap);
            }

            // Return the response
            return ResponseService.generateSuccessResponse("FAQs fetched successfully", responseList, HttpStatus.OK);
        } catch (Exception e) {

            return ResponseService.generateErrorResponse("Error fetching FAQs: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
