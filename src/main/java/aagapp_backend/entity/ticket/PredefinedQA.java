package aagapp_backend.entity.ticket;

import aagapp_backend.enums.TicketUserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "predefined_qa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedQA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    private TicketUserType userType; // CUSTOMER or VENDOR
}
