package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceDTO {

    private Long walletId;
    private Double unplayedBalance;
    private BigDecimal winningAmount;
    private BigDecimal bonusAmount;
    private Boolean isTest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
