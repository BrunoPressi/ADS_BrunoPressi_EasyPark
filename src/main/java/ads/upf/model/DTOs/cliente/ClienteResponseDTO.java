package ads.upf.model.DTOs.cliente;

import ads.upf.model.DTOs.usuario.UsuarioResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClienteResponseDTO extends UsuarioResponseDTO {

    private String cpf;

    public String getCpf() {
        if (this.cpf == null) return null;
        return this.cpf.replaceAll("^(\\d{3})\\.?(\\d{3})\\.?(\\d{3})-?(\\d{2})$", "$1.***.***-$4");
    }

    public String getCpfNormal() {
        return this.cpf;
    }

}
