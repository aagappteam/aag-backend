package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class WithdrawalRequestSubmitDto {

    private Long influencerId;
    private BigDecimal amount;
    private String reason; // Optional
}
