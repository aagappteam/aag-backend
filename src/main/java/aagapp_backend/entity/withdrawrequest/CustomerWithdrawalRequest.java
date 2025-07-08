package aagapp_backend.entity.withdrawrequest;


import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.enums.WithdrawalType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_withdrawal_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerWithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomCustomer customer;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "upi_id", nullable = false)
    private String upiId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(name = "admin_comment")
    private String adminComment;

    @Column(name = "withdrawal_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WithdrawalType withdrawalType;

    @Column(name = "processing_fee", nullable = false)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "final_payout_amount", nullable = false)
    private BigDecimal finalPayoutAmount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

}
