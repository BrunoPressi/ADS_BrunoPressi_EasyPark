package ads.upf.model.DTOs.funcionario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class FuncionarioResponseDTO {

    private Long id;
    private String nomeCompleto;
    private String dataNascimento;
    private String telefone;
    private String email;

}
