package ads.upf.model.DTOs.dashboard;

public record EstadiaExcessivaDTO(
        long permanenciaId,
        String placa,
        String modelo,
        String vagaNome,
        String dataEntrada,
        double horasEstacionado,
        boolean isRotativo
) {}
