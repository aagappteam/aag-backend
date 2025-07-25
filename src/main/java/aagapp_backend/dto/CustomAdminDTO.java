package aagapp_backend.dto;

import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class CustomAdminDTO {
    private Long adminId;
    private Set<Long> roleIds;          // For internal use
    private Set<String> roleNames;      // For structured UI filtering
    private String roleNamesString;     // For simple UI display
    private String userName;
    private String mobileNumber;
    private String countryCode;
    private int active;
    private Date createdAt;

    public CustomAdminDTO(CustomAdmin admin) {
        this.adminId = admin.getAdminId();
        this.userName = admin.getUser_name();
        this.mobileNumber = admin.getMobileNumber();
        this.countryCode = admin.getCountry_code();
        this.active = admin.getActive();
        this.createdAt = admin.getCreated_at();

        Set<Role> roles = admin.getRoles();
        this.roleIds = roles.stream()
                .map(role -> (long) role.getRoleId())
                .collect(Collectors.toSet());

        this.roleNames = roles.stream().map(Role::getRoleName).collect(Collectors.toSet());
        this.roleNamesString = String.join(", ", this.roleNames);
    }
}
