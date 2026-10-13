package ads.upf.model.DTOs.dashboard;

import java.math.BigDecimal;

public record ParcelaVencerDTO(
        long parcelaId,
        long contratoId,
        String clienteNome,
        String telefone,
        int numeroParcela,
        String dataVencimento,
        BigDecimal valor
) {}
