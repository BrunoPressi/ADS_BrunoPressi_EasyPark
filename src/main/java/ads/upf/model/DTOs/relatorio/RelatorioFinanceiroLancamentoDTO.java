package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioFinanceiroLancamentoDTO(
        String codigoReferencia,
        String dataPagamentoFormatada,
        String origem,
        String identificacao,
        String meioPagamento,
        BigDecimal valor
) implements Serializable {}
