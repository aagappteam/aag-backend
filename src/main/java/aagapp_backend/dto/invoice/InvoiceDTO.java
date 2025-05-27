package aagapp_backend.dto.invoice;

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
    private String month;
    private String transactionId;
    private BigDecimal approvedAmount;
}

