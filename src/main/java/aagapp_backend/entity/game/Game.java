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

@Entity
@Table(
        name = "aag_game",
        indexes = {
                @Index(name = "idx_vendor_id", columnList = "vendor_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
                @Index(name = "idx_created_date", columnList = "created_date")
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

    @Column(nullable = false)
    private String description;

    @Column(name = "entry_fee", nullable = false)
    private Double entryFee;

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

    @Column(name = "scheduled_at")
    private ZonedDateTime scheduledAt;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;
}
