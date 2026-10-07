package ads.upf.model.entities;

import ads.upf.model.enums.ParcelaMeiosPagamento;
import ads.upf.model.enums.ParcelaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Parcelas")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Parcela extends Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = true)
    private LocalDate dataPagamento;

    @Column(nullable = false)
    private Integer numeroParcela;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private ParcelaMeiosPagamento meioPagamento;

    @Column(nullable = false)
    private Boolean cobrancaEnviada;

    @Enumerated(EnumType.STRING)
    private ParcelaStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @Override
    protected void prePersist() {
        this.valor = BigDecimal.valueOf(180.00);
        this.dataPagamento = null;
        this.meioPagamento = null;
        this.cobrancaEnviada = false;
    }

}

