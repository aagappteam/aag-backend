package aagapp_backend.dto;

import aagapp_backend.entity.withdrawrequest.WithdrawalRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WithdrawalRequestHistoryDTO {
    private Long id;
    private Long influencerId;
    private String influencerName;
    private String mobile;
    private String email;
    private BigDecimal amount;
    private String monthYear;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime requestedAt;

    // Constructor with WithdrawalRequest and vendor details
    public WithdrawalRequestHistoryDTO(WithdrawalRequest request, String influencerName, String mobile, String email) {
        this.id = request.getId();
        this.influencerId = request.getInfluencerId();
        this.influencerName = influencerName;
        this.mobile = mobile;
        this.email = email;
        this.amount = request.getAmount();
        this.monthYear = request.getMonthYear();
        this.status = request.getStatus();
        this.requestedAt = request.getRequestedAt();
    }
}
