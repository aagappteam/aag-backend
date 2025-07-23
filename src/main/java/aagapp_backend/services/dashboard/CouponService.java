package aagapp_backend.services.dashboard;

import aagapp_backend.entity.coupon.Coupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponService {
/*    @Autowired
    private CouponRepository couponRepository;

    public float applyCouponIfValid(String couponCode, float rechargeAmount) {
        if (couponCode == null || couponCode.trim().isEmpty()) return 0f;

        Coupon coupon = couponRepository.findByCodeAndActiveTrue(couponCode.trim())
                .orElseThrow(() -> new BusinessException("Invalid or expired coupon"));

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().before(new Date())) {
            throw new BusinessException("Coupon expired");
        }

        if (rechargeAmount < coupon.getMinimumRecharge()) {
            throw new BusinessException("Recharge amount must be at least ₹" + coupon.getMinimumRecharge());
        }

        return coupon.getBonusAmount(); // Return applicable bonus
    }*/
}

