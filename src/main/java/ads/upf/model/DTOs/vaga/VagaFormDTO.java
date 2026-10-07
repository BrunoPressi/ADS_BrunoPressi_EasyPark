package ads.upf.model.DTOs.vaga;

import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class VagaFormDTO {

        private Long id;

        @Pattern(regexp = "^[A-Z]\\d{2}$", message = "Deve estar no formato LNN (uma letra, dois números)")
        private String nome;

        @NotNull(message = "O status da vaga é obrigatório.")
        private VagaStatus status;

        @NotNull(message = "O tipo da vaga é obrigatório.")
        private VagaTipo tipoVaga;

}
