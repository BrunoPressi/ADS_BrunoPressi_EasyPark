package ads.upf.model.DTOs.pagamento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumoCalculoPermanenciaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime dataEntrada;
    private LocalDateTime dataSaida;
    private long minutosTotais;
    private String tempoFormatado;

    private BigDecimal valorPrimeiraHora;
    private BigDecimal valorUnitarioFracao;
    private long quantidadeFracoesAdicionais;
    private BigDecimal valorTotalFracoes;
    private BigDecimal valorTotal;

}
