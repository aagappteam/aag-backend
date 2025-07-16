package aagapp_backend.dto;

import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.enums.WithdrawalType;
import lombok.*;
import org.checkerframework.checker.units.qual.N;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestResponseDTO {

    private Long id;
    private BigDecimal amount;
    private String upiId;
    private WithdrawalStatus status;
    private LocalDateTime requestDate;
    private String adminComment;
    private WithdrawalType withdrawalType;
    private BigDecimal processingFee;
    private BigDecimal finalPayoutAmount;
    private LocalDateTime updatedAt;

    private String accountNumber;

    private String accountHolderName;

    private String bankName;

    private String ifscCode;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerMobileNumber;

    public WithdrawalRequestResponseDTO(CustomerWithdrawalRequest entity) {
        this.id = entity.getId();
        this.amount = entity.getAmount();
        this.upiId = entity.getUpiId();
        this.status = entity.getStatus();
        this.requestDate = entity.getRequestDate();
        this.adminComment = entity.getAdminComment();
        this.withdrawalType = entity.getWithdrawalType();
//        this.processingFee = entity.getProcessingFee();
//        this.finalPayoutAmount = entity.getFinalPayoutAmount();
        this.updatedAt = entity.getUpdatedAt();
        this.accountNumber = entity.getAccountNumber();
        this.accountHolderName = entity.getAccountHolderName();
        this.bankName = entity.getBankName();
        this.ifscCode = entity.getIfscCode();

        if (entity.getCustomer() != null) {
            this.customerId = entity.getCustomer().getId();
            this.customerName = entity.getCustomer().getName();
            this.customerEmail = entity.getCustomer().getEmail();
            this.customerMobileNumber = entity.getCustomer().getMobileNumber();
        }
    }

    // Getters and Setters (or use @Getter/@Setter/@Data from Lombok if preferred)
}