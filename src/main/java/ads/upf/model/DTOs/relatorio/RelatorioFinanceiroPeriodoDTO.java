package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioFinanceiroPeriodoDTO(
        String periodoLabel,
        long qtdRotativo,
        BigDecimal valorRotativo,
        long qtdMensalista,
        BigDecimal valorMensalista,
        long totalTransacoes,
        BigDecimal valorTotal,
        BigDecimal ticketMedio
) implements Serializable {}
