package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record GiroVagasKpiDTO(
        BigDecimal giroMedioHoje,
        long totalEntradasHoje
) {}
