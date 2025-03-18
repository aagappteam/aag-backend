package aagapp_backend.entity.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plans_table", indexes = {
        @Index(name = "idx_plan_id", columnList = "id"),
        @Index(name = "idx_plan_name", columnList = "plan_name"),
        @Index(name = "idx_plan_variant", columnList = "plan_variant"),
        @Index(name = "idx_plan_price", columnList = "price"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_updated_at", columnList = "updated_at")
})
@Getter
@Setter
public class PlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "plan_variant", nullable = false)
    private String planVariant; // Monthly, Yearly

    @Column(name = "followers_requirement", nullable = false)
    private String followersRequirement;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "subtitle", nullable = false)
    private String subtitle;

    @ElementCollection
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private List<String> features;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
