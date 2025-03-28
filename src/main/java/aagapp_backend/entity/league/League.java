package aagapp_backend.entity.league;

import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.LeagueStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

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
    private Double fee;

    private Integer move;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private VendorEntity vendorEntity;

    private String shareableLink;

    @Column(nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    private LeagueStatus status = LeagueStatus.SCHEDULED;

    @Column(name = "league_type", nullable = true)
    private String leagueType;

    @Column(name = "scheduled_at", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime scheduledAt;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = true)
    private ThemeEntity theme;

    @ManyToMany
    @JoinTable(
            name = "league_challenges",
            joinColumns = @JoinColumn(name = "league_id"),
            inverseJoinColumns = @JoinColumn(name = "vendor_id")
    )
    private List<VendorEntity> challengedVendors;

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

  /*  private ZonedDateTime challengeTimestamp;
*/
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }

    @PrePersist
    public void prePersist() {
/*
        this.challengeTimestamp=ZonedDateTime.now();
*/
        if (this.minPlayersPerTeam == null) {
            this.minPlayersPerTeam = 1; // Default value for minPlayersPerTeam
        }
        if (this.maxPlayersPerTeam == null) {
            this.maxPlayersPerTeam = 2; // Default value for maxPlayersPerTeam
        }
        if (this.endDate == null) {
            this.endDate = ZonedDateTime.now();
        }

        if (this.move == null) {
            this.move = 12; // Default value for move if not provided
        }

    }

}
