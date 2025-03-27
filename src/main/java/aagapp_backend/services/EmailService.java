package aagapp_backend.services;
import aagapp_backend.components.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
        String template = loadTemplate("email-templates/vendor-onboarding-email.txt");
        System.out.println("sendOnboardingEmail" + to + " " + customerFirstName + " " + customerLastName);
        String messageBody = template
                .replace("{firstName}", customerFirstName)
                .replace("{lastName}", customerLastName);

        try {
            sendEmail(to, "Onboarding Email", messageBody);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending onboarding email: " + e.getMessage(), e);
        }
    }
    private void sendEmail(String to, String subject, String body) throws MessagingException {
        // Create the MimeMessage for the email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

        try {
            // Set the "From" address (your company or no-reply email)
            helper.setFrom(fromEmail, "AAG App Team");

            // Set the recipient email address
            helper.setTo(to);

            // Set the subject of the email
            helper.setSubject(subject);

            // Set the email body
            helper.setText(body);

            // Send the email
            mailSender.send(message);
        } catch (MessagingException | MailException  | UnsupportedEncodingException e) {
            throw new MessagingException("Error while sending email: " + e.getMessage(), e);
        }
    }

    private String loadTemplate(String templateName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateName)) {
            if (inputStream == null) {
                throw new IOException("Template file not found: " + templateName);
            }
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
            return scanner.useDelimiter("\\A").next();
        }
    }
}
