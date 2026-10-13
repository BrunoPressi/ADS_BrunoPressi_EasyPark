package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record ReceitaHojeKpiDTO(
        BigDecimal faturamentoHoje,
        long totalTransacoesHoje
) {}
