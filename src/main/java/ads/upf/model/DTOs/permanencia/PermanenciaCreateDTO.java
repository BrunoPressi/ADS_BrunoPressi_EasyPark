package ads.upf.model.DTOs.permanencia;

import ads.upf.model.enums.VagaTipo;
import ads.upf.model.enums.VeiculoTipo;
import ads.upf.validation.Placa;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class PermanenciaCreateDTO {

    @NotNull(message = "A placa é obrigatória")
    @Placa
    private String placa;

    @NotNull(message = "A vaga é obrigatória")
    private VagaTipo tipoVaga;

    @NotNull(message = "O tipo do veículo é obrigatório")
    private VeiculoTipo tipoVeiculo;

}
