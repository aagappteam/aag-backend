package aagapp_backend.entity.faqs;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_question", columnList = "question"),
                @Index(name = "idx_createdFor", columnList = "createdFor")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FAQs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Category cannot be null")
    private String category;
    private String question;
    private String answer;
    private String createdFor;
}
