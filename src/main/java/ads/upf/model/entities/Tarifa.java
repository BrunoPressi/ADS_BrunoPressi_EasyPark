package ads.upf.model.entities;

import ads.upf.exceptions.InvalidEditException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifas")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class Tarifa extends Auditado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal valorHora = BigDecimal.valueOf(70.00);

    @Column(nullable = false)
    private BigDecimal valorFracaoHora = BigDecimal.valueOf(35.00);

    @Column(nullable = false)
    private boolean ativo;

    @Override
    protected void prePersist() {
        super.prePersist();
        this.ativo = true;
    }

    public void editar() {
        if (!this.ativo) {
            throw new InvalidEditException("Tarifas desativadas não podem ser editadas.");
        }
    }

    public void desativar() {
        if (!this.ativo) {
            throw new InvalidEditException("Esta tarifa já está desativada.");
        }
        this.ativo = false;
    }

}
