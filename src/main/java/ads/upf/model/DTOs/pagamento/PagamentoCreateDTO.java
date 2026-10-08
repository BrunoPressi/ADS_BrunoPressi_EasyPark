package ads.upf.model.DTOs.pagamento;

import ads.upf.model.enums.PagamentoMeio;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class PagamentoCreateDTO {

    @NotNull(message = "O meio de pagamento é obrigatório")
    private PagamentoMeio meioPagamento;

}
