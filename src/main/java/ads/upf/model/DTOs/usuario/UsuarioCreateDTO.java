package ads.upf.model.DTOs.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UsuarioCreateDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    private String nomeCompleto;

    @Email(message = "O email informado deve ser válido.")
    @NotBlank(message = "O email é obrigatório.")
    private String email;

    @NotBlank(message = "O telefone é obrigatório.")
    @Pattern(
            regexp = "^(?:\\(?([1-9]{2})\\)?[-. ]?)?(?:[2-8]|9[0-9])[0-9]{3}[-. ]?[0-9]{4}$",
            message = "O telefone informado é inválido."
    )
    private String telefone;

}
