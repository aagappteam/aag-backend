package aagapp_backend.dto;

import aagapp_backend.enums.KycStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KycVerificationRequest {

    private Long kycId;
    private KycStatus status;
}
