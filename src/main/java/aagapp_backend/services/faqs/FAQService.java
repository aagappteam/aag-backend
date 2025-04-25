package aagapp_backend.services.faqs;

import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.repository.faqs.FAQRepository;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FAQService {

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    // Method to get FAQs by 'createdFor' (User or Vendor) with optional filters for question and answer
    public Map<String, List<FAQs>> getFAQsGroupedByCategoryForCreatedFor(String createdFor,
                                                                         String questionFilter,
                                                                         String answerFilter) {
        try {
            List<FAQs> faqs;

            // If both filters are provided, fetch based on createdFor and the filters
            if (questionFilter != null && answerFilter != null) {
                faqs = faqRepository.findByCreatedForIgnoreCaseAndQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(
                        createdFor, questionFilter, answerFilter);
            } else if (questionFilter != null) {
                faqs = faqRepository.findByCreatedForIgnoreCaseAndQuestionContainingIgnoreCase(createdFor, questionFilter);
            } else if (answerFilter != null) {
                faqs = faqRepository.findByCreatedForIgnoreCaseAndAnswerContainingIgnoreCase(createdFor, answerFilter);
            } else {
                faqs = faqRepository.findByCreatedForIgnoreCase(createdFor);
            }

            return faqs.stream()
                    .collect(Collectors.groupingBy(faq -> Optional.ofNullable(faq.getCategory()).orElse("Uncategorized")));

//            return faqs.stream().collect(Collectors.groupingBy(FAQs::getCategory));
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error retrieving FAQs for " + createdFor + ": " + e.getMessage());
        }
    }


    // Create a new FAQ
    public FAQs createFAQ(FAQs faq) {
        try {
            return faqRepository.save(faq);
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            throw new RuntimeException("Error creating FAQ: " + e.getMessage());
        }
    }

    // Get a FAQ by ID
    public Optional<FAQs> getFAQById(Long id) {
        try {
            return faqRepository.findById(id);
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            throw new RuntimeException("Error retrieving FAQ by ID: " + e.getMessage());
        }
    }

    // Update an existing FAQ
    public FAQs updateFAQ(Long id, FAQs faqDetails) {
        try {
            Optional<FAQs> faq = faqRepository.findById(id);
            if (faq.isPresent()) {
                FAQs updatedFAQ = faq.get();
                updatedFAQ.setCategory(faqDetails.getCategory());  // Make sure category is updated as well
                updatedFAQ.setQuestion(faqDetails.getQuestion());
                updatedFAQ.setAnswer(faqDetails.getAnswer());
                return faqRepository.save(updatedFAQ);
            }
            return null;
        } catch (Exception e) {
            exceptionHandling.handleException(e);

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
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error deleting FAQ: " + e.getMessage());
        }
    }

    @Transactional
    public void addFAQIfNeeded() {
        // Check if any FAQs already exist
        if (entityManager.createQuery("SELECT COUNT(f) FROM FAQs f", Long.class).getSingleResult() == 0) {

            // Insert all FAQs
            insertFAQ("General", "What is AAG?",
                    "AAG is a real-money gaming platform that allows users to play and earn money by engaging in various games. You can participate in games like Ludo, Snake & Ladders, and more, and earn real cash rewards based on your performance.", "User");

            insertFAQ("General", "How do I sign up for AAG?",
                    "To sign up, download the AAG app from the Play Store (for Android users) or App Store (for iOS users). Once downloaded, click on the Sign Up button and provide the necessary details like your name, phone number, and email.", "User");

            insertFAQ("General", "What are the KYC requirements?",
                    "To fully access all features on AAG, you will need to complete your KYC (Know Your Customer) verification. This process includes submitting a government-issued ID (e.g., Aadhaar, Passport, Voter ID) and a selfie for identity confirmation.", "User");

            insertFAQ("General", "How can I deposit money into my AAG account?",
                    "You can deposit money into your AAG account through various payment methods, including:\n" +
                            "UPI\n" +
                            "Debit/Credit Cards\n" +
                            "Net Banking\n" +
                            "Wallets (Google Pay, PhonePe, etc.)\n\n" +
                            "To deposit, navigate to the Deposit section in your app, select the preferred method, and follow the instructions.", "User");

            insertFAQ("General", "How can I withdraw my earnings?",
                    "You can withdraw your earnings by requesting a withdrawal via the Withdrawal section of the app. Ensure that your KYC is completed before making any withdrawals. Your funds will be transferred to your registered bank account or wallet, depending on the withdrawal method selected.", "User");

            insertFAQ("General", "Is AAG secure to use?",
                    "Yes, AAG uses state-of-the-art security measures to protect your data and transactions. We encrypt all sensitive information and work with trusted payment partners to ensure secure transactions.", "User");

            insertFAQ("General", "What games can I play on AAG?",
                    "Currently, AAG offers casual games like Ludo, Snake & Ladders, and more. These games are designed for all ages and skill levels. New games may be added to the platform in the future.", "User");

            insertFAQ("General", "Can I play for free?",
                    "Yes, AAG offers free-to-play games, but to earn real money, you will need to participate in cash-based games. The winnings depend on your performance in the games.", "User");

            insertFAQ("General", "How do I contact support?",
                    "If you have any issues or questions, you can reach our support team through the following channels:\n" +
                            "Email: support@aag.com\n" +
                            "Phone: +91 XXXXXXXXXX\n" +
                            "In-app Support Chat", "User");

            insertFAQ("General", "How do I delete my AAG account?",
                    "If you wish to delete your AAG account, please contact our support team at support@aag.com. Please note that once your account is deleted, all data, including transaction history and funds, will be permanently removed.", "User");

            insertFAQ("General", "How do I refer friends to AAG?",
                    "You can refer your friends by using the Referral Code available in the app. When your friends sign up and play on AAG using your code, both you and your friend will receive referral bonuses.", "User");

            insertFAQ("General", "Are there any age restrictions for playing on AAG?",
                    "Yes, you must be 18 years or older to participate in real-money games on AAG. The platform is strictly for adult users, and all users must comply with age verification during the sign-up process.", "User");

            insertFAQ("General", "How do I improve my chances of winning?",
                    "Winning on AAG depends on skill and luck. To improve your chances:\n" +
                            "1. Practice regularly\n" +
                            "2. Understand the game mechanics\n" +
                            "3. Stay updated on any new game features or changes", "User");

            System.out.println("All FAQs have been inserted successfully!");
        } else {
            System.out.println("FAQs already exist, skipping insert.");
        }
    }

    private void insertFAQ(String category, String question, String answer, String createdFor) {
        // Create a new FAQ object and insert it into the database
        FAQs faq = new FAQs(category, question, answer, createdFor);
        entityManager.persist(faq);
    }
}
