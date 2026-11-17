package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioOcupacaoVagaTipoDTO(
        String tipoVaga,
        long totalVagasCadastradas,
        long totalUtilizacoes,
        BigDecimal percentualUtilizacao,
        String tempoMedioFormatado
) implements Serializable {}
