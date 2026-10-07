package ads.upf.model.entities;

import ads.upf.model.enums.PagamentoMeio;
import ads.upf.model.enums.PagamentoStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Pagamento extends Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column
    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    private PagamentoStatus status;

    @Enumerated(EnumType.STRING)
    private PagamentoMeio meioPagamento;

    @Column
    private BigDecimal desconto;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permanencia_id")
    private Permanencia permanencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

}
