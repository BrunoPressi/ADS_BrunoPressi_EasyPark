package ads.upf.model.entities;

import ads.upf.model.enums.FuncionarioRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Table(name = "funcionarios")
@PrimaryKeyJoinColumn(name = "usuarioId")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Funcionario extends Usuario{

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    private FuncionarioRole role;

    @Override
    protected void prePersist() {
        super.prePersist();
        this.role = FuncionarioRole.atendente;
    }

}
