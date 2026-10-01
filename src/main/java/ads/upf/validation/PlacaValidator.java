package ads.upf.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para a anotação {@link Placa}.
 * <p>
 * Suporta:
 *   Padrão Mercosul (Brasil):</b> 3 letras, 1 número, 1 letra e 2 números (ex: <code>BRA2E19</code> ou <code>ABC-1D23</code>).
 *   Padrão Anterior:</b> 3 letras e 4 números, com ou sem hífen (ex: <code>ABC-1234</code> ou <code>ABC1234</code>).
 */
public class PlacaValidator implements ConstraintValidator<Placa, String> {

    /**
     * Expressão regular que valida ambos os formatos:
     *     <li><code>^[a-zA-Z]{3}</code>: exatamente 3 letras iniciais.</li>
     *     <li><code>-?</code>: hífen opcional.</li>
     *     <li><code>[0-9]</code>: 1 número obrigatório (4º caractere alfanumérico).</li>
     *     <li><code>[a-zA-Z0-9]</code>: se letra, completa o padrão Mercosul (LLLNLNN); se número, completa o padrão anterior (LLLNNNN).</li>
     *     <li><code>[0-9]{2}$</code>: exatamente 2 números finais.</li>
     */
    private static final Pattern PLACA_PATTERN = Pattern.compile("^[a-zA-Z]{3}-?[0-9][a-zA-Z0-9][0-9]{2}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Conforme convenção do Jakarta Bean Validation, valores nulos são considerados válidos
        // para que a responsabilidade de obrigatoriedade seja de anotações como @NotNull ou @NotBlank.
        if (value == null) {
            return true;
        }

        return PLACA_PATTERN.matcher(value.trim()).matches();
    }
}
