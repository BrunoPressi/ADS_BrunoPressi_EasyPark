package ads.upf.model.entities;

import ads.upf.model.enums.UsuarioRole;
import ads.upf.utils.SecurityUtil;
import ads.upf.utils.StringCryptoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuarioId")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Cliente extends Usuario {

    @Convert(converter = StringCryptoConverter.class)
    @Column(nullable = false)
    private String cpf;

    @Column(unique = true, nullable = false, length = 64)
    private String cpfHash;

    @Enumerated(EnumType.STRING)
    private UsuarioRole role;

    @Override
    protected void prePersist() {
        this.role = UsuarioRole.cliente;
    }

    // Método facilitador: atualiza o CPF e gera o hash automaticamente
    public void setCpf(String cpf) {
        if (cpf != null) {
            this.cpf = cpf;
            this.cpfHash = SecurityUtil.generateBlindIndex(cpf);
        } else {
            this.cpf = null;
            this.cpfHash = null;
        }
    }

}
