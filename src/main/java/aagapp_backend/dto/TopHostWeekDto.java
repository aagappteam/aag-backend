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
    private String user_name;

    public TopHostWeekDto(Long hostId, String hostName, Long publishCount, String email, String profilePic,String user_name) {
        this.vendorId = hostId;
        this.vendorName = hostName;
        this.publishCount = publishCount;
        this.primaryEmail = email;
        this.profilePicture = profilePic;
        this.user_name = user_name;
    }

    // Getters and setters
}
