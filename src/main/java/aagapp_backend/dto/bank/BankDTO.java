package aagapp_backend.dto.bank;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDTO {

    private Long bankId;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    private String ifscCode;
}

