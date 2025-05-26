package aagapp_backend.entity.withdrawrequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_withdrawal_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long influencerId;
    private BigDecimal amount;
    private String monthYear;
    private String status; // PENDING, APPROVED, REJECTED
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime requestedAt;
}
