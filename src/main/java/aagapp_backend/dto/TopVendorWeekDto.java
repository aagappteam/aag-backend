package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopVendorWeekDto {
    private Long vendorId;
    private String vendorName;
    private Long followerCount;
    private String primaryEmail;
    private String profilePicture;

    public TopVendorWeekDto(Long vendorId, String vendorName, Long followerCount, String primaryEmail, String profilePicture) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.followerCount = followerCount;
        this.primaryEmail = primaryEmail;
        this.profilePicture = profilePicture;
    }
}

