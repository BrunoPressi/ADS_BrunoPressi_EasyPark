package ads.upf.model.DTOs.contrato;

import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.DTOs.parcela.ParcelaResponseDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ContratoTipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class ContratoResponseDTO {

    private Long id;
    private LocalDate dataInicio;
    private LocalDate dataTermino;
    private BigDecimal valorContratado;
    private ContratoTipo contratoTipo;
    private ContratoStatus status;
    private ClienteResponseDTO cliente;
    private VeiculoResponseDTO veiculo;
    private List<ParcelaResponseDTO> parcelas;

}
