package ads.upf.model.DTOs;

import ads.upf.model.enums.VagaStatus;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class VagaCreateDTO {

        @Pattern(regexp = "^[A-Z]\\d{2}$", message = "Deve estar no formato LNN (uma letra, dois números)")
        private String nome;
}
