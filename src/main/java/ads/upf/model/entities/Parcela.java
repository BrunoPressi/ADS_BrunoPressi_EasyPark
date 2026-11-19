package ads.upf.model.entities;

import ads.upf.model.enums.ParcelaMeiosPagamento;
import ads.upf.model.enums.ParcelaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Parcelas")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Parcela extends Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String asaasPaymentId;

    @Column
    private String invoiceUrl;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = true)
    private LocalDate dataPagamento;

    @Column(nullable = false)
    private Integer numeroParcela;

    @Column(nullable = false)
    private BigDecimal valor;

    /** Multa por atraso (0,15% do valor), preenchida quando a parcela fica atrasada. */
    @Column
    private BigDecimal valorMulta;

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
        super.prePersist();
        this.valor = BigDecimal.valueOf(180.00);
    }

}

