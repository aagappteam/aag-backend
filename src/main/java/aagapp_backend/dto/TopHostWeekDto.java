package aagapp_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TopHostWeekDto {
    private Long vendorId;
    private String vendorName;
    private Long publishCount;
    private String primaryEmail;
    private String profilePicture;

    public TopHostWeekDto(Long hostId, String hostName, Long publishCount, String email, String profilePic) {
        this.vendorId = hostId;
        this.vendorName = hostName;
        this.publishCount = publishCount;
        this.primaryEmail = email;
        this.profilePicture = profilePic;
    }

    // Getters and setters
}
