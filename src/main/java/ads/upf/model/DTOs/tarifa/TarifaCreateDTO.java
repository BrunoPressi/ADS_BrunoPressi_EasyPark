package ads.upf.model.DTOs.tarifa;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class TarifaCreateDTO {

    private Long id;

    @NotNull(message = "O valor da hora não pode ser nulo")
    @DecimalMin(value = "45.00", message = "O valor da hora mínimo deve ser R$ 45,00")
    @DecimalMax(value = "350.00", message = "O valor da hora excede o limite máximo permitido (R$ 350,00)")
    @Digits(integer = 3, fraction = 2, message = "Formato de valor da hora inválido. Use até 3 dígitos inteiros e 2 decimais.")
    private BigDecimal valorHora;

    private BigDecimal valorFracaoHora;

    public void setValorHora(BigDecimal valorHora) {
        this.valorHora = valorHora;
        calcularValorFracaoHora();
    }

    public void calcularValorFracaoHora() {
        if (this.valorHora != null) {
            this.valorFracaoHora = this.valorHora.divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
        } else {
            this.valorFracaoHora = null;
        }
    }

    @AssertTrue(message = "O valor da fração hora deve ser metade que o valor hora total")
    public boolean verificarValorFracaoHora() {
        if (this.valorHora == null) {
            return true;
        }
        if (this.valorFracaoHora == null) {
            calcularValorFracaoHora();
        }
        BigDecimal metadeEsperada = this.valorHora.divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);

        // Compara o valor numérico puro usando compareTo (retorna 0 se forem iguais)
        return this.valorFracaoHora.compareTo(metadeEsperada) == 0;
    }

}
