package aagapp_backend.dto;

import aagapp_backend.enums.SocialStatus;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialUserResponseDto {
    private Long id;
    private Map<String, String> socialMediaUrls;
    private SocialStatus status;
    private Date createdAt;
    private Date updatedAt;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerMobileNumber;
}
