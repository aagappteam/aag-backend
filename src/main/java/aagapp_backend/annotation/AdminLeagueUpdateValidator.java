package aagapp_backend.annotation;

import aagapp_backend.dto.admin.league.AdminLeagueUpdateRequest;
import aagapp_backend.enums.LeagueStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AdminLeagueUpdateValidator implements ConstraintValidator<ValidAdminLeagueUpdate, AdminLeagueUpdateRequest> {

    @Override
    public boolean isValid(AdminLeagueUpdateRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        if (request.getStatus() == LeagueStatus.APPROVED) {
            if (request.getPrizePool() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Prize pool is required when you APPROVED league")
                        .addPropertyNode("prizePool")
                        .addConstraintViolation();
                return false;
            }
        }

        // If REJECTED or other cases, it's fine
        return true;
    }
}
