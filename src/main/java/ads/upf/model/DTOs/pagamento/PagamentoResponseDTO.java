package ads.upf.model.DTOs.pagamento;

import ads.upf.model.enums.PagamentoMeio;
import ads.upf.model.enums.PagamentoStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagamentoResponseDTO {

    private Long id;
    private BigDecimal valor;
    private PagamentoMeio meioPagamento;
    private LocalDateTime dataPagamento;
    private PagamentoStatus status;

}
