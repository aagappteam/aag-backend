package aagapp_backend.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ReferralValidationRequest {
    private String referralCode;
    private Integer roleId;

    // Getters and setters
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}

