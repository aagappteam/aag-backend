package aagapp_backend.entity.tournament;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.util.Date;

@Entity
@Table(name = "tournament", indexes = {
        @Index(name = "idx_name", columnList = "name"),

        @Index(name = "idx_participants_tournament", columnList = "participants"),
        @Index(name = "idx_entryFee_tournament", columnList = "entryFee"),
        @Index(name = "idx_status_tournament", columnList = "status"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int participants;
    private int entryFee;
    private double totalPrizePool;
    private String status; // Ongoing, Completed
}
