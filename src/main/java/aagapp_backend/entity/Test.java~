package aagapp_backend.entity;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "test")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nullable
    @Column(name = "country_code")
    private String countryCode;

    @Nullable
    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;
}
