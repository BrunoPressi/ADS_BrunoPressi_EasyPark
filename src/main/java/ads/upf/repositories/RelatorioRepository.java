package ads.upf.repositories;

import ads.upf.model.DTOs.relatorio.*;
import ads.upf.model.enums.PeriodoRelatorioTipo;
import ads.upf.model.enums.VagaTipo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RelatorioRepository {

    @Inject
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public List<RelatorioFinanceiroPeriodoDTO> buscarFinanceiroPorPeriodo(
            LocalDate dataInicio,
            LocalDate dataFim,
            PeriodoRelatorioTipo agrupamento,
            String origemReceita) {

        PeriodoRelatorioTipo tipo = agrupamento != null ? agrupamento : PeriodoRelatorioTipo.DIA;
        String unit = tipo.getPgTruncUnit();
        String format = tipo.getPgDateFormat();
        String origem = normalizarFiltro(origemReceita);

        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            WITH movimentos AS (
                SELECT
                    p.datapagamento AS data_ref,
                    'ROTATIVO' AS origem,
                    p.valor AS valor
                FROM pagamentos p
                WHERE p.status = 'pago'
                  AND p.datapagamento IS NOT NULL
                  AND p.datapagamento BETWEEN :inicioTs AND :fimTs
                UNION ALL
                SELECT
                    par.datapagamento::timestamp AS data_ref,
                    'MENSALISTA' AS origem,
                    par.valor AS valor
                FROM parcelas par
                WHERE par.status = 'paga'
                  AND par.datapagamento IS NOT NULL
                  AND par.datapagamento BETWEEN :inicioDate AND :fimDate
            )
            SELECT
                TO_CHAR(DATE_TRUNC('%s', m.data_ref), '%s') AS periodo_label,
                COUNT(*) FILTER (WHERE m.origem = 'ROTATIVO') AS qtd_rotativo,
                COALESCE(SUM(m.valor) FILTER (WHERE m.origem = 'ROTATIVO'), 0) AS valor_rotativo,
                COUNT(*) FILTER (WHERE m.origem = 'MENSALISTA') AS qtd_mensalista,
                COALESCE(SUM(m.valor) FILTER (WHERE m.origem = 'MENSALISTA'), 0) AS valor_mensalista,
                COUNT(*) AS total_transacoes,
                COALESCE(SUM(m.valor), 0) AS valor_total,
                COALESCE(ROUND(AVG(m.valor), 2), 0) AS ticket_medio
            FROM movimentos m
            WHERE (:origem = 'TODAS' OR m.origem = :origem)
            GROUP BY DATE_TRUNC('%s', m.data_ref)
            ORDER BY DATE_TRUNC('%s', m.data_ref) DESC;
        """.formatted(unit, format, unit, unit);

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("inicioDate", dataInicio)
                .setParameter("fimDate", dataFim)
                .setParameter("origem", origem)
                .getResultList();

        List<RelatorioFinanceiroPeriodoDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            lista.add(new RelatorioFinanceiroPeriodoDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    toBigDecimal(row[2]),
                    ((Number) row[3]).longValue(),
                    toBigDecimal(row[4]),
                    ((Number) row[5]).longValue(),
                    toBigDecimal(row[6]),
                    toBigDecimal(row[7])
            ));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public List<RelatorioFinanceiroMeioPagamentoDTO> buscarFinanceiroPorMeioPagamento(
            LocalDate dataInicio,
            LocalDate dataFim,
            String origemReceita) {

        String origem = normalizarFiltro(origemReceita);
        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            WITH movimentos AS (
                SELECT
                    COALESCE(p.meiopagamento, 'UNDEFINED') AS meio,
                    'ROTATIVO' AS origem,
                    p.valor AS valor
                FROM pagamentos p
                WHERE p.status = 'pago'
                  AND p.datapagamento IS NOT NULL
                  AND p.datapagamento BETWEEN :inicioTs AND :fimTs
                UNION ALL
                SELECT
                    COALESCE(par.meiopagamento, 'UNDEFINED') AS meio,
                    'MENSALISTA' AS origem,
                    par.valor AS valor
                FROM parcelas par
                WHERE par.status = 'paga'
                  AND par.datapagamento IS NOT NULL
                  AND par.datapagamento BETWEEN :inicioDate AND :fimDate
            ),
            agrupado AS (
                SELECT
                    CASE m.meio
                        WHEN 'PIX' THEN 'Pix'
                        WHEN 'CREDIT_CARD' THEN 'Cartão de Crédito'
                        WHEN 'DEBIT_CARD' THEN 'Cartão de Débito'
                        WHEN 'DINHEIRO' THEN 'Dinheiro'
                        WHEN 'BOLETO' THEN 'Boleto'
                        ELSE 'Outros / Indefinido'
                    END AS meio_label,
                    COUNT(*) AS qtd,
                    COALESCE(SUM(m.valor), 0) AS total
                FROM movimentos m
                WHERE (:origem = 'TODAS' OR m.origem = :origem)
                GROUP BY meio_label
            )
            SELECT
                a.meio_label,
                a.qtd,
                a.total,
                COALESCE(ROUND((a.total / NULLIF(SUM(a.total) OVER (), 0)) * 100, 2), 0) AS percentual
            FROM agrupado a
            ORDER BY a.total DESC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("inicioDate", dataInicio)
                .setParameter("fimDate", dataFim)
                .setParameter("origem", origem)
                .getResultList();

        List<RelatorioFinanceiroMeioPagamentoDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            lista.add(new RelatorioFinanceiroMeioPagamentoDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    toBigDecimal(row[2]),
                    toBigDecimal(row[3])
            ));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public List<RelatorioFinanceiroLancamentoDTO> buscarLancamentosFinanceiros(
            LocalDate dataInicio,
            LocalDate dataFim,
            String origemReceita) {

        String origem = normalizarFiltro(origemReceita);
        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            WITH lancamentos AS (
                SELECT
                    p.datapagamento AS data_ordem,
                    'PAG #' || p.id AS codigo_ref,
                    TO_CHAR(p.datapagamento, 'DD/MM/YYYY HH24:MI') AS data_fmt,
                    'ROTATIVO' AS origem_filtro,
                    'Rotativo' AS origem_label,
                    COALESCE(v.placa, 'Avulso') ||
                        CASE WHEN v.modelo IS NOT NULL AND v.modelo <> '' THEN ' (' || v.modelo || ')' ELSE '' END AS identificacao,
                    CASE COALESCE(p.meiopagamento, 'UNDEFINED')
                        WHEN 'PIX' THEN 'Pix'
                        WHEN 'CREDIT_CARD' THEN 'Cartão de Crédito'
                        WHEN 'DEBIT_CARD' THEN 'Cartão de Débito'
                        WHEN 'DINHEIRO' THEN 'Dinheiro'
                        ELSE 'Não informado'
                    END AS meio_label,
                    p.valor AS valor
                FROM pagamentos p
                LEFT JOIN permanencias perm ON perm.pagamento_id = p.id OR p.permanencia_id = perm.id
                LEFT JOIN veiculos v ON perm.veiculo_id = v.id
                WHERE p.status = 'pago'
                  AND p.datapagamento IS NOT NULL
                  AND p.datapagamento BETWEEN :inicioTs AND :fimTs

                UNION ALL

                SELECT
                    par.datapagamento::timestamp AS data_ordem,
                    'PARC #' || par.numeroparcela || ' (Contr. #' || c.id || ')' AS codigo_ref,
                    TO_CHAR(par.datapagamento, 'DD/MM/YYYY') AS data_fmt,
                    'MENSALISTA' AS origem_filtro,
                    'Mensalista' AS origem_label,
                    COALESCE(u.nomecompleto, 'Cliente') ||
                        CASE WHEN v.placa IS NOT NULL THEN ' (' || v.placa || ')' ELSE '' END AS identificacao,
                    CASE COALESCE(par.meiopagamento, 'UNDEFINED')
                        WHEN 'PIX' THEN 'Pix'
                        WHEN 'CREDIT_CARD' THEN 'Cartão de Crédito'
                        WHEN 'DEBIT_CARD' THEN 'Cartão de Débito'
                        WHEN 'BOLETO' THEN 'Boleto'
                        ELSE 'Indefinido'
                    END AS meio_label,
                    par.valor AS valor
                FROM parcelas par
                JOIN contratos c ON par.contrato_id = c.id
                LEFT JOIN clientes cli ON c.cliente_id = cli.usuarioid
                LEFT JOIN usuarios u ON cli.usuarioid = u.id
                LEFT JOIN veiculos v ON c.veiculo_id = v.id
                WHERE par.status = 'paga'
                  AND par.datapagamento IS NOT NULL
                  AND par.datapagamento BETWEEN :inicioDate AND :fimDate
            )
            SELECT
                l.codigo_ref,
                l.data_fmt,
                l.origem_label,
                l.identificacao,
                l.meio_label,
                l.valor
            FROM lancamentos l
            WHERE (:origem = 'TODAS' OR l.origem_filtro = :origem)
            ORDER BY l.data_ordem DESC, l.codigo_ref DESC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("inicioDate", dataInicio)
                .setParameter("fimDate", dataFim)
                .setParameter("origem", origem)
                .getResultList();

        List<RelatorioFinanceiroLancamentoDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            lista.add(new RelatorioFinanceiroLancamentoDTO(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    toBigDecimal(row[5])
            ));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public List<RelatorioOcupacaoPeriodoDTO> buscarOcupacaoPorPeriodo(
            LocalDate dataInicio,
            LocalDate dataFim,
            PeriodoRelatorioTipo agrupamento,
            String modalidadeOcupacao,
            VagaTipo tipoVaga) {

        PeriodoRelatorioTipo tipo = agrupamento != null ? agrupamento : PeriodoRelatorioTipo.DIA;
        String unit = tipo.getPgTruncUnit();
        String format = tipo.getPgDateFormat();
        String modalidade = normalizarFiltro(modalidadeOcupacao);
        String vagaFiltro = tipoVaga != null ? tipoVaga.name() : "TODOS";

        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            SELECT
                TO_CHAR(DATE_TRUNC('%s', p.dataentrada), '%s') AS periodo_label,
                COUNT(*) AS total_entradas,
                COUNT(*) FILTER (WHERE COALESCE(p.isrotativo, true) = true) AS entradas_rotativo,
                COUNT(*) FILTER (WHERE COALESCE(p.isrotativo, true) = false) AS entradas_mensalista,
                COUNT(*) FILTER (WHERE p.status = 'concluida') AS saidas_concluidas,
                COALESCE(
                    ROUND(
                        AVG(EXTRACT(EPOCH FROM (p.datasaida - p.dataentrada)) / 60)
                        FILTER (WHERE p.status = 'concluida' AND p.datasaida IS NOT NULL),
                        0
                    ),
                    0
                ) AS tempo_medio_minutos
            FROM permanencias p
            JOIN vagas vg ON p.vaga_id = vg.id
            WHERE p.dataentrada BETWEEN :inicioTs AND :fimTs
              AND (:modalidade = 'TODAS'
                   OR (:modalidade = 'ROTATIVO' AND COALESCE(p.isrotativo, true) = true)
                   OR (:modalidade = 'MENSALISTA' AND COALESCE(p.isrotativo, true) = false))
              AND (:tipoVaga = 'TODOS' OR vg.tipovaga = :tipoVaga)
            GROUP BY DATE_TRUNC('%s', p.dataentrada)
            ORDER BY DATE_TRUNC('%s', p.dataentrada) DESC;
        """.formatted(unit, format, unit, unit);

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("modalidade", modalidade)
                .setParameter("tipoVaga", vagaFiltro)
                .getResultList();

        List<RelatorioOcupacaoPeriodoDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            long minutos = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            lista.add(new RelatorioOcupacaoPeriodoDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue(),
                    minutos,
                    formatarMinutos(minutos)
            ));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public List<RelatorioOcupacaoVagaTipoDTO> buscarOcupacaoPorTipoVaga(
            LocalDate dataInicio,
            LocalDate dataFim,
            String modalidadeOcupacao) {

        String modalidade = normalizarFiltro(modalidadeOcupacao);
        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            WITH por_tipo AS (
                SELECT
                    vg.tipovaga AS codigo_tipo,
                    COUNT(DISTINCT vg.id) AS total_vagas,
                    COUNT(p.id) AS total_usos,
                    COALESCE(
                        ROUND(
                            AVG(EXTRACT(EPOCH FROM (p.datasaida - p.dataentrada)) / 60)
                            FILTER (WHERE p.status = 'concluida' AND p.datasaida IS NOT NULL),
                            0
                        ),
                        0
                    ) AS tempo_medio_min
                FROM vagas vg
                LEFT JOIN permanencias p
                    ON p.vaga_id = vg.id
                   AND p.dataentrada BETWEEN :inicioTs AND :fimTs
                   AND (:modalidade = 'TODAS'
                        OR (:modalidade = 'ROTATIVO' AND COALESCE(p.isrotativo, true) = true)
                        OR (:modalidade = 'MENSALISTA' AND COALESCE(p.isrotativo, true) = false))
                GROUP BY vg.tipovaga
            )
            SELECT
                CASE t.codigo_tipo
                    WHEN 'comum' THEN 'Comum'
                    WHEN 'moto' THEN 'Moto'
                    WHEN 'pne' THEN 'PNE (Acessível)'
                    WHEN 'idoso' THEN 'Idoso'
                    ELSE COALESCE(t.codigo_tipo, 'Outros')
                END AS tipo_label,
                t.total_vagas,
                t.total_usos,
                COALESCE(ROUND((t.total_usos::numeric / NULLIF(SUM(t.total_usos) OVER (), 0)) * 100, 2), 0) AS percentual,
                t.tempo_medio_min
            FROM por_tipo t
            ORDER BY t.total_usos DESC, tipo_label ASC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("modalidade", modalidade)
                .getResultList();

        List<RelatorioOcupacaoVagaTipoDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            long minutos = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            lista.add(new RelatorioOcupacaoVagaTipoDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    toBigDecimal(row[3]),
                    formatarMinutos(minutos)
            ));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public List<RelatorioOcupacaoFaixaHorariaDTO> buscarOcupacaoPorFaixaHoraria(
            LocalDate dataInicio,
            LocalDate dataFim,
            String modalidadeOcupacao,
            VagaTipo tipoVaga) {

        String modalidade = normalizarFiltro(modalidadeOcupacao);
        String vagaFiltro = tipoVaga != null ? tipoVaga.name() : "TODOS";
        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            WITH por_hora AS (
                SELECT
                    EXTRACT(HOUR FROM p.dataentrada)::int AS hora,
                    COUNT(*) AS total_entradas,
                    COUNT(*) FILTER (WHERE COALESCE(p.isrotativo, true) = true) AS rotativos,
                    COUNT(*) FILTER (WHERE COALESCE(p.isrotativo, true) = false) AS mensalistas
                FROM permanencias p
                JOIN vagas vg ON p.vaga_id = vg.id
                WHERE p.dataentrada BETWEEN :inicioTs AND :fimTs
                  AND (:modalidade = 'TODAS'
                       OR (:modalidade = 'ROTATIVO' AND COALESCE(p.isrotativo, true) = true)
                       OR (:modalidade = 'MENSALISTA' AND COALESCE(p.isrotativo, true) = false))
                  AND (:tipoVaga = 'TODOS' OR vg.tipovaga = :tipoVaga)
                GROUP BY EXTRACT(HOUR FROM p.dataentrada)::int
            )
            SELECT
                LPAD(h.hora::text, 2, '0') || ':00 - ' || LPAD(h.hora::text, 2, '0') || ':59' AS faixa_horaria,
                h.total_entradas,
                h.rotativos,
                h.mensalistas,
                COALESCE(ROUND((h.total_entradas::numeric / NULLIF(SUM(h.total_entradas) OVER (), 0)) * 100, 2), 0) AS percentual
            FROM por_hora h
            ORDER BY h.total_entradas DESC, h.hora ASC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("modalidade", modalidade)
                .setParameter("tipoVaga", vagaFiltro)
                .getResultList();

        List<RelatorioOcupacaoFaixaHorariaDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            lista.add(new RelatorioOcupacaoFaixaHorariaDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    ((Number) row[3]).longValue(),
                    toBigDecimal(row[4])
            ));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public List<RelatorioOcupacaoDetalheDTO> buscarDetalhesOcupacao(
            LocalDate dataInicio,
            LocalDate dataFim,
            String modalidadeOcupacao,
            VagaTipo tipoVaga) {

        String modalidade = normalizarFiltro(modalidadeOcupacao);
        String vagaFiltro = tipoVaga != null ? tipoVaga.name() : "TODOS";
        LocalDateTime inicioTs = dataInicio.atStartOfDay();
        LocalDateTime fimTs = dataFim.atTime(LocalTime.MAX);

        String sql = """
            SELECT
                p.id,
                v.placa,
                COALESCE(NULLIF(TRIM(COALESCE(v.marca, '') || ' ' || COALESCE(v.modelo, '')), ''), 'Não informado') AS veiculo_desc,
                vg.nome AS vaga_nome,
                CASE vg.tipovaga
                    WHEN 'comum' THEN 'Comum'
                    WHEN 'moto' THEN 'Moto'
                    WHEN 'pne' THEN 'PNE'
                    WHEN 'idoso' THEN 'Idoso'
                    ELSE COALESCE(vg.tipovaga, '-')
                END AS tipo_vaga_label,
                COALESCE(p.isrotativo, true) AS is_rotativo,
                TO_CHAR(p.dataentrada, 'DD/MM/YYYY HH24:MI') AS entrada_fmt,
                COALESCE(TO_CHAR(p.datasaida, 'DD/MM/YYYY HH24:MI'), 'No pátio') AS saida_fmt,
                ROUND(
                    EXTRACT(EPOCH FROM (COALESCE(p.datasaida, CURRENT_TIMESTAMP) - p.dataentrada)) / 60,
                    0
                ) AS duracao_minutos,
                CASE p.status
                    WHEN 'concluida' THEN 'Concluída'
                    WHEN 'em_andamento' THEN 'Em Andamento'
                    ELSE COALESCE(p.status, '-')
                END AS status_label,
                COALESCE(pag.valor, 0) AS valor_pago
            FROM permanencias p
            JOIN veiculos v ON p.veiculo_id = v.id
            JOIN vagas vg ON p.vaga_id = vg.id
            LEFT JOIN pagamentos pag ON (p.pagamento_id = pag.id OR pag.permanencia_id = p.id) AND pag.status = 'pago'
            WHERE p.dataentrada BETWEEN :inicioTs AND :fimTs
              AND (:modalidade = 'TODAS'
                   OR (:modalidade = 'ROTATIVO' AND COALESCE(p.isrotativo, true) = true)
                   OR (:modalidade = 'MENSALISTA' AND COALESCE(p.isrotativo, true) = false))
              AND (:tipoVaga = 'TODOS' OR vg.tipovaga = :tipoVaga)
            ORDER BY p.dataentrada DESC, p.id DESC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("inicioTs", inicioTs)
                .setParameter("fimTs", fimTs)
                .setParameter("modalidade", modalidade)
                .setParameter("tipoVaga", vagaFiltro)
                .getResultList();

        List<RelatorioOcupacaoDetalheDTO> lista = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            long minutos = row[8] != null ? ((Number) row[8]).longValue() : 0L;
            lista.add(new RelatorioOcupacaoDetalheDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    Boolean.TRUE.equals(row[5]),
                    (String) row[6],
                    (String) row[7],
                    formatarMinutos(minutos),
                    (String) row[9],
                    toBigDecimal(row[10])
            ));
        }
        return lista;
    }

    public static String formatarMinutos(long minutosTotais) {
        if (minutosTotais <= 0) {
            return "00h 00m";
        }
        long horas = minutosTotais / 60;
        long minutos = minutosTotais % 60;
        return String.format("%02dh %02dm", horas, minutos);
    }

    private String normalizarFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return "TODAS";
        }
        return valor.trim().toUpperCase();
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) {
            return BigDecimal.ZERO;
        }
        if (val instanceof BigDecimal bd) {
            return bd;
        }
        if (val instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        return BigDecimal.ZERO;
    }

}
