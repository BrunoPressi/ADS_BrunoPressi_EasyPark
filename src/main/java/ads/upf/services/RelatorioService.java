package ads.upf.services;

import ads.upf.model.DTOs.relatorio.*;
import ads.upf.model.enums.PeriodoRelatorioTipo;
import ads.upf.repositories.RelatorioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;
    private final RelatorioPdfGenerator relatorioPdfGenerator;

    @Inject
    public RelatorioService(
            RelatorioRepository relatorioRepository,
            RelatorioPdfGenerator relatorioPdfGenerator) {
        this.relatorioRepository = relatorioRepository;
        this.relatorioPdfGenerator = relatorioPdfGenerator;
    }

    public void validarEPrepararFiltro(RelatorioFiltroDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException("O filtro de relatório é obrigatório.");
        }
        if (filtro.getDataInicio() == null) {
            filtro.setDataInicio(LocalDate.now().withDayOfMonth(1));
        }
        if (filtro.getDataFim() == null) {
            filtro.setDataFim(LocalDate.now());
        }
        if (filtro.getDataFim().isBefore(filtro.getDataInicio())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }
        if (filtro.getAgrupamento() == null) {
            filtro.setAgrupamento(PeriodoRelatorioTipo.DIA);
        }
        if (filtro.getOrigemReceita() == null || filtro.getOrigemReceita().isBlank()) {
            filtro.setOrigemReceita("TODAS");
        }
        if (filtro.getModalidadeOcupacao() == null || filtro.getModalidadeOcupacao().isBlank()) {
            filtro.setModalidadeOcupacao("TODAS");
        }
    }

    public List<RelatorioFinanceiroPeriodoDTO> buscarFinanceiroPorPeriodo(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarFinanceiroPorPeriodo(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getAgrupamento(),
                filtro.getOrigemReceita()
        );
    }

    public List<RelatorioFinanceiroMeioPagamentoDTO> buscarFinanceiroPorMeioPagamento(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarFinanceiroPorMeioPagamento(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getOrigemReceita()
        );
    }

    public List<RelatorioFinanceiroLancamentoDTO> buscarLancamentosFinanceiros(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarLancamentosFinanceiros(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getOrigemReceita()
        );
    }

    /**
     * Calcula os indicadores executivos do Relatório Financeiro (HU23).
     * Conforme Opção A validada, o maior e o menor faturamento consideram apenas os
     * subperíodos em que houve operação registrada.
     */
    public RelatorioFinanceiroResumoDTO calcularResumoFinanceiro(List<RelatorioFinanceiroPeriodoDTO> seriePeriodo) {
        if (seriePeriodo == null || seriePeriodo.isEmpty()) {
            return new RelatorioFinanceiroResumoDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "Sem registros",
                    BigDecimal.ZERO,
                    "Sem registros",
                    BigDecimal.ZERO,
                    0L,
                    0L,
                    0L,
                    BigDecimal.ZERO
            );
        }

        BigDecimal receitaTotal = BigDecimal.ZERO;
        BigDecimal receitaRotativo = BigDecimal.ZERO;
        BigDecimal receitaMensalista = BigDecimal.ZERO;
        long totalTransacoes = 0L;
        long transacoesRotativo = 0L;
        long transacoesMensalista = 0L;

        for (RelatorioFinanceiroPeriodoDTO item : seriePeriodo) {
            receitaTotal = receitaTotal.add(nonNull(item.valorTotal()));
            receitaRotativo = receitaRotativo.add(nonNull(item.valorRotativo()));
            receitaMensalista = receitaMensalista.add(nonNull(item.valorMensalista()));
            totalTransacoes += item.totalTransacoes();
            transacoesRotativo += item.qtdRotativo();
            transacoesMensalista += item.qtdMensalista();
        }

        BigDecimal percentualRotativo = calcularPercentual(receitaRotativo, receitaTotal);
        BigDecimal percentualMensalista = calcularPercentual(receitaMensalista, receitaTotal);

        BigDecimal mediaPorPeriodo = receitaTotal.divide(
                BigDecimal.valueOf(seriePeriodo.size()), 2, RoundingMode.HALF_UP);

        BigDecimal ticketMedio = totalTransacoes > 0
                ? receitaTotal.divide(BigDecimal.valueOf(totalTransacoes), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        RelatorioFinanceiroPeriodoDTO maior = seriePeriodo.stream()
                .max(Comparator.comparing(i -> nonNull(i.valorTotal())))
                .orElse(seriePeriodo.getFirst());

        RelatorioFinanceiroPeriodoDTO menor = seriePeriodo.stream()
                .min(Comparator.comparing(i -> nonNull(i.valorTotal())))
                .orElse(seriePeriodo.getFirst());

        return new RelatorioFinanceiroResumoDTO(
                receitaTotal.setScale(2, RoundingMode.HALF_UP),
                receitaRotativo.setScale(2, RoundingMode.HALF_UP),
                receitaMensalista.setScale(2, RoundingMode.HALF_UP),
                percentualRotativo,
                percentualMensalista,
                mediaPorPeriodo,
                maior.periodoLabel(),
                nonNull(maior.valorTotal()).setScale(2, RoundingMode.HALF_UP),
                menor.periodoLabel(),
                nonNull(menor.valorTotal()).setScale(2, RoundingMode.HALF_UP),
                totalTransacoes,
                transacoesRotativo,
                transacoesMensalista,
                ticketMedio
        );
    }

    public List<RelatorioOcupacaoPeriodoDTO> buscarOcupacaoPorPeriodo(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarOcupacaoPorPeriodo(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getAgrupamento(),
                filtro.getModalidadeOcupacao(),
                filtro.getTipoVaga()
        );
    }

    public List<RelatorioOcupacaoVagaTipoDTO> buscarOcupacaoPorTipoVaga(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarOcupacaoPorTipoVaga(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getModalidadeOcupacao()
        );
    }

    public List<RelatorioOcupacaoFaixaHorariaDTO> buscarOcupacaoPorFaixaHoraria(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarOcupacaoPorFaixaHoraria(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getModalidadeOcupacao(),
                filtro.getTipoVaga()
        );
    }

    public List<RelatorioOcupacaoDetalheDTO> buscarDetalhesOcupacao(RelatorioFiltroDTO filtro) {
        validarEPrepararFiltro(filtro);
        return relatorioRepository.buscarDetalhesOcupacao(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getModalidadeOcupacao(),
                filtro.getTipoVaga()
        );
    }

    /**
     * Calcula os indicadores executivos do Relatório de Ocupação (HU24).
     * Conforme Opção A validada, o maior e o menor movimento consideram apenas os
     * subperíodos em que houve operação registrada.
     */
    public RelatorioOcupacaoResumoDTO calcularResumoOcupacao(List<RelatorioOcupacaoPeriodoDTO> seriePeriodo) {
        if (seriePeriodo == null || seriePeriodo.isEmpty()) {
            return new RelatorioOcupacaoResumoDTO(
                    0L,
                    0L,
                    0L,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L,
                    0L,
                    BigDecimal.ZERO,
                    "Sem registros",
                    0L,
                    "Sem registros",
                    0L,
                    0L,
                    "00h 00m"
            );
        }

        long totalEntradas = 0L;
        long entradasRotativo = 0L;
        long entradasMensalista = 0L;
        long saidasConcluidas = 0L;
        long somaMinutosPonderados = 0L;

        for (RelatorioOcupacaoPeriodoDTO item : seriePeriodo) {
            totalEntradas += item.totalEntradas();
            entradasRotativo += item.entradasRotativo();
            entradasMensalista += item.entradasMensalista();
            saidasConcluidas += item.saidasConcluidas();
            somaMinutosPonderados += item.tempoMedioMinutos() * item.saidasConcluidas();
        }

        long emAndamento = Math.max(0L, totalEntradas - saidasConcluidas);
        BigDecimal totalBd = BigDecimal.valueOf(totalEntradas);
        BigDecimal percentualRotativo = calcularPercentual(BigDecimal.valueOf(entradasRotativo), totalBd);
        BigDecimal percentualMensalista = calcularPercentual(BigDecimal.valueOf(entradasMensalista), totalBd);

        BigDecimal mediaVeiculos = totalBd.divide(
                BigDecimal.valueOf(seriePeriodo.size()), 1, RoundingMode.HALF_UP);

        RelatorioOcupacaoPeriodoDTO maior = seriePeriodo.stream()
                .max(Comparator.comparingLong(RelatorioOcupacaoPeriodoDTO::totalEntradas))
                .orElse(seriePeriodo.getFirst());

        RelatorioOcupacaoPeriodoDTO menor = seriePeriodo.stream()
                .min(Comparator.comparingLong(RelatorioOcupacaoPeriodoDTO::totalEntradas))
                .orElse(seriePeriodo.getFirst());

        long tempoMedioGlobalMin = saidasConcluidas > 0
                ? Math.round((double) somaMinutosPonderados / saidasConcluidas)
                : 0L;

        return new RelatorioOcupacaoResumoDTO(
                totalEntradas,
                entradasRotativo,
                entradasMensalista,
                percentualRotativo,
                percentualMensalista,
                saidasConcluidas,
                emAndamento,
                mediaVeiculos,
                maior.periodoLabel(),
                maior.totalEntradas(),
                menor.periodoLabel(),
                menor.totalEntradas(),
                tempoMedioGlobalMin,
                RelatorioRepository.formatarMinutos(tempoMedioGlobalMin)
        );
    }

    public byte[] gerarPdfFinanceiro(
            RelatorioFiltroDTO filtro,
            RelatorioFinanceiroResumoDTO resumo,
            List<RelatorioFinanceiroPeriodoDTO> porPeriodo,
            List<RelatorioFinanceiroMeioPagamentoDTO> porMeioPagamento,
            List<RelatorioFinanceiroLancamentoDTO> lancamentos,
            String usuarioEmissor) {
        validarEPrepararFiltro(filtro);
        return relatorioPdfGenerator.gerarPdfFinanceiro(
                filtro, resumo, porPeriodo, porMeioPagamento, lancamentos, usuarioEmissor);
    }

    public byte[] gerarPdfOcupacao(
            RelatorioFiltroDTO filtro,
            RelatorioOcupacaoResumoDTO resumo,
            List<RelatorioOcupacaoPeriodoDTO> porPeriodo,
            List<RelatorioOcupacaoVagaTipoDTO> porTipoVaga,
            List<RelatorioOcupacaoFaixaHorariaDTO> porFaixaHoraria,
            List<RelatorioOcupacaoDetalheDTO> detalhes,
            String usuarioEmissor) {
        validarEPrepararFiltro(filtro);
        return relatorioPdfGenerator.gerarPdfOcupacao(
                filtro, resumo, porPeriodo, porTipoVaga, porFaixaHoraria, detalhes, usuarioEmissor);
    }

    private BigDecimal nonNull(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal calcularPercentual(BigDecimal parte, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0 || parte == null) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }

}
