package ads.upf.model.DTOs.tarifa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class TarifaResponseDTO {

    private Long id;
    private BigDecimal valorHora;
    private BigDecimal valorFracaoHora;
    private boolean ativo;

}
