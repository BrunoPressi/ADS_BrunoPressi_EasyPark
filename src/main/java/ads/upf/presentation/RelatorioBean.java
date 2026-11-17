package ads.upf.presentation;

import ads.upf.model.DTOs.relatorio.*;
import ads.upf.model.enums.PeriodoRelatorioTipo;
import ads.upf.model.enums.VagaTipo;
import ads.upf.services.RelatorioService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ViewScoped
@Named("relatorioBean")
@Getter
@Setter
public class RelatorioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RelatorioService relatorioService;

    @Inject
    private transient SecurityIdentity identity;

    private RelatorioFiltroDTO filtro;
    private String atalhoAtivo;
    private int abaAtiva = 0;

    // Dados da Aba 1: Relatório Financeiro (HU23)
    private RelatorioFinanceiroResumoDTO resumoFinanceiro;
    private List<RelatorioFinanceiroPeriodoDTO> financeiroPorPeriodo = Collections.emptyList();
    private List<RelatorioFinanceiroMeioPagamentoDTO> financeiroPorMeioPagamento = Collections.emptyList();
    private List<RelatorioFinanceiroLancamentoDTO> lancamentosFinanceiros = Collections.emptyList();

    // Dados da Aba 2: Relatório de Ocupação (HU24)
    private RelatorioOcupacaoResumoDTO resumoOcupacao;
    private List<RelatorioOcupacaoPeriodoDTO> ocupacaoPorPeriodo = Collections.emptyList();
    private List<RelatorioOcupacaoVagaTipoDTO> ocupacaoPorTipoVaga = Collections.emptyList();
    private List<RelatorioOcupacaoFaixaHorariaDTO> ocupacaoPorFaixaHoraria = Collections.emptyList();
    private List<RelatorioOcupacaoDetalheDTO> detalhesOcupacao = Collections.emptyList();

    @PostConstruct
    public void init() {
        this.filtro = new RelatorioFiltroDTO();
        configurarPeriodoMesAtual();
        carregarRelatorios();
    }

    public void aplicarAtalhoHoje() {
        LocalDate hoje = LocalDate.now();
        this.filtro.setDataInicio(hoje);
        this.filtro.setDataFim(hoje);
        this.filtro.setAgrupamento(PeriodoRelatorioTipo.DIA);
        this.atalhoAtivo = "DIA";
        carregarRelatorios();
    }

    public void aplicarAtalhoSemana() {
        LocalDate hoje = LocalDate.now();
        this.filtro.setDataInicio(hoje.minusDays(6));
        this.filtro.setDataFim(hoje);
        this.filtro.setAgrupamento(PeriodoRelatorioTipo.DIA);
        this.atalhoAtivo = "SEMANA";
        carregarRelatorios();
    }

    public void aplicarAtalhoMes() {
        configurarPeriodoMesAtual();
        carregarRelatorios();
    }

    public void aplicarAtalhoAno() {
        LocalDate hoje = LocalDate.now();
        this.filtro.setDataInicio(hoje.withDayOfYear(1));
        this.filtro.setDataFim(hoje);
        this.filtro.setAgrupamento(PeriodoRelatorioTipo.MES);
        this.atalhoAtivo = "ANO";
        carregarRelatorios();
    }

    public void pesquisar() {
        try {
            relatorioService.validarEPrepararFiltro(this.filtro);
            this.atalhoAtivo = "PERSONALIZADO";
            carregarRelatorios();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Relatórios atualizados",
                    "Os indicadores e tabelas foram atualizados para o período selecionado.");
        } catch (IllegalArgumentException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Período inválido", e.getMessage());
        } catch (Exception e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao gerar relatórios", e.getMessage());
        }
    }

    public void limparFiltros() {
        this.filtro = new RelatorioFiltroDTO();
        configurarPeriodoMesAtual();
        carregarRelatorios();
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Filtros restaurados",
                "Exibindo dados consolidados do mês corrente.");
    }

    public StreamedContent getArquivoPdfFinanceiro() {
        try {
            byte[] pdfBytes = relatorioService.gerarPdfFinanceiro(
                    this.filtro,
                    this.resumoFinanceiro,
                    this.financeiroPorPeriodo,
                    this.financeiroPorMeioPagamento,
                    this.lancamentosFinanceiros,
                    obterUsuarioLogado()
            );
            String nomeArquivo = String.format("relatorio-financeiro-%s-a-%s.pdf",
                    this.filtro.getDataInicio(), this.filtro.getDataFim());

            return DefaultStreamedContent.builder()
                    .name(nomeArquivo)
                    .contentType("application/pdf")
                    .stream(() -> new ByteArrayInputStream(pdfBytes))
                    .build();
        } catch (Exception e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha ao exportar PDF Financeiro", e.getMessage());
            return null;
        }
    }

    public StreamedContent getArquivoPdfOcupacao() {
        try {
            byte[] pdfBytes = relatorioService.gerarPdfOcupacao(
                    this.filtro,
                    this.resumoOcupacao,
                    this.ocupacaoPorPeriodo,
                    this.ocupacaoPorTipoVaga,
                    this.ocupacaoPorFaixaHoraria,
                    this.detalhesOcupacao,
                    obterUsuarioLogado()
            );
            String nomeArquivo = String.format("relatorio-ocupacao-%s-a-%s.pdf",
                    this.filtro.getDataInicio(), this.filtro.getDataFim());

            return DefaultStreamedContent.builder()
                    .name(nomeArquivo)
                    .contentType("application/pdf")
                    .stream(() -> new ByteArrayInputStream(pdfBytes))
                    .build();
        } catch (Exception e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha ao exportar PDF de Ocupação", e.getMessage());
            return null;
        }
    }

    public void filtrarPorTipoVaga() {
        this.abaAtiva = 1;
        this.ocupacaoPorPeriodo = relatorioService.buscarOcupacaoPorPeriodo(this.filtro);
        this.resumoOcupacao = relatorioService.calcularResumoOcupacao(this.ocupacaoPorPeriodo);
        this.ocupacaoPorTipoVaga = relatorioService.buscarOcupacaoPorTipoVaga(this.filtro);
        this.ocupacaoPorFaixaHoraria = relatorioService.buscarOcupacaoPorFaixaHoraria(this.filtro);
        this.detalhesOcupacao = relatorioService.buscarDetalhesOcupacao(this.filtro);
    }

    public PeriodoRelatorioTipo[] getTiposAgrupamento() {
        return PeriodoRelatorioTipo.values();
    }

    public VagaTipo[] getTiposVaga() {
        return VagaTipo.values();
    }

    private void configurarPeriodoMesAtual() {
        LocalDate hoje = LocalDate.now();
        this.filtro.setDataInicio(hoje.withDayOfMonth(1));
        this.filtro.setDataFim(hoje);
        this.filtro.setAgrupamento(PeriodoRelatorioTipo.DIA);
        this.atalhoAtivo = "MES";
    }

    private void carregarRelatorios() {
        // Relatório Financeiro (HU23)
        this.financeiroPorPeriodo = relatorioService.buscarFinanceiroPorPeriodo(this.filtro);
        this.resumoFinanceiro = relatorioService.calcularResumoFinanceiro(this.financeiroPorPeriodo);
        this.financeiroPorMeioPagamento = relatorioService.buscarFinanceiroPorMeioPagamento(this.filtro);
        this.lancamentosFinanceiros = relatorioService.buscarLancamentosFinanceiros(this.filtro);

        // Relatório de Ocupação (HU24)
        this.ocupacaoPorPeriodo = relatorioService.buscarOcupacaoPorPeriodo(this.filtro);
        this.resumoOcupacao = relatorioService.calcularResumoOcupacao(this.ocupacaoPorPeriodo);
        this.ocupacaoPorTipoVaga = relatorioService.buscarOcupacaoPorTipoVaga(this.filtro);
        this.ocupacaoPorFaixaHoraria = relatorioService.buscarOcupacaoPorFaixaHoraria(this.filtro);
        this.detalhesOcupacao = relatorioService.buscarDetalhesOcupacao(this.filtro);
    }

    private String obterUsuarioLogado() {
        try {
            if (identity != null && !identity.isAnonymous()) {
                return identity.getPrincipal().getName();
            }
        } catch (Exception ignored) {
        }
        return "Gerente";
    }

    private void adicionarMensagem(FacesMessage.Severity severidade, String resumo, String detalhe) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(severidade, resumo, detalhe));
        }
    }

}
