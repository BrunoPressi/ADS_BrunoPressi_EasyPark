package ads.upf.model.DTOs.contrato;

import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ContratoTipo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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
    private ClienteResponseDTO cliente;

    @NotNull(message = "O veículo é obrigatório.")
    private VeiculoResponseDTO veiculo;

    @NotNull(message = "O valor contratado é obrigatório.")
    @Positive(message = "O valor deve ser positivo.")
    @Digits(integer = 3, fraction = 2, message = "O preço deve ter no máximo 3 dígitos e 2 casas decimais")
    private BigDecimal valorContratado;

    @NotNull(message = "O tipo do contrato é obrigatório.")
    private ContratoTipo contratoTipo;

    private ContratoStatus status = ContratoStatus.ativo;

    @AssertTrue(message = "O contrato deve ter a duração mínima de 1 mês.")
    public boolean isPeriodoMinimoValido() {
        if (dataInicio == null || dataTermino == null) {
            return true; // Deixa o @NotNull cuidar dos nulos
        }

        // dataTermino deve ser igual ou posterior a dataInicio + 1 mês
        return !dataTermino.isBefore(dataInicio.plusMonths(1));
    }

}
