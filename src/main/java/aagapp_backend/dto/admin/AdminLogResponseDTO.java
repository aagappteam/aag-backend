
package aagapp_backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogResponseDTO {
    private Long id;
    private Long senderId;
    private String senderRole;
    private String name;
    private String profilePic;
    private String message;
    private String targetType;
    private Long targetId;
    private String targetRole;
    private Long receiverId;
    private String assignedRole;
    private Long assignedUserId;
    private Long assignedBy;
    private boolean read;
    private String performedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;
}
