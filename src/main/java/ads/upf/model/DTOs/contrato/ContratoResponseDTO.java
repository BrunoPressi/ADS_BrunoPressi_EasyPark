package ads.upf.model.DTOs.contrato;

import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.enums.ContratoStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class ContratoResponseDTO {

    private Long id;
    private LocalDate dataInicio;
    private LocalDate dataTermino;
    private BigDecimal valorContratado;
    private ContratoStatus status;
    private ClienteResponseDTO cliente;

}
