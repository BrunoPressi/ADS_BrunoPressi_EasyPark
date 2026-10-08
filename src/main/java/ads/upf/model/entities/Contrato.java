package ads.upf.model.entities;

import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ContratoTipo;
import ads.upf.model.enums.ParcelaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contratos")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Contrato extends Auditado {

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

    public void adicionarParcela(Parcela parcela) {
        parcelas.add(parcela);
        parcela.setContrato(this); // Sincroniza o lado dono
    }

    public void removerParcela(Parcela parcela) {
        parcelas.remove(parcela);
        parcela.setContrato(null); // Desvincula
    }

    public boolean possuiParcelaPaga() {
        return this.parcelas.stream()
                .anyMatch(p -> p.getStatus() == ParcelaStatus.paga || p.getDataPagamento() != null);
    }

    public boolean possuiParcelaAtrasada() {
        return this.parcelas.stream()
                .anyMatch(p -> p.getStatus() == ParcelaStatus.atrasada);
    }

    public void atualizarVigenciaEParcelas(LocalDate novaDataInicio, LocalDate novaDataTermino, List<Parcela> novasParcelas) {
        if (this.status != ContratoStatus.ativo) {
            throw new InvalidEditException("Não é permitido editar um contrato que não esteja ativo.");
        }
        if (possuiParcelaPaga()) {
            throw new InvalidEditException("Não é possível alterar as datas de um contrato com parcelas já pagas.");
        }

        this.dataInicio = novaDataInicio;
        this.dataTermino = novaDataTermino;

        // Limpa as atuais e adiciona as novas
        new ArrayList<>(this.parcelas).forEach(this::removerParcela);
        novasParcelas.forEach(this::adicionarParcela);
    }

    public void cancelar() {
        if (this.status == ContratoStatus.cancelado || this.status == ContratoStatus.encerrado) {
            throw new InvalidEditException("Contratos encerrados ou cancelados não podem ser cancelados!");
        }
        if (possuiParcelaAtrasada()) {
            throw new InvalidEditException("Não é possível cancelar um contrato com parcelas atrasadas!");
        }

        this.status = ContratoStatus.cancelado;
    }

}
