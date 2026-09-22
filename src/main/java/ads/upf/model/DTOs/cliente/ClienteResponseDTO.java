package ads.upf.model.DTOs.cliente;

import ads.upf.model.DTOs.usuario.UsuarioResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClienteResponseDTO extends UsuarioResponseDTO {

    private String cpfEncrypted;

}
