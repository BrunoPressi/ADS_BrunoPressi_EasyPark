package ads.upf.model.DTOs.vaga;

import ads.upf.model.enums.VagaStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class VagaResponseDTO {
    private Long id;
    private String nome;
    private VagaStatus status;
}
