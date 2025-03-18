package aagapp_backend.entity.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_vendor_payment", columnList = "service_provider_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_created_at", columnList = "created_at"),
        @Index(name = "idx_payment_expiry_at", columnList = "expiry_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_provider_id", nullable = false)
    @JsonBackReference
    private VendorEntity vendorEntity;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @NotNull(message = "Amount is required.")
    @PositiveOrZero(message = "Amount must be zero or positive.")
    @Column(name = "amount", nullable = false)
    private Double amount;


    @Column(name = "plan_id", nullable = false)  // Ensure this column name matches the one in your database
    private Long planId;

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit = 5;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;

    // Added to define the plan duration: "Monthly", "Yearly", etc.
    @Column(name = "plan_duration")
    private String planDuration;  // "Monthly", "Yearly", etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "is_test", nullable = false)
    private Boolean isTest = false;

/*    @ManyToOne
    @JoinColumn(name = "plan_id", referencedColumnName = "id")
    @JsonBackReference
    private PlanEntity plan;*/

    @Column(name = "gateway_response")
    private String gatewayResponse;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

}
