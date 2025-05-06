package aagapp_backend.entity.game;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.ZonedDateTime;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "aag_ludo_game",
        indexes = {
                @Index(name = "idx_vendor_id_aag_ludo_game", columnList = "vendor_id"),
                @Index(name = "idx_status_aag_ludo_game", columnList = "status"),
                @Index(name = "idx_scheduled_at_aag_ludo_game", columnList = "scheduled_at"),
                @Index(name = "idx_created_date_aag_ludo_game", columnList = "created_date"),
                @Index(name = "idx_vendor_id_status_aag_ludo_game", columnList = "vendor_id, status"),
                @Index(name = "idx_scheduled_at_status_aag_ludo_game", columnList = "scheduled_at, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fee and Move columns
    @Column(name = "fee", nullable = false)
    private Double fee;

    @Column(name = "move", nullable = false)
    private Integer move;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    private String shareableLink;

    @Column(name = "aaggameid", nullable = true)
    private Long aaggameid;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonBackReference
    private VendorEntity vendorEntity;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = true)
    private ThemeEntity theme;

    @Column(name = "scheduled_at", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime scheduledAt;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endDate;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    private Integer totalplayerscount;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;


    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;


    @PrePersist
    public void prePersist() {
        if (this.minPlayersPerTeam == null) {
            this.minPlayersPerTeam = 1; // Default value for minPlayersPerTeam
        }
        if (this.maxPlayersPerTeam == null) {
            this.maxPlayersPerTeam = 2; // Default value for maxPlayersPerTeam
        }
        if (this.endDate == null) {
            this.endDate = ZonedDateTime.now().plusHours(4);
        }
        if (this.move == null) {
            this.move = 12; // Default value for move if not provided
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }



}

