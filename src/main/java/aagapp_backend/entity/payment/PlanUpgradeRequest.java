package aagapp_backend.entity.payment;

import aagapp_backend.enums.RequestStatus;
import com.twilio.rest.numbers.v2.BulkHostedNumberOrder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_upgrade_request")
@Getter
@Setter
public class PlanUpgradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;

    private String name;
    private String email;
    private Long requestedPlanId;
    private String requestedPlanName;

    @Enumerated(EnumType.STRING)
    private RequestStatus status; // PENDING, APPROVED, REJECTED

    private LocalDateTime requestDate;

    private LocalDateTime approvedDate;

    private String adminRemarks;

}

