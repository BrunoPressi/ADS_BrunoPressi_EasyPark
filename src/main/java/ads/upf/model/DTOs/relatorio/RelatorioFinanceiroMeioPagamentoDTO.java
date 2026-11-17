package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioFinanceiroMeioPagamentoDTO(
        String meioPagamento,
        long quantidade,
        BigDecimal valorTotal,
        BigDecimal percentual
) implements Serializable {}
