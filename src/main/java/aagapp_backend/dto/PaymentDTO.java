package aagapp_backend.dto;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.enums.PaymentType;
import jakarta.persistence.Column;
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
    private String transactionType;
    private LocalDateTime createdAt;
    private LocalDateTime expiryAt;
    private String planDuration;
    private PaymentType paymentType;
    private String fromUser;
    private String toUser;
    private String downloadInvoice;
    private Boolean isTest;


    public PaymentDTO(PaymentEntity payment, PlanEntity planEntity) {
        this.id = payment.getId();
        this.transactionId = payment.getTransactionId();
        this.amount = payment.getAmount();
        this.planName = planEntity != null ? planEntity.getPlanName() : null;
        this.dailyLimit = payment.getDailyLimit();
        this.status = payment.getStatus();
        this.createdAt = payment.getCreatedAt();
        this.expiryAt = payment.getExpiryAt();
        this.planDuration = payment.getPlanDuration();
        this.paymentType = payment.getPaymentType();
        this.fromUser = payment.getFromUser();
        this.toUser = payment.getToUser();
        this.downloadInvoice = payment.getDownloadInvoice();
        this.isTest = payment.getIsTest();
    }
}
