package aagapp_backend.entity.tournament;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.TournamentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Table(name = "tournament", indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_theme_id", columnList = "theme_id"),
        @Index(name = "idx_existinggame_id", columnList = "existinggameId"),
        @Index(name = "idx_participants", columnList = "participants"),
        @Index(name = "idx_entry_fee", columnList = "entryFee"),
        @Index(name = "idx_scheduled_at", columnList = "scheduledAt"),
        @Index(name = "idx_shareable_link", columnList = "shareableLink"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;
    private String name;

    private Double totalPrizePool;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = true)
    private ThemeEntity theme;

    private Long existinggameId;
    private String gameUrl;
    private int participants;

    private int currentJoinedPlayers=0;

    @NotNull
    private int entryFee;

    @Nullable
    private int move;

    private TournamentStatus status;
    private String shareableLink;

    @ManyToOne
    private VendorEntity vendorEntity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @CreationTimestamp
    @Column(updatable = false)
    private ZonedDateTime createdDate;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;


}
