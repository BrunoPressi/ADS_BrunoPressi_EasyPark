package ads.upf.model.entities;

import ads.upf.model.enums.PermanenciaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "permanencias")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Permanencia extends Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataEntrada;

    @Column
    private LocalDateTime dataSaida;

    @Enumerated(EnumType.STRING)
    private PermanenciaStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id")
    private Vaga vaga;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @Column()
    private Boolean isRotativo;

}
