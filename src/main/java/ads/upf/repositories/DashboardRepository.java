package ads.upf.repositories;

import ads.upf.model.DTOs.dashboard.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DashboardRepository {

    @Inject
    private EntityManager em;

    public OcupacaoKpiDTO buscarKpiOcupacao() {
        String sql = """
            SELECT
                COUNT(*) AS total_vagas,
                COUNT(*) FILTER (WHERE status = 'ocupada') AS vagas_ocupadas,
                COUNT(*) FILTER (WHERE status = 'disponivel') AS vagas_disponiveis,
                COUNT(*) FILTER (WHERE status = 'em_manutencao') AS vagas_manutencao,
                ROUND(
                    (COUNT(*) FILTER (WHERE status = 'ocupada')::NUMERIC /
                     NULLIF(COUNT(*) FILTER (WHERE status != 'em_manutencao'), 0)) * 100,
                    2
                ) AS taxa_ocupacao_percentual
            FROM vagas;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new OcupacaoKpiDTO(
                ((Number) result[0]).longValue(),
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).longValue(),
                result[4] != null ? (BigDecimal) result[4] : BigDecimal.ZERO
        );
    }

    public VeiculosAtuaisKpiDTO buscarKpiVeiculosAtuais() {
        String sql = """
            SELECT
                COUNT(*) AS veiculos_no_patio,
                COUNT(*) FILTER (WHERE isrotativo = true) AS rotativos_no_patio,
                COUNT(*) FILTER (WHERE isrotativo = false) AS mensalistas_no_patio
            FROM permanencias
            WHERE status = 'em_andamento';
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new VeiculosAtuaisKpiDTO(
                ((Number) result[0]).longValue(),
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue()
        );
    }

    public TempoMedioKpiDTO buscarKpiTempoMedio() {
        String sql = """
            SELECT
                ROUND(AVG(EXTRACT(EPOCH FROM (datasaida - dataentrada)) / 60), 0) AS tempo_medio_minutos_hoje,
                TO_CHAR(
                    INTERVAL '1 second' * AVG(EXTRACT(EPOCH FROM (datasaida - dataentrada))),
                    'HH24"h "MI"m"'
                ) AS tempo_medio_formatado
            FROM permanencias
            WHERE status = 'concluida'
              AND isrotativo = true
              AND datasaida >= CURRENT_DATE;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        long minutos = result[0] != null ? ((Number) result[0]).longValue() : 0L;
        String formatado = result[1] != null ? result[1].toString() : "00h 00m";

        return new TempoMedioKpiDTO(minutos, formatado);
    }

    public GiroVagasKpiDTO buscarKpiGiroVagas() {
        String sql = """
            SELECT
                COALESCE(
                    ROUND(
                        COUNT(p.id)::NUMERIC / NULLIF((SELECT COUNT(*) FROM vagas WHERE status != 'em_manutencao'), 0),
                        2
                    ),
                    0
                ) AS giro_medio_hoje,
                COUNT(p.id) AS total_entradas_hoje
            FROM permanencias p
            WHERE p.dataentrada >= CURRENT_DATE;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        BigDecimal giro = result[0] != null ? (BigDecimal) result[0] : BigDecimal.ZERO;
        long totalEntradas = result[1] != null ? ((Number) result[1]).longValue() : 0L;

        return new GiroVagasKpiDTO(giro, totalEntradas);
    }

    public ReceitaHojeKpiDTO buscarKpiReceitaHoje() {
        String sql = """
            SELECT
                COALESCE(SUM(valor), 0) AS faturamento_hoje,
                COUNT(*) AS total_transacoes_hoje
            FROM pagamentos
            WHERE status = 'pago'
              AND datapagamento >= CURRENT_DATE;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        BigDecimal faturamento = result[0] != null ? (BigDecimal) result[0] : BigDecimal.ZERO;
        long transacoes = result[1] != null ? ((Number) result[1]).longValue() : 0L;

        return new ReceitaHojeKpiDTO(faturamento, transacoes);
    }

    public ReceitaMesKpiDTO buscarKpiReceitaMes() {
        String sql = """
            SELECT
                r.receita_rotativo,
                m.receita_mensalistas,
                (r.receita_rotativo + m.receita_mensalistas) AS receita_total_mes,
                ROUND((r.receita_rotativo / NULLIF(r.receita_rotativo + m.receita_mensalistas, 0)) * 100, 2) AS percentual_rotativo,
                ROUND((m.receita_mensalistas / NULLIF(r.receita_rotativo + m.receita_mensalistas, 0)) * 100, 2) AS percentual_mensalistas,
                r.ticket_medio_rotativo,
                ROUND(
                    (r.receita_rotativo + m.receita_mensalistas) / NULLIF((SELECT COUNT(*) FROM vagas), 0),
                    2
                ) AS revpas
            FROM (
                SELECT
                    COALESCE(SUM(valor), 0) AS receita_rotativo,
                    COALESCE(ROUND(AVG(valor), 2), 0) AS ticket_medio_rotativo
                FROM pagamentos
                WHERE status = 'pago'
                  AND DATE_TRUNC('month', datapagamento) = DATE_TRUNC('month', CURRENT_DATE)
            ) r
            CROSS JOIN (
                SELECT COALESCE(SUM(valor), 0) AS receita_mensalistas
                FROM parcelas
                WHERE status = 'paga'
                  AND DATE_TRUNC('month', datapagamento) = DATE_TRUNC('month', CURRENT_DATE)
            ) m;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new ReceitaMesKpiDTO(
                result[0] != null ? (BigDecimal) result[0] : BigDecimal.ZERO,
                result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO,
                result[2] != null ? (BigDecimal) result[2] : BigDecimal.ZERO,
                result[3] != null ? (BigDecimal) result[3] : BigDecimal.ZERO,
                result[4] != null ? (BigDecimal) result[4] : BigDecimal.ZERO,
                result[5] != null ? (BigDecimal) result[5] : BigDecimal.ZERO,
                result[6] != null ? (BigDecimal) result[6] : BigDecimal.ZERO
        );
    }

    public ContratosKpiDTO buscarKpiContratos() {
        String sql = """
            SELECT
                COUNT(*) AS total_cadastrados,
                COUNT(*) FILTER (WHERE status = 'ativo') AS ativos,
                COUNT(*) FILTER (WHERE status = 'inadimplente') AS inadimplentes,
                COUNT(*) FILTER (WHERE status = 'cancelado') AS cancelados,
                COUNT(*) FILTER (WHERE status = 'encerrado') AS encerrados,
                ROUND(
                    (COUNT(*) FILTER (WHERE status = 'ativo')::NUMERIC / NULLIF((SELECT COUNT(*) FROM vagas), 0)) * 100,
                    2
                ) AS percentual_vagas_comprometidas
            FROM contratos;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new ContratosKpiDTO(
                ((Number) result[0]).longValue(),
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).longValue(),
                ((Number) result[4]).longValue(),
                result[5] != null ? (BigDecimal) result[5] : BigDecimal.ZERO
        );
    }

    public ChurnKpiDTO buscarKpiChurn() {
        String sql = """
            SELECT
                COUNT(*) FILTER (
                    WHERE status = 'ativo'
                      AND DATE_TRUNC('month', datainicio) = DATE_TRUNC('month', CURRENT_DATE)
                ) AS novos_contratos_mes,
                COUNT(*) FILTER (
                    WHERE (status = 'cancelado' AND DATE_TRUNC('month', COALESCE(atualizado_em, datatermino)) = DATE_TRUNC('month', CURRENT_DATE))
                       OR (status = 'encerrado' AND DATE_TRUNC('month', datatermino) = DATE_TRUNC('month', CURRENT_DATE))
                ) AS contratos_perdidos_mes,
                (
                    COUNT(*) FILTER (
                        WHERE status = 'ativo'
                          AND DATE_TRUNC('month', datainicio) = DATE_TRUNC('month', CURRENT_DATE)
                    ) -
                    COUNT(*) FILTER (
                        WHERE (status = 'cancelado' AND DATE_TRUNC('month', COALESCE(atualizado_em, datatermino)) = DATE_TRUNC('month', CURRENT_DATE))
                           OR (status = 'encerrado' AND DATE_TRUNC('month', datatermino) = DATE_TRUNC('month', CURRENT_DATE))
                    )
                ) AS saldo_liquido_mes
            FROM contratos;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new ChurnKpiDTO(
                ((Number) result[0]).longValue(),
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue()
        );
    }

    public InadimplenciaKpiDTO buscarKpiInadimplencia() {
        String sql = """
            SELECT
                COUNT(*) FILTER (WHERE status = 'atrasada') AS parcelas_atrasadas,
                COALESCE(SUM(valor) FILTER (WHERE status = 'atrasada'), 0) AS valor_inadimplente,
                ROUND(
                    (COALESCE(SUM(valor) FILTER (WHERE status = 'atrasada'), 0) /
                     NULLIF(SUM(valor), 0)) * 100,
                    2
                ) AS taxa_inadimplencia_percentual
            FROM parcelas
            WHERE datavencimento <= CURRENT_DATE;
        """;

        Object[] result = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new InadimplenciaKpiDTO(
                ((Number) result[0]).longValue(),
                result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO,
                result[2] != null ? (BigDecimal) result[2] : BigDecimal.ZERO
        );
    }

    @SuppressWarnings("unchecked")
    public List<EstadiaExcessivaDTO> buscarEstadiasExcessivas() {
        String sql = """
            SELECT
                p.id AS permanencia_id,
                v.placa,
                COALESCE(v.modelo, 'Não informado') AS modelo,
                vg.nome AS vaga_nome,
                TO_CHAR(p.dataentrada, 'DD/MM/YYYY HH24:MI') AS data_entrada,
                ROUND((EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.dataentrada)) / 3600)::NUMERIC, 1) AS horas_estacionado,
                COALESCE(p.isrotativo, true) AS is_rotativo
            FROM permanencias p
            JOIN veiculos v ON p.veiculo_id = v.id
            JOIN vagas vg ON p.vaga_id = vg.id
            WHERE p.status = 'em_andamento'
              AND p.dataentrada < (CURRENT_TIMESTAMP - INTERVAL '24 hours')
            ORDER BY p.dataentrada ASC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        List<EstadiaExcessivaDTO> list = new ArrayList<>();

        for (Object[] row : rows) {
            list.add(new EstadiaExcessivaDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    ((Number) row[5]).doubleValue(),
                    (Boolean) row[6]
            ));
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<ParcelaVencerDTO> buscarParcelasAVencer() {
        String sql = """
            SELECT
                par.id AS parcela_id,
                c.id AS contrato_id,
                u.nomecompleto AS cliente_nome,
                u.telefone,
                par.numeroparcela,
                TO_CHAR(par.datavencimento, 'DD/MM/YYYY') AS data_vencimento,
                par.valor
            FROM parcelas par
            JOIN contratos c ON par.contrato_id = c.id
            JOIN clientes cli ON c.cliente_id = cli.usuarioid
            JOIN usuarios u ON cli.usuarioid = u.id
            WHERE par.status = 'pendente'
              AND par.datavencimento BETWEEN CURRENT_DATE AND (CURRENT_DATE + INTERVAL '7 days')
            ORDER BY par.datavencimento ASC;
        """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        List<ParcelaVencerDTO> list = new ArrayList<>();

        for (Object[] row : rows) {
            list.add(new ParcelaVencerDTO(
                    ((Number) row[0]).longValue(),
                    ((Number) row[1]).longValue(),
                    (String) row[2],
                    (String) row[3],
                    ((Number) row[4]).intValue(),
                    (String) row[5],
                    (BigDecimal) row[6]
            ));
        }

        return list;
    }

}
