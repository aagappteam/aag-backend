package aagapp_backend.dto;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Long id;
    private String transactionId;
    private Double amount;
    private String planName;
    private Integer dailyLimit;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiryAt;
    private String planDuration;
    private String paymentType;
    private Boolean isTest;


    public PaymentDTO(PaymentEntity payment) {
        this.id = payment.getId();
        this.transactionId = payment.getTransactionId();
        this.amount = payment.getAmount();
        this.planName = payment.getPlanName();
        this.dailyLimit = payment.getDailyLimit();
        this.status = payment.getStatus();
        this.createdAt = payment.getCreatedAt();
        this.expiryAt = payment.getExpiryAt();
        this.planDuration = payment.getPlanDuration();
        this.paymentType = payment.getPaymentType();
        this.isTest = payment.getIsTest();
    }
}
