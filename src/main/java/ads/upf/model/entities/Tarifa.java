package ads.upf.model.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import java.math.BigDecimal;

public class Tarifa extends PanacheEntity {

    private static BigDecimal valorHora = BigDecimal.valueOf(70.00);
    private static BigDecimal valorFracaoHora = BigDecimal.valueOf(35.00);

    public static BigDecimal getValorHora() {
        return valorHora;
    }

    public static void setValorHora(BigDecimal valorHora) {
        Tarifa.valorHora = valorHora;
    }

    public static BigDecimal getValorFracaoHora() {
        return valorFracaoHora;
    }

    public static void setValorFracaoHora(BigDecimal valorFracaoHora) {
        Tarifa.valorFracaoHora = valorFracaoHora;
    }

}
