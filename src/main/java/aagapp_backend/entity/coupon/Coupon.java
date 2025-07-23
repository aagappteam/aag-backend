package aagapp_backend.entity.coupon;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private float bonusAmount;

    private float minimumRecharge;

    private boolean active = true;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
}

