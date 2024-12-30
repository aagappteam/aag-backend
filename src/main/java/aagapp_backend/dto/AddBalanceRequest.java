package aagapp_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddBalanceRequest {
    @NotNull(message = "Customer ID must not be null")
    @Positive(message = "Customer ID must be a positive number")
    private Long customerId;

    @NotNull
    @Positive(message = "Amount must be a positive number")
    private float amount;

    @NotNull(message = "Is True must not be null")
    private Boolean isTest;
}
