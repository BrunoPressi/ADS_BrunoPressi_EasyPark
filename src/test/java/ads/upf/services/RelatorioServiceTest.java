package ads.upf.services;

import ads.upf.model.DTOs.relatorio.*;
import ads.upf.model.enums.PeriodoRelatorioTipo;
import ads.upf.model.enums.VagaTipo;
import ads.upf.repositories.RelatorioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioService - Suíte de Testes Unitários (RF10 / UC11)")
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    private RelatorioPdfGenerator pdfGenerator;
    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        pdfGenerator = new RelatorioPdfGenerator();
        relatorioService = new RelatorioService(relatorioRepository, pdfGenerator);
    }

    @Test
    @DisplayName("Deve rejeitar filtro nulo ou data final anterior à data inicial")
    void deveRejeitarFiltroComDatasInvalidas() {
        assertThatThrownBy(() -> relatorioService.validarEPrepararFiltro(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obrigatório");

        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO();
        filtro.setDataInicio(LocalDate.of(2026, 11, 20));
        filtro.setDataFim(LocalDate.of(2026, 11, 10));

        assertThatThrownBy(() -> relatorioService.validarEPrepararFiltro(filtro))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data final não pode ser anterior");
    }

    @Test
    @DisplayName("Deve preencher valores padrão quando campos opcionais do filtro forem nulos")
    void devePreencherValoresPadraoNoFiltro() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO();
        filtro.setDataInicio(null);
        filtro.setDataFim(null);
        filtro.setAgrupamento(null);
        filtro.setOrigemReceita(null);
        filtro.setModalidadeOcupacao(" ");

        relatorioService.validarEPrepararFiltro(filtro);

        assertThat(filtro.getDataInicio()).isEqualTo(LocalDate.now().withDayOfMonth(1));
        assertThat(filtro.getDataFim()).isEqualTo(LocalDate.now());
        assertThat(filtro.getAgrupamento()).isEqualTo(PeriodoRelatorioTipo.DIA);
        assertThat(filtro.getOrigemReceita()).isEqualTo("TODAS");
        assertThat(filtro.getModalidadeOcupacao()).isEqualTo("TODAS");
    }

    @Test
    @DisplayName("HU23 - Deve calcular corretamente o resumo financeiro (total, média, maior e menor período com movimento)")
    void deveCalcularResumoFinanceiroCorretamente() {
        List<RelatorioFinanceiroPeriodoDTO> serie = List.of(
                new RelatorioFinanceiroPeriodoDTO(
                        "16/11/2026", 3L, new BigDecimal("420.00"),
                        1L, new BigDecimal("180.00"), 4L, new BigDecimal("600.00"), new BigDecimal("150.00")
                ),
                new RelatorioFinanceiroPeriodoDTO(
                        "15/11/2026", 2L, new BigDecimal("210.00"),
                        0L, BigDecimal.ZERO, 2L, new BigDecimal("210.00"), new BigDecimal("105.00")
                ),
                new RelatorioFinanceiroPeriodoDTO(
                        "14/11/2026", 1L, new BigDecimal("90.00"),
                        1L, new BigDecimal("180.00"), 2L, new BigDecimal("270.00"), new BigDecimal("135.00")
                )
        );

        RelatorioFinanceiroResumoDTO resumo = relatorioService.calcularResumoFinanceiro(serie);

        assertThat(resumo.receitaTotal()).isEqualByComparingTo("1080.00");
        assertThat(resumo.receitaRotativo()).isEqualByComparingTo("720.00");
        assertThat(resumo.receitaMensalista()).isEqualByComparingTo("360.00");
        assertThat(resumo.percentualRotativo()).isEqualByComparingTo("66.7");
        assertThat(resumo.percentualMensalista()).isEqualByComparingTo("33.3");
        assertThat(resumo.receitaMediaPorPeriodo()).isEqualByComparingTo("360.00");
        assertThat(resumo.periodoMaiorReceita()).isEqualTo("16/11/2026");
        assertThat(resumo.valorMaiorReceita()).isEqualByComparingTo("600.00");
        assertThat(resumo.periodoMenorReceita()).isEqualTo("15/11/2026");
        assertThat(resumo.valorMenorReceita()).isEqualByComparingTo("210.00");
        assertThat(resumo.totalTransacoes()).isEqualTo(8L);
        assertThat(resumo.ticketMedioGeral()).isEqualByComparingTo("135.00");
    }

    @Test
    @DisplayName("HU23 - Deve retornar resumo financeiro zerado quando não houver registros no período")
    void deveRetornarResumoFinanceiroZeradoQuandoListaVazia() {
        RelatorioFinanceiroResumoDTO resumo = relatorioService.calcularResumoFinanceiro(Collections.emptyList());

        assertThat(resumo.receitaTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.receitaMediaPorPeriodo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.periodoMaiorReceita()).isEqualTo("Sem registros");
        assertThat(resumo.periodoMenorReceita()).isEqualTo("Sem registros");
        assertThat(resumo.totalTransacoes()).isZero();
    }

    @Test
    @DisplayName("HU24 - Deve calcular corretamente o resumo de ocupação (total, média, maior e menor movimento e tempo médio)")
    void deveCalcularResumoOcupacaoCorretamente() {
        List<RelatorioOcupacaoPeriodoDTO> serie = List.of(
                new RelatorioOcupacaoPeriodoDTO("16/11/2026", 10L, 7L, 3L, 8L, 90L, "01h 30m"),
                new RelatorioOcupacaoPeriodoDTO("15/11/2026", 4L, 3L, 1L, 4L, 60L, "01h 00m"),
                new RelatorioOcupacaoPeriodoDTO("14/11/2026", 7L, 5L, 2L, 6L, 120L, "02h 00m")
        );

        RelatorioOcupacaoResumoDTO resumo = relatorioService.calcularResumoOcupacao(serie);

        assertThat(resumo.totalEntradas()).isEqualTo(21L);
        assertThat(resumo.entradasRotativo()).isEqualTo(15L);
        assertThat(resumo.entradasMensalista()).isEqualTo(6L);
        assertThat(resumo.saidasConcluidas()).isEqualTo(18L);
        assertThat(resumo.emAndamentoNoPeriodo()).isEqualTo(3L);
        assertThat(resumo.mediaVeiculosPorPeriodo()).isEqualByComparingTo("7.0");
        assertThat(resumo.periodoMaiorMovimento()).isEqualTo("16/11/2026");
        assertThat(resumo.qtdMaiorMovimento()).isEqualTo(10L);
        assertThat(resumo.periodoMenorMovimento()).isEqualTo("15/11/2026");
        assertThat(resumo.qtdMenorMovimento()).isEqualTo(4L);
        // Média ponderada: (8*90 + 4*60 + 6*120) / 18 = (720 + 240 + 720) / 18 = 1680 / 18 = 93 min -> 01h 33m
        assertThat(resumo.tempoMedioMinutos()).isEqualTo(93L);
        assertThat(resumo.tempoMedioFormatado()).isEqualTo("01h 33m");
    }

    @Test
    @DisplayName("HU24 - Deve retornar resumo de ocupação zerado quando não houver registros")
    void deveRetornarResumoOcupacaoZeradoQuandoListaVazia() {
        RelatorioOcupacaoResumoDTO resumo = relatorioService.calcularResumoOcupacao(Collections.emptyList());

        assertThat(resumo.totalEntradas()).isZero();
        assertThat(resumo.mediaVeiculosPorPeriodo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.periodoMaiorMovimento()).isEqualTo("Sem registros");
        assertThat(resumo.periodoMenorMovimento()).isEqualTo("Sem registros");
        assertThat(resumo.tempoMedioFormatado()).isEqualTo("00h 00m");
    }

    @Test
    @DisplayName("Deve delegar consultas parametrizadas ao RelatorioRepository")
    void deveDelegarConsultasAoRepositorio() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 16),
                PeriodoRelatorioTipo.SEMANA,
                "ROTATIVO",
                "MENSALISTA",
                VagaTipo.comum
        );

        when(relatorioRepository.buscarFinanceiroPorPeriodo(
                filtro.getDataInicio(), filtro.getDataFim(), PeriodoRelatorioTipo.SEMANA, "ROTATIVO"))
                .thenReturn(Collections.emptyList());
        when(relatorioRepository.buscarOcupacaoPorPeriodo(
                filtro.getDataInicio(), filtro.getDataFim(), PeriodoRelatorioTipo.SEMANA, "MENSALISTA", VagaTipo.comum))
                .thenReturn(Collections.emptyList());

        assertThat(relatorioService.buscarFinanceiroPorPeriodo(filtro)).isEmpty();
        assertThat(relatorioService.buscarOcupacaoPorPeriodo(filtro)).isEmpty();

        verify(relatorioRepository).buscarFinanceiroPorPeriodo(
                filtro.getDataInicio(), filtro.getDataFim(), PeriodoRelatorioTipo.SEMANA, "ROTATIVO");
        verify(relatorioRepository).buscarOcupacaoPorPeriodo(
                filtro.getDataInicio(), filtro.getDataFim(), PeriodoRelatorioTipo.SEMANA, "MENSALISTA", VagaTipo.comum);
    }

    @Test
    @DisplayName("HU23 e HU24 - Deve gerar arquivos PDF válidos para os relatórios Financeiro e de Ocupação")
    void deveGerarArquivosPdfValidos() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 16),
                PeriodoRelatorioTipo.DIA,
                "TODAS",
                "TODAS",
                null
        );

        List<RelatorioFinanceiroPeriodoDTO> serieFin = List.of(
                new RelatorioFinanceiroPeriodoDTO(
                        "16/11/2026", 2L, new BigDecimal("280.00"),
                        1L, new BigDecimal("180.00"), 3L, new BigDecimal("460.00"), new BigDecimal("153.33")
                )
        );
        RelatorioFinanceiroResumoDTO resumoFin = relatorioService.calcularResumoFinanceiro(serieFin);
        List<RelatorioFinanceiroMeioPagamentoDTO> meios = List.of(
                new RelatorioFinanceiroMeioPagamentoDTO("Pix", 2L, new BigDecimal("320.00"), new BigDecimal("69.6"))
        );
        List<RelatorioFinanceiroLancamentoDTO> lancamentos = List.of(
                new RelatorioFinanceiroLancamentoDTO("PAG #705", "16/11/2026 08:45", "Rotativo", "ROT1006 (Yaris)", "Pix", new BigDecimal("105.00"))
        );

        byte[] pdfFinanceiro = relatorioService.gerarPdfFinanceiro(
                filtro, resumoFin, serieFin, meios, lancamentos, "John@email.com");

        assertThat(pdfFinanceiro).isNotEmpty();
        String cabecalhoFin = new String(pdfFinanceiro, 0, 5, StandardCharsets.US_ASCII);
        assertThat(cabecalhoFin).isEqualTo("%PDF-");

        List<RelatorioOcupacaoPeriodoDTO> serieOcup = List.of(
                new RelatorioOcupacaoPeriodoDTO("16/11/2026", 5L, 4L, 1L, 4L, 75L, "01h 15m")
        );
        RelatorioOcupacaoResumoDTO resumoOcup = relatorioService.calcularResumoOcupacao(serieOcup);
        List<RelatorioOcupacaoVagaTipoDTO> vagas = List.of(
                new RelatorioOcupacaoVagaTipoDTO("Comum", 15L, 4L, new BigDecimal("80.0"), "01h 15m")
        );
        List<RelatorioOcupacaoFaixaHorariaDTO> horarios = List.of(
                new RelatorioOcupacaoFaixaHorariaDTO("08:00 - 08:59", 3L, 3L, 0L, new BigDecimal("60.0"))
        );
        List<RelatorioOcupacaoDetalheDTO> detalhes = List.of(
                new RelatorioOcupacaoDetalheDTO(607L, "ROT1006", "Toyota Yaris", "A01", "Comum", true,
                        "16/11/2026 07:30", "16/11/2026 08:45", "01h 15m", "Concluída", new BigDecimal("105.00"))
        );

        byte[] pdfOcupacao = relatorioService.gerarPdfOcupacao(
                filtro, resumoOcup, serieOcup, vagas, horarios, detalhes, "John@email.com");

        assertThat(pdfOcupacao).isNotEmpty();
        String cabecalhoOcup = new String(pdfOcupacao, 0, 5, StandardCharsets.US_ASCII);
        assertThat(cabecalhoOcup).isEqualTo("%PDF-");
    }

}
