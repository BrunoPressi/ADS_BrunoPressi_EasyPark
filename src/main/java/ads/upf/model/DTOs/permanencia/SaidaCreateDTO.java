package ads.upf.model.DTOs.permanencia;

import ads.upf.model.DTOs.pagamento.PagamentoCreateDTO;
import ads.upf.model.enums.PagamentoMeio;
import ads.upf.validation.Placa;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class SaidaCreateDTO {

    @NotNull(message = "O pagamento é obrigatório")
    private PagamentoMeio meioPagamento;

    @NotNull(message = "O valor é obrigatório")
    private BigDecimal valor;

    @NotNull(message = "A placa do veículo é obrigatória")
    @Placa
    private String placa;

    @NotNull(message = "A data de saída é obrigatória")
    private LocalDateTime dataSaida;

}
