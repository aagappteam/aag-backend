package aagapp_backend.services.bonus;

import org.springframework.stereotype.Service;

@Service
public class BonusOfferService {

    public float getBonusForAmount(float amount) {
        if (amount == 200f) return 100f;
        if (amount == 500f) return 500f;
        if (amount == 1000f) return 500f;
        return 0f;
    }

    public String getBonusLabel(float amount) {
        if (amount == 200f) return "50% Bonus on ₹200";
        if (amount == 500f) return "100% Bonus on ₹500";
        if (amount == 1000f) return "₹500 Bonus on ₹1000";
        return "";
    }
}

