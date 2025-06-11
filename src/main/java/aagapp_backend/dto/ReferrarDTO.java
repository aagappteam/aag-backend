package aagapp_backend.dto;

public class ReferrarDTO {
    private Long referrerId;

    public ReferrarDTO(Long referrerId) {
        this.referrerId = referrerId;
    }

    public Long getReferrerId() {
        return referrerId;
    }

    public void setReferrerId(Long referrerId) {
        this.referrerId = referrerId;
    }
}

