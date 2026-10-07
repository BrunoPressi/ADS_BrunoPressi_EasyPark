package ads.upf.model.entities;

import ads.upf.model.enums.VeiculoTipo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veiculos")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Veiculo extends  Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String placa;

    @Column()
    private String marca;

    @Column()
    private String modelo;

    @Column()
    private String cor;

    @Enumerated(EnumType.STRING)
    private VeiculoTipo tipo;

    @OneToMany(mappedBy = "veiculo", fetch = FetchType.LAZY)
    private List<Permanencia> permanenciaList = new ArrayList<>();

}
