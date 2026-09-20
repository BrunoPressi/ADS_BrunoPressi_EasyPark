package ads.upf.model.DTOs.funcionario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class FuncionarioCreateDTO {

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

    @NotBlank(message = "A data de nascimento é obrigatória.")
    @Pattern(
            regexp = "^(?:(?:0[1-9]|[12][0-9]|30)/(?:0[13-9]|1[0-2])|31/(?:0[13578]|1[02])|(?:0[1-9]|1[0-9]|2[0-8])/02)/(?:19|20)\\d{2}$|^(?:29/02/(?:(?:19|20)(?:04|08|[2468][048]|[13579][26])))$",
            message = "A data de nascimento informada é inválida."
    )
    private String dataNascimento;

}
