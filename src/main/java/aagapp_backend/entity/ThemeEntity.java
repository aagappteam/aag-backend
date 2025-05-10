package aagapp_backend.entity;
import aagapp_backend.entity.game.AagAvailableGames;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        name = "themes",
        indexes = {
                @Index(name = "idx_theme_name", columnList = "name"),
                @Index(name = "idx_theme_created_date", columnList = "created_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "game_image_url")
    private String gameimageUrl;

    @ManyToMany(mappedBy = "themes")
    @JsonBackReference
    private List<AagAvailableGames> games;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;


    public ThemeEntity(String aDefault, String url, LocalDateTime currentTimestamp) {
        this.name = aDefault;
        this.imageUrl = url;
        this.createdDate = Date.from(currentTimestamp.atZone(ZoneId.of("Asia/Kolkata")).toInstant());
        this.games = new ArrayList<>();
    }
}

