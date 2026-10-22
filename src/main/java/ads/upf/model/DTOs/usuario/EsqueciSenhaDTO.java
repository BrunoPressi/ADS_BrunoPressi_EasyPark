package ads.upf.model.DTOs.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class EsqueciSenhaDTO {

    @NotBlank(message = "O E-mail é obrigatório")
    @Email(message = "O E-mail deve ser válido")
    private String email;

}
