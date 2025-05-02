package aagapp_backend.entity.league;

import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.team.LeagueTeam;
import aagapp_backend.enums.ChallengeStatus;
import aagapp_backend.enums.LeagueStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
                @Index(name = "idx_scheduled_at_status_league", columnList = "scheduled_at, status"),
                @Index(name = "idx_opponent_vendor_id", columnList = "opponent_vendor_id"),
                @Index(name = "idx_updated_date_league", columnList = "updated_date")
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

    private String name;

    private String gameName;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LeagueTeam> teams;


    @Column(name = "challenging_vendor_id")
    private Long challengingVendorId;

    @Column(name = "challenging_vendor_name")
    private String challengingVendorName;

    @Column(name = "challenging_vendor_profile_pic")
    private String challengingVendorProfilePic;

    @Column(name = "fee", nullable = false)
    private Double fee;

    @Column(name = "move", nullable = false)
    private Integer move;

    @Enumerated(EnumType.STRING)
    private LeagueStatus status;

    private String leagueUrl;

    @Column(name = "aaggameid", nullable = true)
    private Long aagGameId;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    private VendorEntity vendorEntity;


    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = true)
    private ThemeEntity theme;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    private String shareableLink;

    @Column(name = "scheduled_at", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime scheduledAt;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endDate;

    @Column(name = "opponent_vendor_id")
    private Long opponentVendorId;

    @Column(name = "opponent_vendor_profile_pic")
    private String opponentVendorProfilePic;

    @Column(name = "opponent_vendor_name")
    private String opponentVendorName;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

//    private Integer totalplayerscount;


    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }

    @PrePersist
    public void prePersist() {
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
