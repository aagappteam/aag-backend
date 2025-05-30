package aagapp_backend.entity.wallet;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
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
        name = "vendor_wallet",
        indexes = {
                @Index(name = "idx_service_provider_id", columnList = "service_provider_id"),
                @Index(name = "idx_winning_amount", columnList = "winning_amount"),
                @Index(name = "idx_created_at_vendor_wallet", columnList = "created_at"),
                @Index(name = "idx_updated_at_vendor_wallet", columnList = "updated_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class VendorWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @OneToOne
    @JoinColumn(name = "service_provider_id", referencedColumnName = "service_provider_id", nullable = false)
    @JsonBackReference
    private VendorEntity vendorEntity;

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
