package ads.upf.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "usuarios")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String dataNascimento;

    @Column(nullable = true)
    private LocalDateTime criadoEm;

    @Column(nullable = true)
    private LocalDateTime atualizadoEm;

    @Column(nullable = true)
    private String criadoPor;

    @Column(nullable = true)
    private String atualizadoPor;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdateUsuario() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
