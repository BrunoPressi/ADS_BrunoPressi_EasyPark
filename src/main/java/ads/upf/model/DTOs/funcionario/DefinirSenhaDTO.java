package ads.upf.model.DTOs.funcionario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class DefinirSenhaDTO {

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no m\u00ednimo 8 caracteres")
    private String novaSenha;

    @NotBlank(message = "Confirmar a senha é obrigatório")
    private String novaSenhaConfirmar;

}
