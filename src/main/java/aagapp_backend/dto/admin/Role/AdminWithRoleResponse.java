package aagapp_backend.dto.admin.Role;

import aagapp_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminWithRoleResponse {
    private Long adminId;
    private String userName;
    private String email;
    private Integer roleId;
    private String mobileNumber;
    private String countryCode;
    private int active;
    private String createdBy;
    private Date createdAt;
    private String updatedAt;
    private String role;
}

