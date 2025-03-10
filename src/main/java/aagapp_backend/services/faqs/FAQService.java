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
        if (questionFilter != null && answerFilter != null) {
            return faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(questionFilter, answerFilter);
        } else if (questionFilter != null) {
            return faqRepository.findByQuestionContainingIgnoreCase(questionFilter);
        } else if (answerFilter != null) {
            return faqRepository.findByAnswerContainingIgnoreCase(answerFilter);
        } else {
            return faqRepository.findAll();
        }
    }

    // Create a new FAQ
    public FAQs createFAQ(FAQs faq) {
        return faqRepository.save(faq);
    }

    // Get a FAQ by ID
    public Optional<FAQs> getFAQById(Long id) {
        return faqRepository.findById(id);
    }

    // Update an existing FAQ
    public FAQs updateFAQ(Long id, FAQs faqDetails) {
        Optional<FAQs> faq = faqRepository.findById(id);
        if (faq.isPresent()) {
            FAQs updatedFAQ = faq.get();
            updatedFAQ.setQuestion(faqDetails.getQuestion());
            updatedFAQ.setAnswer(faqDetails.getAnswer());
            return faqRepository.save(updatedFAQ);
        }
        return null;
    }

    // Delete an FAQ by ID
    public void deleteFAQ(Long id) {
        faqRepository.deleteById(id);
    }
}
