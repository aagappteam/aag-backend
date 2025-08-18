package aagapp_backend.entity.earning;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
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
/*    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private ZonedDateTime createdDate;*/


    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;


    @CurrentTimestamp
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = new Date();
    }


    public BigDecimal getMaxReturnAmount() {
        return rechargeAmount.multiply(BigDecimal.valueOf(multiplier));
    }
}
