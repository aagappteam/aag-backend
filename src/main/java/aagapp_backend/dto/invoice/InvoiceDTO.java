package aagapp_backend.dto.invoice;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceDTO {
    private String invoiceId;
    private LocalDate date;
    private String influencerName;
    private Long influencerId;
    @NotNull(message = "State can not be null")
    private String state;
    @NotNull(message = "User Type can not be null")
    private String userType;
    private String month;
    private String transactionId;
    private BigDecimal approvedAmount;
}

