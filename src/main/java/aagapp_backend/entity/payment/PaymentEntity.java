package aagapp_backend.entity.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_vendor_payment", columnList = "vendor_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_plan", columnList = "plan_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private VendorEntity vendorEntity;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "plan_name", nullable = false)
    private String planName; // e.g., Basic, Premium, Pro

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit; // Number of games allowed daily

    @Column(name = "weekly_limit", nullable = false)
    private Integer weeklyLimit;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, SUCCESSFUL, FAILED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiry_at", nullable = false)
    private LocalDateTime expiryAt;
}
