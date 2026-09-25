package ads.upf.model.entities;

import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ContratoTipo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratos")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Contrato {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataTermino;

    @Column(nullable = false)
    private BigDecimal valorContratado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    private ContratoStatus status;

    @Enumerated(EnumType.STRING)
    private ContratoTipo contratoTipo;

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
        this.setStatus(ContratoStatus.ativo);
        this.setContratoTipo(ContratoTipo.mensal);
        this.setValorContratado(BigDecimal.valueOf(180.00));
        this.criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdateUsuario() {
        this.atualizadoEm = LocalDateTime.now();
    }

}
