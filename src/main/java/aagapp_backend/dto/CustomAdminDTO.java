package aagapp_backend.dto;

import aagapp_backend.entity.CustomAdmin;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CustomAdminDTO {
    private Long adminId;
    private int role;
    private String roleName;
    private String userName;
    private String mobileNumber;
    private String countryCode;
    private int active;
    private Date createdAt;

    // Constructor
    public CustomAdminDTO(CustomAdmin admin, String roleName) {
        this.adminId = admin.getAdminId();
        this.role = admin.getRole();
        this.roleName = roleName;
        this.userName = admin.getUser_name();
        this.mobileNumber = admin.getMobileNumber();
        this.countryCode = admin.getCountry_code();
        this.active = admin.getActive();
        this.createdAt = admin.getCreated_at();
    }

    // Getters and Setters (or use Lombok @Getter @Setter)
}

