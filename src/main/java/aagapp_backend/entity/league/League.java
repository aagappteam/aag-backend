package aagapp_backend.entity.league;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.LeagueStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "aag_league",
        indexes = {
                @Index(name = "idx_vendor_id_league", columnList = "vendor_id"),
                @Index(name = "idx_status_league", columnList = "status"),
                @Index(name = "idx_scheduled_at_league", columnList = "scheduled_at"),
                @Index(name = "idx_end_date_league", columnList = "end_date"),
                @Index(name = "idx_created_date_league", columnList = "created_date"),
                @Index(name = "idx_vendor_id_status_league", columnList = "vendor_id, status"),
                @Index(name = "idx_scheduled_at_status_league", columnList = "scheduled_at, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "entry_fee", nullable = false)
    private Double entryFee;

    @Column(name = "reward_amount", nullable = false)
    private Double rewardAmount;

    @Enumerated(EnumType.STRING)
    private LeagueStatus status;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;


    @Column(name = "scheduled_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime scheduledAt;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;
}
