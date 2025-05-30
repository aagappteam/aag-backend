package aagapp_backend.dto.ticketdto;

import aagapp_backend.enums.TicketEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketResponseDto {
    private Long id;
    private String subject;
    private String description;
    private TicketEnum status;
    private String remark;
    private Long customerOrVendorId;
    private String role;
    private String email;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Date createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Date updatedDate;
}
