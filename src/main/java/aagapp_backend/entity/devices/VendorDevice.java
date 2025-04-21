package aagapp_backend.entity.devices;


import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_device")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class VendorDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendorId", referencedColumnName = "service_provider_id")
    private VendorEntity vendor;

    @Column(name = "vendorId", insertable = false, updatable = false)
    private Long vendorId;

    @Column(name = "ipAddress")
    private String ipAddress;




    @Column(name = "userAgent")
    private String userAgent;

    @Column(name = "loginTime")
    private LocalDateTime loginTime;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

}

