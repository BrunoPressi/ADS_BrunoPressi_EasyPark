package ads.upf.model.enums;

public enum PeriodoRelatorioTipo {

    DIA("Diário", "day", "DD/MM/YYYY"),
    SEMANA("Semanal", "week", "\"Sem. \"IW/IYYY (DD/MM)"),
    MES("Mensal", "month", "MM/YYYY"),
    ANO("Anual", "year", "YYYY");

    private final String descricao;
    private final String pgTruncUnit;
    private final String pgDateFormat;

    PeriodoRelatorioTipo(String descricao, String pgTruncUnit, String pgDateFormat) {
        this.descricao = descricao;
        this.pgTruncUnit = pgTruncUnit;
        this.pgDateFormat = pgDateFormat;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPgTruncUnit() {
        return pgTruncUnit;
    }

    public String getPgDateFormat() {
        return pgDateFormat;
    }
}
