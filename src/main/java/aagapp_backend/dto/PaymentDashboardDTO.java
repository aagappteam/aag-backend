package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDashboardDTO {

    private String planName;
    private String planVariant;
    private String returnPercentage;
    private String dailyLimit;
    private Long id;
    private Double price;

    private String returnX;
    private Integer progressPercent;
    private Integer filledBoxes;
    private Integer totalBoxes;

    public PaymentDashboardDTO(String planName, String planVariant, String returnPercentage, String dailyLimit, Long id, Double price,
                               String returnX, Integer progressPercent, Integer filledBoxes, Integer totalBoxes) {
        this.planName = planName;
        this.planVariant = planVariant;
        this.returnPercentage = returnPercentage;
        this.dailyLimit = dailyLimit;
        this.id = id;
        this.price = price;
        this.returnX = returnX;
        this.progressPercent = progressPercent;
        this.filledBoxes = filledBoxes;
        this.totalBoxes = totalBoxes;
    }

}
