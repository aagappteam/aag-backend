package aagapp_backend.services.faqs;

import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.repository.faqs.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FAQService {

    @Autowired
    private FAQRepository faqRepository;

    // Get all FAQs with optional filters
    public List<FAQs> getFAQs(String questionFilter, String answerFilter) {
        try {
            if (questionFilter != null && answerFilter != null) {
                return faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(questionFilter, answerFilter);
            } else if (questionFilter != null) {
                return faqRepository.findByQuestionContainingIgnoreCase(questionFilter);
            } else if (answerFilter != null) {
                return faqRepository.findByAnswerContainingIgnoreCase(answerFilter);
            } else {
                return faqRepository.findAll();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving FAQs: " + e.getMessage());
        }
    }

    // Create a new FAQ
    public FAQs createFAQ(FAQs faq) {
        try {
            return faqRepository.save(faq);
        } catch (Exception e) {
            throw new RuntimeException("Error creating FAQ: " + e.getMessage());
        }
    }

    // Get a FAQ by ID
    public Optional<FAQs> getFAQById(Long id) {
        try {
            return faqRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving FAQ by ID: " + e.getMessage());
        }
    }

    // Update an existing FAQ
    public FAQs updateFAQ(Long id, FAQs faqDetails) {
        try {
            Optional<FAQs> faq = faqRepository.findById(id);
            if (faq.isPresent()) {
                FAQs updatedFAQ = faq.get();
                updatedFAQ.setQuestion(faqDetails.getQuestion());
                updatedFAQ.setAnswer(faqDetails.getAnswer());
                return faqRepository.save(updatedFAQ);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error updating FAQ: " + e.getMessage());
        }
    }

    // Delete an FAQ by ID
    public boolean deleteFAQ(Long id) {
        try {
            Optional<FAQs> faq = faqRepository.findById(id);
            if (faq.isPresent()) {
                faqRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting FAQ: " + e.getMessage());
        }
    }
}
