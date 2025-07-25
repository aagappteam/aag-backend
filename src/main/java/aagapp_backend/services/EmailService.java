package aagapp_backend.services;
import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOnboardingEmail(String to, String customerFirstName, String customerLastName) throws IOException {
        String template = loadTemplate("email-templates/vendor-onboarding-email.html");
        String messageBody = template
                .replace("{firstName}", customerFirstName)
                .replace("{lastName}", customerLastName);
        try {
            sendEmail(to, Constant.ONBOARDING_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending onboarding email: " + e.getMessage(), e);
        }
    }

    public void sendKycUploadEmail(String to, String name) throws IOException {
        String template = loadTemplate("email-templates/apply-kyc-verification.html");
        String messageBody = template
                .replace("{Name}", name);
        try {
            sendEmail(to, Constant.APPLYING_KYC_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending kyc upload email: " + e.getMessage(), e);
        }
    }

    public void sendKycVerifiedEmail(String to, String name) throws IOException {
        String template = loadTemplate("email-templates/KYC Verified -AAGVEER.html");
        String messageBody = template
                .replace("{Name}", name);
        try {
            sendEmail(to, Constant.KYC_APPROVED_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending kyc verification email: " + e.getMessage(), e);
        }
    }

    public void sendKycRejectedEmail(String to, String name) throws IOException {
        String template = loadTemplate("email-templates/KYC Rejected-AAGVEER.html");
        String messageBody = template
                .replace("{Name}", name);
        try {
            sendEmail(to, Constant.KYC_REJECTED_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending kyc rejection email: " + e.getMessage(), e);
        }
    }

    public void sendPlanPurchasedEmail(String to, String name, LocalDateTime date, String plan, Double amount) throws IOException {
        String template = loadTemplate("email-templates/Subscription Plan Purchased.html");
//    String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        LocalDateTime purchaseDate = LocalDateTime.now(); // Now
        LocalDateTime renewalDate = purchaseDate.plusMonths(1); // 1 month later
        String formattedDate = renewalDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String messageBody = template
                .replace("{Name}", name)
                .replace("{Date}", formattedDate)
                .replace("{Plan}", plan)
                .replace("{Amount}", amount.toString());
        try {
            sendEmail(to, Constant.PLAN_PURCHASED_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending plan purchase email: " + e.getMessage(), e);
        }
    }
    public void sendPlanRenewEmail(String to, String name, LocalDateTime date, String plan, Double amount) throws IOException {
        String template = loadTemplate("email-templates/Subscription Plan Renewed.html");
//        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        LocalDateTime purchaseDate = LocalDateTime.now(); // Now
        LocalDateTime renewalDate = purchaseDate.plusMonths(1); // 1 month later

        String formattedDate = renewalDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String messageBody = template
                .replace("{Name}", name)
                .replace("{Date}", formattedDate)
                .replace("{Plan}", plan)
                .replace("{Amount}", amount.toString());
        try {
            sendEmail(to, Constant.PLAN_PURCHASED_AGAIN_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending plan purchase email: " + e.getMessage(), e);
        }
    }

    public void sendProfileVerificationEmail(
            VendorEntity vendorEntity,
            String generatedPassword
    ) throws IOException {

        // Load HTML template
        String template = loadTemplate("email-templates/vendora-approve-mail.html");

        String firstName = vendorEntity.getFirst_name();
        String mobileNumber = vendorEntity.getMobileNumber();  // or from vendorSubmissionEntity if applicable
        String to = vendorEntity.getPrimary_email();
        // Replace placeholders
        String messageBody = template
                .replace("{firstName}", firstName)
                .replace("{mobileNumber}", mobileNumber)
                .replace("{password}", generatedPassword);

        try {
            // Send email
            sendEmail(to, Constant.APPROVED_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending profile verification email: " + e.getMessage(), e);
        }
    }

    public void sendProfileRejectionEmail(
            VendorEntity vendorEntity
    ) throws IOException {

        // Load HTML template
        String template = loadTemplate("email-templates/vendor-rejection-mail.html");

        String firstName = vendorEntity.getFirst_name();
        String to = vendorEntity.getPrimary_email();

        // Replace placeholders
        String messageBody = template
                .replace("{firstName}", firstName  );


        try {
            // Send email
            sendEmail(to, Constant.REJCTED_EMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending profile verification email: " + e.getMessage(), e);
        }
    }



    public void sendEmail(String to, String subject, String body, boolean isHtml) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

        try {
            helper.setFrom(fromEmail, "AAG App");
            helper.setTo(to);
            helper.setSubject(subject);

            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }

            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            throw new MessagingException("Error while sending email: " + e.getMessage(), e);
        }
    }




    public String loadTemplate(String templateName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateName)) {
            if (inputStream == null) {
                throw new IOException("Template file not found: " + templateName);
            }
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
            return scanner.useDelimiter("\\A").next();
        }
    }

    public void sendErrorEmail(String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

            helper.setFrom(fromEmail, "AAG App");
            helper.setTo(new String[] {
                    "anilkant.mishra@celestialitverse.com",
                    "juned.idreesh@celestialitverse.com"
            });
            helper.setSubject(subject);

            message.setText(body, "utf-8");

            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            System.err.println("Failed to send error email: " + e.getMessage());
            e.printStackTrace();
        }
    }
//    send expiry mail
    public void sendSubscriptionExpiredMail(String to, String name, LocalDateTime date, String plan, Double amount) throws IOException {
        // Load HTML template
        String template = loadTemplate("email-templates/SubscriptionPlanExpired.html");

        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));



        String messageBody = template
                .replace("{Name}", name)
                .replace("{Date}", formattedDate)
                .replace("{Plan}", plan)
                .replace("{Amount}", amount.toString());


        try {
            // Send email
            sendEmail(to, Constant.PLAN_EXPIREDEMAIL_SUBJECT, messageBody,true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending profile verification email: " + e.getMessage(), e);
        }
    }

//    Send Email  to admin if new tournament or league is published
    public void sendEmailLeague(CustomAdmin admin, String type, String name, Double fee, Long id, ZonedDateTime createdAt, String gameIcon,String vendorname)
            throws IOException, MessagingException {

        String template = loadTemplate("email-templates/league-tournament.html");

        ZonedDateTime indiaTime = createdAt.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        String formattedDate = indiaTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a z"));

        String messageBody = template
                .replace("{type}", type)
                .replace("{vendorname}", vendorname)
                .replace("{adminName}", admin.getUser_name())
                .replace("{name}", name)
                .replace("{Fee}", String.format("%.2f", fee))
                .replace("{id}", String.valueOf(id))
                .replace("{createdAt}", formattedDate)
                .replace("{gameIcon}", gameIcon != null ? gameIcon : "https://yourdomain.com/default-icon.png"); // fallback if needed

        sendEmail(admin.getEmail(), "New " + type + " Published", messageBody, true);
    }





    public void sendLeagueEmail(
            VendorEntity vendorEntity, String title, String body
    ) throws IOException {

        // Load HTML template (same for approval/rejection, use placeholders inside)
        String template = loadTemplate("email-templates/vendor-league-status.html");

        String firstName = vendorEntity.getFirst_name();
        String to = vendorEntity.getPrimary_email();

        // Replace placeholders with dynamic content
        String messageBody = template
                .replace("{firstName}", firstName)
                .replace("{title}", title)
                .replace("{body}", body);

        try {
            sendEmail(to, title, messageBody, true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending league status email: " + e.getMessage(), e);
        }
    }



    public void sendTournamentEmail(
            VendorEntity vendorEntity, String title, String body
    ) throws IOException {

        // Load HTML template (same for approval/rejection, use placeholders inside)
        String template = loadTemplate("email-templates/vendor-tournament-status.html");

        String firstName = vendorEntity.getFirst_name();
        String to = vendorEntity.getPrimary_email();

        // Replace placeholders with dynamic content
        String messageBody = template
                .replace("{firstName}", firstName)
                .replace("{title}", title)
                .replace("{body}", body);

        try {
            sendEmail(to, title, messageBody, true);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending tournament status email: " + e.getMessage(), e);
        }
    }


    public void sendAdminCommonmail(CustomAdmin admin, String type, String name,String message)
            throws IOException, MessagingException {

        String template = loadTemplate("email-templates/admin-mail.html");

        ZonedDateTime indiaTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formattedDate = indiaTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a z"));

        String messageBody = template
                .replace("{type}", type)
                .replace("{adminName}", admin.getUser_name())
                .replace("{name}", name)
                .replace("{message}",message)
                .replace("{createdAt}", formattedDate);

        sendEmail(admin.getEmail(), "New " + type + " has come for approval", messageBody, true);
    }

/*    public void sendAdminWithdrawlapprove(CustomAdmin admin, String Amount, String name,String message)
            throws IOException, MessagingException {

        String template = loadTemplate("email-templates/Withdrawal-Successful–Funds-Transferred.html");

        ZonedDateTime indiaTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formattedDate = indiaTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a z"));

        String messageBody = template
                .replace("{ammount}", Amount)
                .replace("{VendorName}", admin.getUser_name())
                .replace("{bank}", bank)
                .replace("{createdAt}", formattedDate);

        sendEmail(admin.getEmail(), "New " + type + " has come for approval", messageBody, true);
    }*/





}
