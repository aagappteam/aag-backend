package aagapp_backend.entity.social;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_vendor_follow")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserVendorFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "customerId")
    private CustomCustomer user;


    @ManyToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "service_provider_id")
    private VendorEntity vendor;

    private LocalDateTime followedAt;
}
