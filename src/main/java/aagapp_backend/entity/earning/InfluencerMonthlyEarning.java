package aagapp_backend.entity.earning;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "influencer_monthly_earning", uniqueConstraints = @UniqueConstraint(columnNames = {"influencerId", "monthYear"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerMonthlyEarning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long influencerId;
    private String monthYear;
    private BigDecimal rechargeAmount;
    private Integer multiplier;
    private BigDecimal earnedAmount = BigDecimal.ZERO;

    public BigDecimal getMaxReturnAmount() {
        return rechargeAmount.multiply(BigDecimal.valueOf(multiplier));
    }
}