package ads.upf.model.DTOs.dashboard;

public record TempoMedioKpiDTO(
    long tempoEmMinutos,
    String tempoMedioFormatado
) {}
