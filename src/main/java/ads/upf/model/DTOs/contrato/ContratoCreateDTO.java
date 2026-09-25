package ads.upf.model.DTOs.contrato;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class ContratoCreateDTO {

    private Long contratoId;

    @NotNull(message = "A data de início é obrigatória.")
    @FutureOrPresent(message = "A data de início deve estar no presente ou futuro.")
    private LocalDate dataInicio;

    @NotNull(message = "A data de término é obrigatória.")
    @Future(message = "A data de término deve estar no futuro.")
    private LocalDate dataTermino;

    @NotNull(message = "O cliente é obrigatório.")
    private Long clienteId;

    @AssertTrue(message = "O contrato deve ter a duração mínima de 1 mês.")
    public boolean isPeriodoMinimoValido() {
        if (dataInicio == null || dataTermino == null) {
            return true; // Deixa o @NotNull cuidar dos nulos
        }

        // dataTermino deve ser igual ou posterior a dataInicio + 1 mês
        return !dataTermino.isBefore(dataInicio.plusMonths(1));
    }


}
