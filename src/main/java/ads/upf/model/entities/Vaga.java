package ads.upf.model.entities;

import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import jakarta.persistence.*;
import lombok.*;

@Entity()
@Table(name = "vagas")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode (of = "id")
public class Vaga extends Auditado {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 3)
    private String nome;

    @Enumerated(EnumType.STRING)
    private VagaStatus status;

    @Enumerated(EnumType.STRING)
    private VagaTipo tipoVaga;

    public void editar() {
        if (this.status.equals(VagaStatus.ocupada)) throw new InvalidEditException("Vagas ocupadas não podem ser editadas");
    }

}
