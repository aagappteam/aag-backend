package aagapp_backend.repository.faqs;

import aagapp_backend.entity.faqs.FAQs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQs, Long> {

    // Find FAQs by createdFor and filter question and answer
    List<FAQs> findByCreatedForIgnoreCaseAndQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(
            String createdFor, String question, String answer);

    // Find FAQs by createdFor and filter question
    List<FAQs> findByCreatedForIgnoreCaseAndQuestionContainingIgnoreCase(String createdFor, String question);

    // Find FAQs by createdFor and filter answer
    List<FAQs> findByCreatedForIgnoreCaseAndAnswerContainingIgnoreCase(String createdFor, String answer);

    // Find FAQs by createdFor
    List<FAQs> findByCreatedForIgnoreCase(String createdFor);
}


