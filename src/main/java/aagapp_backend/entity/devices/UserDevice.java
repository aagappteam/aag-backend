package aagapp_backend.entity.devices;

import aagapp_backend.entity.CustomCustomer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_device")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "customerId")
    private CustomCustomer user;

    @Column(name = "ipAddress")
    private String ipAddress;


    @Column(name = "userAgent")
    private String userAgent;

    @Column(name = "loginTime")
    private LocalDateTime loginTime;
}
