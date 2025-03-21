package aagapp_backend.entity.game;

import aagapp_backend.enums.MoveType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "price_table_aag_game")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double priceValue;


/*    @ManyToMany(mappedBy = "price")
    @JsonBackReference
    private List<AagAvailableGames> games;  // Initialize the list here*/

    @ManyToMany(mappedBy = "price")
    @JsonBackReference
    private List<AagAvailableGames> games = new ArrayList<>();  // Ensure it's initialized


    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;
}
