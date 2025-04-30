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
    @Column(length = 100)
    private String category;

    @Column(length = 1000)
    private String question;

    @Column(length = 2000)  // Adjust this size based on your requirements
    private String answer;

    @Column(length = 8)
    private String createdFor;

    public FAQs(String category, String question, String answer, String createdFor) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.createdFor = createdFor;
    }
}

