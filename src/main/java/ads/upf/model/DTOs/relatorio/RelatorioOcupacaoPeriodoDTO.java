package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;

public record RelatorioOcupacaoPeriodoDTO(
        String periodoLabel,
        long totalEntradas,
        long entradasRotativo,
        long entradasMensalista,
        long saidasConcluidas,
        long tempoMedioMinutos,
        String tempoMedioFormatado
) implements Serializable {}
