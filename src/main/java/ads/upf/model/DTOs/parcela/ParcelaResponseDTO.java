package ads.upf.model.DTOs.parcela;

import ads.upf.model.enums.ParcelaMeiosPagamento;
import ads.upf.model.enums.ParcelaStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ParcelaResponseDTO {

    private Long id;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private Integer numeroParcela;
    private BigDecimal valor;
    private ParcelaMeiosPagamento meioPagamento;
    private Boolean cobrancaEnviada;
    private ParcelaStatus status;

}
