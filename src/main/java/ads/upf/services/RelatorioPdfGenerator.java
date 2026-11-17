package ads.upf.services;

import ads.upf.model.DTOs.relatorio.*;
import ads.upf.model.enums.PeriodoRelatorioTipo;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class RelatorioPdfGenerator {

    private static final Locale LOCALE_PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Color COLOR_PRIMARY_DARK = new Color(15, 23, 42);    // #0f172a
    private static final Color COLOR_PRIMARY_BLUE = new Color(37, 99, 235);   // #2563eb
    private static final Color COLOR_HEADER_BG = new Color(241, 245, 249);    // #f1f5f9
    private static final Color COLOR_ROW_ALT = new Color(248, 250, 252);      // #f8fafc
    private static final Color COLOR_BORDER = new Color(203, 213, 225);       // #cbd5e1
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);   // #64748b

    private static final Font FONT_BRAND = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
    private static final Font FONT_HEADER_SUB = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(203, 213, 225));
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY_DARK);
    private static final Font FONT_KPI_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXT_MUTED);
    private static final Font FONT_KPI_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY_DARK);
    private static final Font FONT_KPI_SUB = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED);
    private static final Font FONT_TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_PRIMARY_DARK);
    private static final Font FONT_TD = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_PRIMARY_DARK);
    private static final Font FONT_TD_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_PRIMARY_DARK);
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COLOR_TEXT_MUTED);

    public byte[] gerarPdfFinanceiro(
            RelatorioFiltroDTO filtro,
            RelatorioFinanceiroResumoDTO resumo,
            List<RelatorioFinanceiroPeriodoDTO> porPeriodo,
            List<RelatorioFinanceiroMeioPagamentoDTO> porMeioPagamento,
            List<RelatorioFinanceiroLancamentoDTO> lancamentos,
            String usuarioEmissor) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 28f, 28f, 28f, 28f);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            adicionarCabecalhoInstitucional(
                    document,
                    "RELATÓRIO GERENCIAL FINANCEIRO (HU23)",
                    filtro,
                    "Origem da Receita: " + formatarOrigem(filtro.getOrigemReceita()),
                    usuarioEmissor
            );

            // 1. Indicadores Executivos (KPIs)
            adicionarTituloSecao(document, "1. Indicadores Executivos do Período");
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100f);
            kpiTable.setSpacingBefore(4f);
            kpiTable.setSpacingAfter(12f);

            PeriodoRelatorioTipo agrup = filtro.getAgrupamento() != null ? filtro.getAgrupamento() : PeriodoRelatorioTipo.DIA;

            adicionarCardKpi(
                    kpiTable,
                    "RECEITA TOTAL",
                    formatarMoeda(resumo.receitaTotal()),
                    "Rotativo: " + formatarMoeda(resumo.receitaRotativo()) + " (" + resumo.percentualRotativo() + "%)\n"
                            + "Mensalista: " + formatarMoeda(resumo.receitaMensalista()) + " (" + resumo.percentualMensalista() + "%)"
            );
            adicionarCardKpi(
                    kpiTable,
                    "MÉDIA (" + agrup.getDescricao().toUpperCase() + ")",
                    formatarMoeda(resumo.receitaMediaPorPeriodo()),
                    "Transações: " + resumo.totalTransacoes() + "\nTicket Médio: " + formatarMoeda(resumo.ticketMedioGeral())
            );
            adicionarCardKpi(
                    kpiTable,
                    "MAIOR FATURAMENTO",
                    formatarMoeda(resumo.valorMaiorReceita()),
                    "Período: " + resumo.periodoMaiorReceita()
            );
            adicionarCardKpi(
                    kpiTable,
                    "MENOR FATURAMENTO",
                    formatarMoeda(resumo.valorMenorReceita()),
                    "Período: " + resumo.periodoMenorReceita()
            );
            document.add(kpiTable);

            // 2. Desempenho Consolidado por Subperíodo
            adicionarTituloSecao(document, "2. Desempenho Financeiro por Período (" + agrup.getDescricao() + ")");
            PdfPTable tabelaPeriodo = new PdfPTable(new float[]{2.2f, 1.8f, 1.8f, 1.3f, 1.6f, 1.8f});
            tabelaPeriodo.setWidthPercentage(100f);
            tabelaPeriodo.setSpacingBefore(4f);
            tabelaPeriodo.setSpacingAfter(12f);

            adicionarCabecalhoTabela(tabelaPeriodo,
                    "Período", "Receita Rotativo", "Receita Mensalista", "Transações", "Ticket Médio", "Receita Total");

            if (porPeriodo == null || porPeriodo.isEmpty()) {
                adicionarLinhaVazia(tabelaPeriodo, 6, "Nenhum registro financeiro encontrado para os filtros informados.");
            } else {
                int idx = 0;
                for (RelatorioFinanceiroPeriodoDTO item : porPeriodo) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaPeriodo, item.periodoLabel(), FONT_TD_BOLD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaPeriodo, formatarMoeda(item.valorRotativo()) + " (" + item.qtdRotativo() + ")", FONT_TD, Element.ALIGN_RIGHT, bg);
                    adicionarCelula(tabelaPeriodo, formatarMoeda(item.valorMensalista()) + " (" + item.qtdMensalista() + ")", FONT_TD, Element.ALIGN_RIGHT, bg);
                    adicionarCelula(tabelaPeriodo, String.valueOf(item.totalTransacoes()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaPeriodo, formatarMoeda(item.ticketMedio()), FONT_TD, Element.ALIGN_RIGHT, bg);
                    adicionarCelula(tabelaPeriodo, formatarMoeda(item.valorTotal()), FONT_TD_BOLD, Element.ALIGN_RIGHT, bg);
                }
            }
            document.add(tabelaPeriodo);

            // 3. Distribuição por Meio de Pagamento
            adicionarTituloSecao(document, "3. Distribuição por Meio de Pagamento");
            PdfPTable tabelaMeios = new PdfPTable(new float[]{3.5f, 2f, 2.5f, 2f});
            tabelaMeios.setWidthPercentage(100f);
            tabelaMeios.setSpacingBefore(4f);
            tabelaMeios.setSpacingAfter(12f);

            adicionarCabecalhoTabela(tabelaMeios, "Meio de Pagamento", "Quantidade", "Valor Total", "Participação (%)");
            if (porMeioPagamento == null || porMeioPagamento.isEmpty()) {
                adicionarLinhaVazia(tabelaMeios, 4, "Nenhuma transação registrada no período.");
            } else {
                int idx = 0;
                for (RelatorioFinanceiroMeioPagamentoDTO meio : porMeioPagamento) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaMeios, meio.meioPagamento(), FONT_TD_BOLD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaMeios, String.valueOf(meio.quantidade()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaMeios, formatarMoeda(meio.valorTotal()), FONT_TD, Element.ALIGN_RIGHT, bg);
                    adicionarCelula(tabelaMeios, meio.percentual() + "%", FONT_TD, Element.ALIGN_RIGHT, bg);
                }
            }
            document.add(tabelaMeios);

            // 4. Lançamentos Auditáveis do Período
            adicionarTituloSecao(document, "4. Lançamentos Financeiros Auditáveis do Período");
            PdfPTable tabelaLancamentos = new PdfPTable(new float[]{1.8f, 1.8f, 1.4f, 3.0f, 1.8f, 1.6f});
            tabelaLancamentos.setWidthPercentage(100f);
            tabelaLancamentos.setSpacingBefore(4f);
            tabelaLancamentos.setSpacingAfter(10f);

            adicionarCabecalhoTabela(tabelaLancamentos,
                    "Referência", "Data Pagamento", "Origem", "Placa / Cliente", "Meio Pagto.", "Valor Pago");

            if (lancamentos == null || lancamentos.isEmpty()) {
                adicionarLinhaVazia(tabelaLancamentos, 6, "Nenhum lançamento encontrado no período.");
            } else {
                int idx = 0;
                for (RelatorioFinanceiroLancamentoDTO lanc : lancamentos) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaLancamentos, lanc.codigoReferencia(), FONT_TD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaLancamentos, lanc.dataPagamentoFormatada(), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaLancamentos, lanc.origem(), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaLancamentos, lanc.identificacao(), FONT_TD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaLancamentos, lanc.meioPagamento(), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaLancamentos, formatarMoeda(lanc.valor()), FONT_TD_BOLD, Element.ALIGN_RIGHT, bg);
                }
            }
            document.add(tabelaLancamentos);

            adicionarRodapeAuditoria(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar documento PDF do Relatório Financeiro.", e);
        }
    }

    public byte[] gerarPdfOcupacao(
            RelatorioFiltroDTO filtro,
            RelatorioOcupacaoResumoDTO resumo,
            List<RelatorioOcupacaoPeriodoDTO> porPeriodo,
            List<RelatorioOcupacaoVagaTipoDTO> porTipoVaga,
            List<RelatorioOcupacaoFaixaHorariaDTO> porFaixaHoraria,
            List<RelatorioOcupacaoDetalheDTO> detalhes,
            String usuarioEmissor) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 28f, 28f, 28f, 28f);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            String infoExtra = "Modalidade: " + formatarModalidade(filtro.getModalidadeOcupacao())
                    + (filtro.getTipoVaga() != null ? " | Tipo de Vaga: " + filtro.getTipoVaga().name().toUpperCase() : " | Tipo de Vaga: Todas");

            adicionarCabecalhoInstitucional(
                    document,
                    "RELATÓRIO GERENCIAL DE OCUPAÇÃO E FLUXO DE PÁTIO",
                    filtro,
                    infoExtra,
                    usuarioEmissor
            );

            PeriodoRelatorioTipo agrup = filtro.getAgrupamento() != null ? filtro.getAgrupamento() : PeriodoRelatorioTipo.DIA;

            // 1. Indicadores Executivos de Ocupação
            adicionarTituloSecao(document, "1. Indicadores Executivos de Ocupação");
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100f);
            kpiTable.setSpacingBefore(4f);
            kpiTable.setSpacingAfter(12f);

            adicionarCardKpi(
                    kpiTable,
                    "TOTAL DE ENTRADAS",
                    resumo.totalEntradas() + " veículos",
                    "Rotativos: " + resumo.entradasRotativo() + " (" + resumo.percentualRotativo() + "%)\n"
                            + "Mensalistas: " + resumo.entradasMensalista() + " (" + resumo.percentualMensalista() + "%)"
            );
            adicionarCardKpi(
                    kpiTable,
                    "MÉDIA (" + agrup.getDescricao().toUpperCase() + ")",
                    resumo.mediaVeiculosPorPeriodo() + " veíc.",
                    "Tempo Médio: " + resumo.tempoMedioFormatado() + "\nConcluídas: " + resumo.saidasConcluidas()
            );
            adicionarCardKpi(
                    kpiTable,
                    "MAIOR MOVIMENTO",
                    resumo.qtdMaiorMovimento() + " entradas",
                    "Período: " + resumo.periodoMaiorMovimento()
            );
            adicionarCardKpi(
                    kpiTable,
                    "MENOR MOVIMENTO",
                    resumo.qtdMenorMovimento() + " entradas",
                    "Período: " + resumo.periodoMenorMovimento()
            );
            document.add(kpiTable);

            // 2. Evolução de Ocupação por Subperíodo
            adicionarTituloSecao(document, "2. Fluxo de Veículos Estacionados por Período (" + agrup.getDescricao() + ")");
            PdfPTable tabelaPeriodo = new PdfPTable(new float[]{2.4f, 1.5f, 1.5f, 1.5f, 1.5f, 1.6f});
            tabelaPeriodo.setWidthPercentage(100f);
            tabelaPeriodo.setSpacingBefore(4f);
            tabelaPeriodo.setSpacingAfter(12f);

            adicionarCabecalhoTabela(tabelaPeriodo,
                    "Período", "Total Entradas", "Rotativos", "Mensalistas", "Saídas Concluídas", "Tempo Médio");

            if (porPeriodo == null || porPeriodo.isEmpty()) {
                adicionarLinhaVazia(tabelaPeriodo, 6, "Nenhuma movimentação encontrada para os filtros selecionados.");
            } else {
                int idx = 0;
                for (RelatorioOcupacaoPeriodoDTO item : porPeriodo) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaPeriodo, item.periodoLabel(), FONT_TD_BOLD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaPeriodo, String.valueOf(item.totalEntradas()), FONT_TD_BOLD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaPeriodo, String.valueOf(item.entradasRotativo()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaPeriodo, String.valueOf(item.entradasMensalista()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaPeriodo, String.valueOf(item.saidasConcluidas()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaPeriodo, item.tempoMedioFormatado(), FONT_TD, Element.ALIGN_CENTER, bg);
                }
            }
            document.add(tabelaPeriodo);

            // 3. Utilização por Tipo de Vaga e Horários de Movimento
            adicionarTituloSecao(document, "3. Utilização por Categoria de Vaga");
            PdfPTable tabelaVagas = new PdfPTable(new float[]{3.0f, 2.0f, 2.0f, 1.5f, 1.5f});
            tabelaVagas.setWidthPercentage(100f);
            tabelaVagas.setSpacingBefore(4f);
            tabelaVagas.setSpacingAfter(12f);

            adicionarCabecalhoTabela(tabelaVagas,
                    "Categoria da Vaga", "Vagas Cadastradas", "Total de Usos", "Participação (%)", "Tempo Médio");

            if (porTipoVaga == null || porTipoVaga.isEmpty()) {
                adicionarLinhaVazia(tabelaVagas, 5, "Sem dados de categorias de vaga.");
            } else {
                int idx = 0;
                for (RelatorioOcupacaoVagaTipoDTO vt : porTipoVaga) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaVagas, vt.tipoVaga(), FONT_TD_BOLD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaVagas, String.valueOf(vt.totalVagasCadastradas()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaVagas, String.valueOf(vt.totalUtilizacoes()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaVagas, vt.percentualUtilizacao() + "%", FONT_TD, Element.ALIGN_RIGHT, bg);
                    adicionarCelula(tabelaVagas, vt.tempoMedioFormatado(), FONT_TD, Element.ALIGN_CENTER, bg);
                }
            }
            document.add(tabelaVagas);

            // 4. Faixas Horárias de Maior Movimento
            adicionarTituloSecao(document, "4. Concentração de Entradas por Faixa Horária");
            PdfPTable tabelaHorarios = new PdfPTable(new float[]{2.5f, 2.0f, 2.0f, 2.0f, 1.5f});
            tabelaHorarios.setWidthPercentage(100f);
            tabelaHorarios.setSpacingBefore(4f);
            tabelaHorarios.setSpacingAfter(12f);

            adicionarCabecalhoTabela(tabelaHorarios,
                    "Faixa Horária", "Total Entradas", "Rotativos", "Mensalistas", "Concentração (%)");

            if (porFaixaHoraria == null || porFaixaHoraria.isEmpty()) {
                adicionarLinhaVazia(tabelaHorarios, 5, "Nenhuma entrada registrada no período.");
            } else {
                int idx = 0;
                for (RelatorioOcupacaoFaixaHorariaDTO fh : porFaixaHoraria) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaHorarios, fh.faixaHoraria(), FONT_TD_BOLD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaHorarios, String.valueOf(fh.totalEntradas()), FONT_TD_BOLD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaHorarios, String.valueOf(fh.entradasRotativo()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaHorarios, String.valueOf(fh.entradasMensalista()), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaHorarios, fh.percentualMovimento() + "%", FONT_TD, Element.ALIGN_RIGHT, bg);
                }
            }
            document.add(tabelaHorarios);

            // 5. Registro Analítico de Veículos Estacionados
            adicionarTituloSecao(document, "5. Registro Detalhado de Veículos Estacionados no Período");
            PdfPTable tabelaDetalhes = new PdfPTable(new float[]{1.2f, 2.1f, 1.1f, 1.3f, 1.9f, 1.9f, 1.2f, 1.4f});
            tabelaDetalhes.setWidthPercentage(100f);
            tabelaDetalhes.setSpacingBefore(4f);
            tabelaDetalhes.setSpacingAfter(10f);

            adicionarCabecalhoTabela(tabelaDetalhes,
                    "Placa", "Veículo", "Vaga", "Modalidade", "Entrada", "Saída", "Duração", "Status");

            if (detalhes == null || detalhes.isEmpty()) {
                adicionarLinhaVazia(tabelaDetalhes, 8, "Nenhuma permanência registrada no período.");
            } else {
                int idx = 0;
                for (RelatorioOcupacaoDetalheDTO det : detalhes) {
                    Color bg = (idx++ % 2 == 1) ? COLOR_ROW_ALT : Color.WHITE;
                    adicionarCelula(tabelaDetalhes, det.placa(), FONT_TD_BOLD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaDetalhes, det.veiculoDescricao(), FONT_TD, Element.ALIGN_LEFT, bg);
                    adicionarCelula(tabelaDetalhes, det.vagaNome() + " (" + det.tipoVaga() + ")", FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaDetalhes, det.rotativo() ? "Rotativo" : "Mensalista", FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaDetalhes, det.dataEntradaFormatada(), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaDetalhes, det.dataSaidaFormatada(), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaDetalhes, det.duracaoFormatada(), FONT_TD, Element.ALIGN_CENTER, bg);
                    adicionarCelula(tabelaDetalhes, det.status(), FONT_TD, Element.ALIGN_CENTER, bg);
                }
            }
            document.add(tabelaDetalhes);

            adicionarRodapeAuditoria(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar documento PDF do Relatório de Ocupação.", e);
        }
    }

    private void adicionarCabecalhoInstitucional(
            Document document,
            String tituloRelatorio,
            RelatorioFiltroDTO filtro,
            String subfiltroTexto,
            String usuarioEmissor) throws DocumentException {

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100f);
        headerTable.setSpacingAfter(10f);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_PRIMARY_DARK);
        cell.setBorderColor(COLOR_PRIMARY_BLUE);
        cell.setBorderWidthBottom(3f);
        cell.setPadding(12f);

        Paragraph brand = new Paragraph("EASYPARK ERP — GESTÃO DE ESTACIONAMENTO", FONT_BRAND);
        brand.setSpacingAfter(3f);
        cell.addElement(brand);

        Paragraph title = new Paragraph(tituloRelatorio, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(147, 197, 253)));
        title.setSpacingAfter(6f);
        cell.addElement(title);

        String dataIni = filtro.getDataInicio() != null ? filtro.getDataInicio().format(DATE_FORMATTER) : "-";
        String dataFim = filtro.getDataFim() != null ? filtro.getDataFim().format(DATE_FORMATTER) : "-";
        String agrup = filtro.getAgrupamento() != null ? filtro.getAgrupamento().getDescricao() : "Diário";
        String emissor = (usuarioEmissor != null && !usuarioEmissor.isBlank()) ? usuarioEmissor : "Gerente";

        Paragraph meta = new Paragraph(
                "Período Analisado: " + dataIni + " a " + dataFim
                        + "   |   Agrupamento: " + agrup
                        + "   |   " + subfiltroTexto
                        + "\nEmitido em: " + LocalDateTime.now().format(DATETIME_FORMATTER)
                        + "   |   Responsável: " + emissor,
                FONT_HEADER_SUB
        );
        cell.addElement(meta);

        headerTable.addCell(cell);
        document.add(headerTable);
    }

    private void adicionarTituloSecao(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, FONT_SECTION);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(2f);
        document.add(p);
    }

    private void adicionarCardKpi(PdfPTable table, String label, String valor, String subtexto) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_HEADER_BG);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(1f);
        cell.setPadding(8f);

        Paragraph pLabel = new Paragraph(label, FONT_KPI_LABEL);
        pLabel.setSpacingAfter(3f);
        cell.addElement(pLabel);

        Paragraph pValor = new Paragraph(valor != null ? valor : "-", FONT_KPI_VALUE);
        pValor.setSpacingAfter(3f);
        cell.addElement(pValor);

        Paragraph pSub = new Paragraph(subtexto != null ? subtexto : "", FONT_KPI_SUB);
        cell.addElement(pSub);

        table.addCell(cell);
    }

    private void adicionarCabecalhoTabela(PdfPTable table, String... colunas) {
        for (String col : colunas) {
            PdfPCell th = new PdfPCell(new Phrase(col, FONT_TH));
            th.setBackgroundColor(COLOR_HEADER_BG);
            th.setBorderColor(COLOR_BORDER);
            th.setPadding(6f);
            th.setHorizontalAlignment(Element.ALIGN_CENTER);
            th.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(th);
        }
        table.setHeaderRows(1);
    }

    private void adicionarCelula(PdfPTable table, String texto, Font font, int align, Color bg) {
        PdfPCell td = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        td.setBackgroundColor(bg);
        td.setBorderColor(COLOR_BORDER);
        td.setPadding(5f);
        td.setHorizontalAlignment(align);
        td.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(td);
    }

    private void adicionarLinhaVazia(PdfPTable table, int colSpan, String mensagem) {
        PdfPCell empty = new PdfPCell(new Phrase(mensagem, FONT_FOOTER));
        empty.setColspan(colSpan);
        empty.setBorderColor(COLOR_BORDER);
        empty.setPadding(8f);
        empty.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(empty);
    }

    private void adicionarRodapeAuditoria(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Documento gerado eletronicamente pelo módulo de Gestão e Auditoria do EasyPark ERP.",
                FONT_FOOTER
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10f);
        document.add(footer);
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        return NumberFormat.getCurrencyInstance(LOCALE_PT_BR).format(v);
    }

    private String formatarOrigem(String origem) {
        if ("ROTATIVO".equalsIgnoreCase(origem)) return "Apenas Rotativo";
        if ("MENSALISTA".equalsIgnoreCase(origem)) return "Apenas Mensalistas";
        return "Todas (Rotativo + Mensalistas)";
    }

    private String formatarModalidade(String mod) {
        if ("ROTATIVO".equalsIgnoreCase(mod)) return "Apenas Rotativo";
        if ("MENSALISTA".equalsIgnoreCase(mod)) return "Apenas Mensalistas";
        return "Todas (Rotativo + Mensalistas)";
    }
}
