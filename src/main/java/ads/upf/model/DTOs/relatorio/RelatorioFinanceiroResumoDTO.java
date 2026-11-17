package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioFinanceiroResumoDTO(
        BigDecimal receitaTotal,
        BigDecimal receitaRotativo,
        BigDecimal receitaMensalista,
        BigDecimal percentualRotativo,
        BigDecimal percentualMensalista,
        BigDecimal receitaMediaPorPeriodo,
        String periodoMaiorReceita,
        BigDecimal valorMaiorReceita,
        String periodoMenorReceita,
        BigDecimal valorMenorReceita,
        long totalTransacoes,
        long transacoesRotativo,
        long transacoesMensalista,
        BigDecimal ticketMedioGeral
) implements Serializable {}
