package ads.upf.model.DTOs.usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nomeCompleto;
    private String telefone;
    private String email;
}
