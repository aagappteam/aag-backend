package aagapp_backend.entity.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apiPath;
    private String method;
    private String privilegeName;

/*    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privilege_id")
    private Privilege privilege;  // Instead of storing name manually*/
}

