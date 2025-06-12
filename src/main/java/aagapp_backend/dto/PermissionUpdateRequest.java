package aagapp_backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PermissionUpdateRequest {

    private Boolean smsPermission;
    private Boolean whatsappPermission;
}
