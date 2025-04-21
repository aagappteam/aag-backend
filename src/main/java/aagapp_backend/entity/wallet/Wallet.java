package aagapp_backend.entity.wallet;

import aagapp_backend.entity.CustomCustomer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallet",
        indexes = {
                @Index(name = "idx_customer_id_wallet", columnList = "customerId"),
                @Index(name = "idx_unplayed_balance", columnList = "unplayed_balance"),
                @Index(name = "idx_winning_amount_wallet", columnList = "winning_amount"),
                @Index(name = "idx_created_at_wallet", columnList = "created_at"),
                @Index(name = "idx_updated_at_wallet", columnList = "updated_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @ManyToOne
    @JoinColumn(name = "customerId", referencedColumnName = "customerId", nullable = false)
    @JsonBackReference
    private CustomCustomer customCustomer;


    @Column(name = "unplayed_balance", nullable = false)
    private Double unplayedBalance;

    @Column(name = "winning_amount", nullable = false)
    private BigDecimal winningAmount;

    @Column(name = "is_test", nullable = false)
    private Boolean isTest = false;

    @Column(name = "created_at", updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
