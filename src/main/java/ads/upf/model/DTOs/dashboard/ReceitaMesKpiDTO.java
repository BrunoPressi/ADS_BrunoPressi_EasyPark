package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record ReceitaMesKpiDTO(
        BigDecimal receitaRotativo,
        BigDecimal receitaMensalistas,
        BigDecimal receitaTotalMes,
        BigDecimal percentualRotativo,
        BigDecimal percentualMensalistas,
        BigDecimal ticketMedioRotativo,
        BigDecimal revpas
) {}
