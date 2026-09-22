package ads.upf.model.DTOs.cliente;

import ads.upf.model.DTOs.usuario.UsuarioCreateDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter @Setter
public class ClienteCreateDTO extends UsuarioCreateDTO {

    @NotBlank(message = "O CPF é obrigatório.")
    @CPF(message = "O CPF deve ser válido.")
    private String cpf;

}
