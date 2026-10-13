package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record OcupacaoKpiDTO(
        long totalVagas,
        long ocupadas,
        long disponiveis,
        long manutencao,
        BigDecimal taxaOcupacao
) {}
