package aagapp_backend.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AdminLeagueUpdateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAdminLeagueUpdate {
    String message() default "Invalid Admin League Update Request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
