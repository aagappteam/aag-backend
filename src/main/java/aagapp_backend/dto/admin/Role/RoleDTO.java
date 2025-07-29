package aagapp_backend.dto.admin.Role;


public class RoleDTO {
    private Integer roleId;
    private String roleName;

    public RoleDTO(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // Getters and setters (or use Lombok if preferred)
    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }
}

