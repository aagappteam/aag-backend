package aagapp_backend.dto;

import aagapp_backend.enums.WithdrawalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerWithdrawalRequestDto {
    @NotNull(message = "Customer ID must not be null")
    @Positive(message = "Customer ID must be a positive number")
    private Long customerId;

    @NotNull
    @Positive(message = "Amount must be a positive number")
    private Float amount;


    private String upiId;

    private String accountNumber;

    private String accountHolderName;

    private String bankName;

    private String ifscCode;

    @NotNull(message = "Withdrawal Type must not be null")
    private WithdrawalType withdrawalType;
}
