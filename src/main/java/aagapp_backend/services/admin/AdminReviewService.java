package aagapp_backend.services.admin;

import aagapp_backend.components.PasswordGenerator;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.enums.ProfileStatus;
import aagapp_backend.repository.admin.VendorSubmissionRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdminReviewService {

    @Autowired
    private VendorSubmissionRepository submissionRepository;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private EntityManager entitymanager;

    @Autowired
    private VenderService venderService;


    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Object reviewSubmission(Long id, boolean isApproved) {
        try {
            Optional<VendorSubmissionEntity> submission = submissionRepository.findById(id);

            if (submission.isPresent()) {
                VendorSubmissionEntity vendorSubmissionEntity = submission.get();

                if (vendorSubmissionEntity.getApproved()) {
                    return new SubmissionResponse("The submission has already been approved.", vendorSubmissionEntity);
                } else if (vendorSubmissionEntity.getProfileStatus() == ProfileStatus.REJECTED) {
                    return new SubmissionResponse("The submission has already been rejected.", vendorSubmissionEntity);
                }

                VendorEntity vendorEntity = vendorSubmissionEntity.getVendorEntity();


                if (isApproved) {
                    String generatedPassword = PasswordGenerator.generatePassword(8);
                    vendorEntity.setPassword(passwordEncoder.encode(generatedPassword));
                  /*  vendorEntity.setPrimary_email(vendorSubmissionEntity.getEmail());
                    vendorEntity.setFirst_name(vendorSubmissionEntity.getFirstName());
                    vendorEntity.setLast_name(vendorSubmissionEntity.getLastName());*/
                    vendorEntity.setIsVerified(1);

                    entitymanager.merge(vendorEntity);
                    sendApprovalEmail(vendorEntity, vendorSubmissionEntity, generatedPassword);
                    vendorSubmissionEntity.setProfileStatus(ProfileStatus.ACTIVE);

                } else {
/*                    vendorEntity.setPrimary_email(vendorSubmissionEntity.getEmail());
                    vendorEntity.setFirst_name(vendorSubmissionEntity.getFirstName());
                    vendorEntity.setLast_name(vendorSubmissionEntity.getLastName());*/
                    vendorEntity.setIsVerified(0);

                    entitymanager.merge(vendorEntity);
                    sendRejectionEmail(vendorEntity, vendorSubmissionEntity);
                    vendorSubmissionEntity.setProfileStatus(ProfileStatus.REJECTED);
                }

                vendorSubmissionEntity.setApproved(isApproved);
                entitymanager.merge(vendorSubmissionEntity);


                return new SubmissionResponse(isApproved ? "The submission has been successfully approved." : "The submission has been successfully rejected.", vendorSubmissionEntity);
            }

            return new SubmissionResponse("Submission with ID " + id + " not found.", null); // Submission not found
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return new SubmissionResponse("An error occurred while processing the review request.", null);
        }
    }


    private void sendApprovalEmail(VendorEntity vendorEntity, VendorSubmissionEntity vendorSubmissionEntity, String generatedPassword) throws MessagingException {
        // Create the email message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

        try {
            // Set the "From" field with a name
            helper.setFrom("aagappteam@gmail.com", "AAG App Team");

            // Set the recipient, subject, and body of the email
            helper.setTo(vendorSubmissionEntity.getEmail());
            helper.setSubject("Your Vendor Account has been Approved");

            // Body text for the approval email
            String emailBody = "Dear " + vendorEntity.getFirst_name() + " " + vendorEntity.getLast_name() + ",\n\n" +
                    "We are pleased to inform you that your vendor account has been approved.\n\n" +
                    "You can now log in to your account using the following credentials:\n\n" +
                    "Username: " + vendorEntity.getMobileNumber() + "\n" +
                    "Password: " + generatedPassword + "\n\n" +
                    "If you have any questions or need assistance, please don't hesitate to contact us.\n\n" +
                    "Best regards,\n" +
                    "AAG App Team\n\n" +
                    "Please keep this information secure and do not share your password with anyone.";

            helper.setText(emailBody);

            // Send the email
            mailSender.send(message);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
        }
    }



    private void sendRejectionEmail(VendorEntity vendorEntity, VendorSubmissionEntity vendorSubmissionEntity) throws MessagingException {
        // Create the email message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

        try {
            // Set the "From" field with a name
            helper.setFrom("aagappteam@gmail.com", "AAG App Team");

            // Set the recipient, subject, and body of the email
            helper.setTo(vendorSubmissionEntity.getEmail());
            helper.setSubject("Your Vendor Account Submission Status");

            // Body text for the rejection email
            String emailBody = "Dear " + vendorEntity.getFirst_name() + " " + vendorEntity.getLast_name() + ",\n\n" +
                    "We regret to inform you that your vendor account submission has been rejected. After reviewing your application, it did not meet the necessary criteria at this time.\n\n" +
                    "If you would like to receive more details or discuss the outcome of your submission, please feel free to reach out to us. We would be happy to assist you.\n\n" +
                    "Thank you for your time and effort, and we wish you the best in your future endeavors.\n\n" +
                    "Best regards,\n" +
                    "AAG App Team\n\n" +
                    "If you have any further questions or would like more information, please don't hesitate to contact us.";

            helper.setText(emailBody);

            // Send the email
            mailSender.send(message);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
        }
    }


    @Getter
    @Setter
    public static class SubmissionResponse {
        private String message;
        private VendorSubmissionEntity data;

        public SubmissionResponse(String message, VendorSubmissionEntity data) {
            this.message = message;
            this.data = data;
        }

    }
}
