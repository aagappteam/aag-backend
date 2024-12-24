package aagapp_backend.entity.game;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.GameStatus;
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

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "aag_game",
        indexes = {
                @Index(name = "idx_vendor_id", columnList = "vendor_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
                @Index(name = "idx_created_date", columnList = "created_date"),
                @Index(name = "idx_vendor_id_status", columnList = "vendor_id, status"),
                @Index(name = "idx_scheduled_at_status", columnList = "scheduled_at, status")
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

    @Column(nullable = false)
    private String name;


    @ElementCollection
    @CollectionTable(name = "game_fee_to_moves", joinColumns = @JoinColumn(name = "game_id"))
    private List<FeeToMove> feeToMoves;

    private Integer moves;


    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private String shareableLink;

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
            this.endDate = ZonedDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }

    public void calculateMoves(Double selectedFee) {
        if (feeToMoves != null && selectedFee != null) {
            FeeToMove feeToMove = feeToMoves
                    .stream()
                    .filter(mapping -> mapping.getRupees().equals(selectedFee))
                    .findFirst()
                    .orElse(null);

            if (feeToMove != null) {
                this.moves = feeToMove.getMoves();
            } else {
                this.moves = 0;
            }
        } else {
            this.moves = 0;
        }
    }
}

