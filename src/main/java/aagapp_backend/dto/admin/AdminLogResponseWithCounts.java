package aagapp_backend.dto.admin;

import aagapp_backend.entity.admin.AdminLogs;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.Page;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminLogResponseWithCounts {
    private Page<AdminLogs> logs;
    private long readCount;
    private long unreadCount;


}

