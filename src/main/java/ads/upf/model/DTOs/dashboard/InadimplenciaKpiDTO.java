package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record InadimplenciaKpiDTO(
        long parcelasAtrasadas,
        BigDecimal valorInadimplente,
        BigDecimal taxaInadimplenciaPercentual
) {}
