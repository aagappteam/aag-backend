package aagapp_backend.dto;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonthlyEarningWithVendorDTO {
    private Long id;
    private Long influencerId;
    private String monthYear;
    private BigDecimal rechargeAmount;
    private int multiplier;
    private BigDecimal earnedAmount;
    private BigDecimal maxReturnAmount;

    private String vendorName;
    private String mobile;

    // Constructor
    public MonthlyEarningWithVendorDTO(InfluencerMonthlyEarning e, VendorEntity vendor) {
        this.id = e.getId();
        this.influencerId = e.getInfluencerId();
        this.monthYear = e.getMonthYear();
        this.rechargeAmount = e.getRechargeAmount();
        this.multiplier = e.getMultiplier();
        this.earnedAmount = e.getEarnedAmount();
        this.maxReturnAmount = e.getMaxReturnAmount();

        this.vendorName = vendor.getFirst_name() + " " + vendor.getLast_name();
        this.mobile = vendor.getMobileNumber();
    }

}
