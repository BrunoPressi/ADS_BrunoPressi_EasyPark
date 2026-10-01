package ads.upf.model.DTOs.veiculo;

import ads.upf.model.enums.VeiculoTipo;
import ads.upf.validation.Placa;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class VeiculoCreateDTO {

    @Placa
    private String placa;

    @NotNull(message = "O tipo do veículo é obrigatório.")
    private VeiculoTipo tipo;

    private String cor;
    private String marca;
    private String modelo;

}
