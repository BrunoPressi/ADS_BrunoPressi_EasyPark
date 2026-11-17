package ads.upf.model.DTOs.relatorio;

import java.io.Serializable;
import java.math.BigDecimal;

public record RelatorioOcupacaoDetalheDTO(
        Long permanenciaId,
        String placa,
        String veiculoDescricao,
        String vagaNome,
        String tipoVaga,
        boolean rotativo,
        String dataEntradaFormatada,
        String dataSaidaFormatada,
        String duracaoFormatada,
        String status,
        BigDecimal valorPago
) implements Serializable {}
