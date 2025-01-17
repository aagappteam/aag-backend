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
public class VendorDeviceDTO {
    private Long vendorId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
}
