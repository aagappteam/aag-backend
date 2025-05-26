package aagapp_backend.dto;

import aagapp_backend.entity.withdrawrequest.WithdrawalRequest;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WithdrawalRequestDTO {
    private Long id;
    private Long influencerId;
    private String influencerName;
    private BigDecimal amount;
    private String monthYear;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime requestedAt;
    // Constructor used by Hibernate in JPQL
    public WithdrawalRequestDTO(Long id, Long influencerId, String influencerName, BigDecimal amount,
                                String monthYear, String status, LocalDateTime requestedAt) {
        this.id = id;
        this.influencerId = influencerId;
        this.influencerName = influencerName;
        this.amount = amount;
        this.monthYear = monthYear;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    // Constructors, Getters, Setters
    public WithdrawalRequestDTO(WithdrawalRequest request, String influencerName) {
        this.id = request.getId();
        this.influencerId = request.getInfluencerId();
        this.influencerName = influencerName;
        this.amount = request.getAmount();
        this.monthYear = request.getMonthYear();
        this.status = request.getStatus();
        this.requestedAt = request.getRequestedAt();
    }
}
