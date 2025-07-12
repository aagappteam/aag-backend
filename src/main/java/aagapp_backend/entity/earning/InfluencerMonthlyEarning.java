package aagapp_backend.entity.earning;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "influencer_monthly_earning")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerMonthlyEarning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long influencerId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "month_year")
    private String monthYear;

    @Column(name = "recharge_amount")
    private BigDecimal rechargeAmount;

    private Integer multiplier;

    @Column(name = "earned_amount")
    private BigDecimal earnedAmount = BigDecimal.ZERO;
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    public BigDecimal getMaxReturnAmount() {
        return rechargeAmount.multiply(BigDecimal.valueOf(multiplier));
    }
}
