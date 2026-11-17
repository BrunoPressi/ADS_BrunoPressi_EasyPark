package ads.upf.model.DTOs.relatorio;

import ads.upf.model.enums.PeriodoRelatorioTipo;
import ads.upf.model.enums.VagaTipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioFiltroDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private PeriodoRelatorioTipo agrupamento = PeriodoRelatorioTipo.DIA;

    /**
     * Filtro de origem para o Relatório Financeiro:
     * "TODAS", "ROTATIVO" ou "MENSALISTA".
     */
    private String origemReceita = "TODAS";

    /**
     * Filtro de modalidade para o Relatório de Ocupação:
     * "TODAS", "ROTATIVO" ou "MENSALISTA".
     */
    private String modalidadeOcupacao = "TODAS";

    /**
     * Filtro opcional de categoria de vaga para o Relatório de Ocupação.
     */
    private VagaTipo tipoVaga;

}
