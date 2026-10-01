package ads.upf.model.DTOs.veiculo;

import ads.upf.model.enums.VeiculoTipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class VeiculoResponseDTO {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private String cor;
    private VeiculoTipo tipo;

}
