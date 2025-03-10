package aagapp_backend.controller.faqs;

import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.services.faqs.FAQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/faqs")
public class FAQController {

    @Autowired
    private FAQService faqService;


    // 1. Get all FAQs with optional filters for question and answer
    @GetMapping("/allFaqs")
    public ResponseEntity<List<FAQs>> getFAQs(
            @RequestParam(value = "question", required = false) String questionFilter,
            @RequestParam(value = "answer", required = false) String answerFilter

    ) {

        // Get the filtered and sorted FAQs
        List<FAQs> faqs = faqService.getFAQs(questionFilter, answerFilter);

        if (faqs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(faqs);
    }

    // 2. Create a new FAQ
    @PostMapping
    public ResponseEntity<FAQs> createFAQ(@RequestBody FAQs faq) {
        FAQs createdFAQ = faqService.createFAQ(faq);
        return ResponseEntity.status(201).body(createdFAQ);
    }

    // 3. Get a FAQ by ID
    @GetMapping("/{id}")
    public ResponseEntity<FAQs> getFAQById(@PathVariable Long id) {
        Optional<FAQs> faq = faqService.getFAQById(id);

        if (faq.isPresent()) {
            return ResponseEntity.ok(faq.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. Update an existing FAQ
    @PutMapping("/{id}")
    public ResponseEntity<FAQs> updateFAQ(@PathVariable Long id, @RequestBody FAQs faqDetails) {
        FAQs updatedFAQ = faqService.updateFAQ(id, faqDetails);

        if (updatedFAQ != null) {
            return ResponseEntity.ok(updatedFAQ);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Delete an FAQ by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }
}
