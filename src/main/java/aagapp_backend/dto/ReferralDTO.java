package aagapp_backend.dto;


import aagapp_backend.entity.VendorReferral;
import lombok.*;

@Data
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ReferralDTO {
    private Long referId; // ID of the referrer

    public ReferralDTO(Long id, Long referId, Long referredId) {
        this.referId = referId;
    }

}