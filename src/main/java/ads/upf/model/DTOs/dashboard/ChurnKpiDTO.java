package ads.upf.model.DTOs.dashboard;

public record ChurnKpiDTO(
        long novosContratosMes,
        long contratosPerdidosMes,
        long saldoLiquidoMes
) {}
