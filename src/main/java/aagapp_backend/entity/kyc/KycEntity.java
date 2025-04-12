package aagapp_backend.entity.kyc;

import aagapp_backend.enums.KycStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_user_or_vendor_id", columnList = "userOrVendorId"),
                @Index(name = "idx_mobile_number", columnList = "mobileNumber"),
                @Index(name = "idx_aadhar_no", columnList = "aadharNo"),
                @Index(name = "idx_pan_no", columnList = "panNo")
        }
)
public class KycEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userOrVendorId;

    private String role;

    private String mobileNumber;

    private String mailId;

    private String aadharNo;

    private String panNo;

    private String aadharImage;

    private String panImage;

    private KycStatus isKycVerified;

}
