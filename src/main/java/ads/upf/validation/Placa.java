package ads.upf.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotação para validação de placa de veículo nos padrões brasileiros:
 * - Mercosul: 3 letras, 1 número, 1 letra e 2 números (ex: ABC1D23 ou ABC-1D23)
 * - Padrão Anterior: 3 letras e 4 números, com ou sem hífen (ex: ABC-1234 ou ABC1234)
 */
@Documented
@Constraint(validatedBy = PlacaValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Placa {

    String message() default "Placa inválida. Utilize o padrão Mercosul (ex: ABC1D23) ou o padrão anterior (ex: ABC-1234).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
