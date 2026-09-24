package ads.upf.model.entities;

import ads.upf.model.enums.TipoVaga;
import ads.upf.model.enums.VagaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity()
@Table(name = "vagas")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode (of = "id")
public class Vaga {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 3)
    private String nome;

    @Enumerated(EnumType.STRING)
    private VagaStatus status;

    @Enumerated(EnumType.STRING)
    private TipoVaga tipoVaga;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

}
