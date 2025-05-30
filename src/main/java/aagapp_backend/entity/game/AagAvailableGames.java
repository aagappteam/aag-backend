package aagapp_backend.entity.game;

import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "aag_available_games",
        indexes = {
                @Index(name = "idx_game_id", columnList = "id"),
                @Index(name = "idx_game_name", columnList = "gameName"),
                @Index(name = "idx_game_status", columnList = "gameStatus"),
                @Index(name = "idx_created_date", columnList = "created_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AagAvailableGames {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gameName;

    @Column(nullable = false)
    private String gameImage;

    // Cascading all operations on themes: persist, merge, remove, refresh, detach
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "game_themes",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    @JsonManagedReference
    private List<ThemeEntity> themes;




    // Cascading all operations on prices: persist, merge, remove, refresh, detach
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "game_prices",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "price_id")
    )
    @JsonManagedReference
    private List<PriceEntity> price;

    @Column(name = "minRange", nullable = false)
    private Integer minRange = 2;

    @Column(name = "maxRange", nullable = false)
    private Integer maxRange = 1024;

    @Column(nullable = false)
    private GameStatus gameStatus;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }
}
