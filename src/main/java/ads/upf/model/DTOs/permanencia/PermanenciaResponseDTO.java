package ads.upf.model.DTOs.permanencia;

import ads.upf.model.DTOs.pagamento.PagamentoResponseDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.enums.PermanenciaStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class PermanenciaResponseDTO {

    private Long id;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataSaida;
    private PermanenciaStatus status;
    private VeiculoResponseDTO veiculo;
    private VagaResponseDTO vaga;
    private PagamentoResponseDTO pagamento;

    private Boolean isRotativo;
}
