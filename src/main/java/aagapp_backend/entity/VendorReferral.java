package aagapp_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "vendor_referral_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VendorReferral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    @JsonBackReference("vendor-referrer")
    private VendorEntity referrerId;

    @ManyToOne
    @JoinColumn(name = "referred_id")
    @JsonBackReference("vendor-referred")
    private VendorEntity referredId;


}
