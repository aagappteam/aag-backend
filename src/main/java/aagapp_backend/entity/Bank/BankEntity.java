package aagapp_backend.entity.Bank;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankEntity {

    @Id
    private Long bankId;

    @Column(nullable = false)
    private String bankName;

    private String ifscCode;
}

