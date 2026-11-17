package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioOcupacaoResumoDTO(
        long totalEntradas,
        long entradasRotativo,
        long entradasMensalista,
        BigDecimal percentualRotativo,
        BigDecimal percentualMensalista,
        long saidasConcluidas,
        long emAndamentoNoPeriodo,
        BigDecimal mediaVeiculosPorPeriodo,
        String periodoMaiorMovimento,
        long qtdMaiorMovimento,
        String periodoMenorMovimento,
        long qtdMenorMovimento,
        long tempoMedioMinutos,
        String tempoMedioFormatado
) implements Serializable {}
