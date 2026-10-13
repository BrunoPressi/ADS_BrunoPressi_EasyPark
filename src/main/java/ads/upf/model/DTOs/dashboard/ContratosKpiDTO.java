package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record ContratosKpiDTO(
        long totalCadastrados,
        long ativos,
        long inadimplentes,
        long cancelados,
        long encerrados,
        BigDecimal percentualVagasComprometidas
) {}
