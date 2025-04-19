package aagapp_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(
        name = "custom_admin",
        indexes = {
                @Index(name = "idx_user_name", columnList = "user_name"),
//                @Index(name = "idx_mobile_number", columnList = "mobileNumber"),
                @Index(name = "idx_country_code", columnList = "country_code"),
//                @Index(name = "idx_created_at", columnList = "created_at"),
//                @Index(name = "idx_updated_at", columnList = "updated_at")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class CustomAdmin
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long admin_id;

    private int role;
    private String password;
    private String user_name;
    private String otp;
    @Size(min = 9, max = 13)
    private String mobileNumber;
    private String country_code;

    @Nullable
    @Column(length = 512)
    private String token;
    private int active=0;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created_at;

    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updated_at;

   /* public CustomAdmin(Long admin_id, int role, String password, String user_name, String mobileNumber, String country_code, int active, Date created_at, String created_by) {
        this.admin_id = admin_id;
        this.role = role;
        this.password = password;
        this.user_name=user_name;
        this.mobileNumber = mobileNumber;
        this.country_code = country_code;
        this.active = active;
        this.created_at = created_at;
        this.updated_at = updated_at;

    }*/
}
