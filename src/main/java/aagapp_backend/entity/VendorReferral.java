package aagapp_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(
        name = "vendor_referral_table",
        indexes = {
                @Index(name = "idx_vendor_referrer_id", columnList = "vendor_id"),
                @Index(name = "idx_vendor_referred_id", columnList = "referred_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VendorReferral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    @JsonIgnoreProperties("givenReferral")
    private VendorEntity referrerId;

    @ManyToOne
    @JoinColumn(name = "referred_id")
    @JsonIgnoreProperties("receivedReferral")
    private VendorEntity referredId;
}