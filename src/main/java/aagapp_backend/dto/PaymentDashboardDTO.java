package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentDashboardDTO {

    private String planName;
    private String planVariant;
    private String returnPercentage;
    private String dailyLimit;
    private Long id;
    private Double price;
}
