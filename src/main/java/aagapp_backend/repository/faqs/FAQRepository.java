package aagapp_backend.repository.faqs;

import aagapp_backend.entity.faqs.FAQs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQs, Long> {
    List<FAQs> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(String question, String answer);

    List<FAQs> findByQuestionContainingIgnoreCase(String questionFilter);

    List<FAQs> findByAnswerContainingIgnoreCase(String answerFilter);

//    List<FAQs> findByCategory(String category);
}

