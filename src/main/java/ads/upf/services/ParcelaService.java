package ads.upf.services;

import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.StatusParcela;
import ads.upf.repositories.ParcelaRepository;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@ApplicationScoped
public class ParcelaService {

    private static final BigDecimal VALOR_PADRAO = BigDecimal.valueOf(180.00);

    private final ParcelaRepository parcelaRepository;

    @Inject
    public ParcelaService(ParcelaRepository parcelaRepository) {
        this.parcelaRepository = parcelaRepository;
    }

    /**
     * Gera as parcelas em memória com base no intervalo de datas.
     * Operação pura em memória (sem abertura de transação de banco).
     */
    public List<Parcela> gerarParcelas(LocalDate dataInicio, LocalDate dataTermino) {
        Objects.requireNonNull(dataInicio, "A data de início é obrigatória.");
        Objects.requireNonNull(dataTermino, "A data de término é obrigatória.");

        if (!dataTermino.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data de término deve ser posterior à data de início.");
        }

        // Calcula a quantidade de meses considerando início e fim de calendário
        int totalMeses = calcularTotalMeses(dataInicio, dataTermino);
        if (totalMeses < 1) {
            throw new IllegalArgumentException("O período informado deve contemplar ao menos 1 mês de contrato.");
        }

        List<Parcela> parcelas = new ArrayList<>(totalMeses);

        for (int i = 1; i <= totalMeses; i++) {
            Parcela parcela = new Parcela();

            // Vencimento baseado no ciclo mensal regular
            LocalDate dataVencimento = dataInicio.plusMonths(i);

            parcela.setDataVencimento(dataVencimento);
            parcela.setNumeroParcela(i);
            parcela.setStatus(StatusParcela.pendente);
            parcela.setValor(VALOR_PADRAO);
            parcela.setCobrancaEnviada(false);

            parcelas.add(parcela);
        }

        return parcelas;
    }

    /**
     * Executa todos os dias às 01:00 da manhã.
     * Atualização atômica (Bulk Update) em banco, evitando N+1 e estouro de memória.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processarParcelasVencidas() {
        Log.info("Iniciando rotina agendada de atualização de parcelas vencidas...");

        int totalAtualizadas = parcelaRepository.update(
                "status = ?1, atualizadoEm = ?2 where status = ?3 and dataVencimento < ?4 and dataPagamento is null",
                StatusParcela.atrasada,
                LocalDateTime.now(),
                StatusParcela.pendente,
                LocalDate.now()
        );

        Log.infof("Rotina concluída: %d parcela(s) marcada(s) como atrasada(s).", totalAtualizadas);
    }

    /**
     * Método auxiliar para usar na classe de testes unitários ParcelaServiceTest
     * Classes anotadas com @Scheduled devem retornar void
     */
    public int processarParcelasVencidasTest() {
        Log.info("Iniciando rotina agendada de atualização de parcelas vencidas...");

        int totalAtualizadas = parcelaRepository.update(
                "status = ?1, atualizadoEm = ?2 where status = ?3 and dataVencimento < ?4 and dataPagamento is null",
                StatusParcela.atrasada,
                LocalDateTime.now(),
                StatusParcela.pendente,
                LocalDate.now()
        );

        Log.infof("Rotina concluída: %d parcela(s) marcada(s) como atrasada(s).", totalAtualizadas);
        return totalAtualizadas;
    }

    /**
     * Sincroniza parcelas no startup da aplicação caso o servidor estivesse desligado no horário do cron.
     */
    void onStart(@Observes StartupEvent ev) {
        Log.info("Startup da aplicação: verificando parcelas vencidas pendentes...");
        processarParcelasVencidas();
    }

    private int calcularTotalMeses(LocalDate dataInicio, LocalDate dataTermino) {
        YearMonth inicio = YearMonth.from(dataInicio);
        YearMonth termino = YearMonth.from(dataTermino);

        // Quantidade de viradas de mês entre as competências
        long diferencaCompetencias = ChronoUnit.MONTHS.between(inicio, termino);

        return (int) Math.max(diferencaCompetencias, 1);
    }
}
