package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class CustomerDeviceDTO {
    private Long customerId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
}
