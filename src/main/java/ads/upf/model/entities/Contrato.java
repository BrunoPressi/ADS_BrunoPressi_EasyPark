package ads.upf.model.entities;

import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ContratoTipo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "contrato",
            cascade = CascadeType.PERSIST,
            orphanRemoval = true)
    private List<Parcela> parcelas = new ArrayList<>();

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
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // Método auxiliar (helper) para adicionar
    public void adicionarParcela(Parcela parcela) {
        parcelas.add(parcela);
        parcela.setContrato(this); // Sincroniza o lado dono
    }

    // Método auxiliar para remover (ativa o orphanRemoval)
    public void removerParcela(Parcela parcela) {
        parcelas.remove(parcela);
        parcela.setContrato(null); // Desvincula
    }

}
