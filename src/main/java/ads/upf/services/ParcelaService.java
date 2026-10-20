package ads.upf.services;

import ads.upf.integration.asaas.DTOs.AsaasWebhookEvent;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ParcelaMeiosPagamento;
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.repositories.ParcelaRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            parcela.setStatus(ParcelaStatus.pendente);
            parcela.setValor(VALOR_PADRAO);
            parcela.setCobrancaEnviada(false);

            parcelas.add(parcela);
        }

        return parcelas;
    }

    private int calcularTotalMeses(LocalDate dataInicio, LocalDate dataTermino) {
        YearMonth inicio = YearMonth.from(dataInicio);
        YearMonth termino = YearMonth.from(dataTermino);

        // Quantidade de viradas de mês entre as competências
        long diferencaCompetencias = ChronoUnit.MONTHS.between(inicio, termino);

        return (int) Math.max(diferencaCompetencias, 1);
    }

    @Transactional
    public void marcarComoPago(AsaasWebhookEvent.Payment payment) {
        Optional<Parcela> opt = parcelaRepository.findByAsaasPaymentId(payment.id())
                .firstResultOptional();

        if (opt.isEmpty()) {
            Log.warnf("Webhook ignorado: parcela não encontrada para o pagamento %s", payment.id());
            return;
        }
        Parcela parcela = opt.get();

        if (parcela.getStatus().equals(ParcelaStatus.paga)) return;

        if (parcela.getStatus().equals(ParcelaStatus.cancelada)) {
            Log.warnf("Alerta no Webhook: Pagamento recebido de parcela cancelada %s", payment.id());
        }

        parcela.setStatus(ParcelaStatus.paga);
        parcela.setDataPagamento(payment.paymentDate());
        parcela.setMeioPagamento(ParcelaMeiosPagamento.valueOf(payment.billingType()));

        parcela.getContrato().encerrarSeQuitado();
    }

    @Transactional
    public void marcarComoAtrasada(AsaasWebhookEvent.Payment payment) {
        Optional<Parcela> opt = parcelaRepository.findByAsaasPaymentId(payment.id())
                .firstResultOptional();

        if (opt.isEmpty()) {
            Log.warnf("Webhook ignorado: parcela não encontrada para o pagamento %s", payment.id());
            return;
        }
        Parcela parcela = opt.get();

        if (parcela.getStatus().equals(ParcelaStatus.paga)) return;

        parcela.setStatus(ParcelaStatus.atrasada);
    }

    @Transactional
    public List<Long> buscarIdsParaCobrar() {
        return parcelaRepository.buscarComVencimentoEm7Dias().stream()
                .map(Parcela::getId)
                .toList();
    }
}
