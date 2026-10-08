package ads.upf.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "usuarios")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario extends Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telefone;
}
