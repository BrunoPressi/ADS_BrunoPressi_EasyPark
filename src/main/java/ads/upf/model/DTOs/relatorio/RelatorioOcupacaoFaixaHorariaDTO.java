package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioOcupacaoFaixaHorariaDTO(
        String faixaHoraria,
        long totalEntradas,
        long entradasRotativo,
        long entradasMensalista,
        BigDecimal percentualMovimento
) implements Serializable {}
