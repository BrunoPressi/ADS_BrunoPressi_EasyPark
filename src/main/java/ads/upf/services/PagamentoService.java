package ads.upf.services;

import ads.upf.model.entities.Pagamento;
import ads.upf.model.entities.Tarifa;
import ads.upf.model.enums.PagamentoStatus;
import ads.upf.repositories.PagamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class PagamentoService {

    private static final long SEGUNDOS_PRIMEIRA_HORA = 3600L; // 60 minutos
    private static final long SEGUNDOS_POR_FRACAO = 1800L;    // 30 minutos

    private final PagamentoRepository pagamentoRepository;

    @Inject
    public PagamentoService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional
    public Pagamento gerarPagamento(LocalDateTime dataEntrada, LocalDateTime dataSaida) {
        BigDecimal valorTotal = calcularValorPermanencia(dataEntrada, dataSaida);

        Pagamento pagamento = new Pagamento();
        pagamento.setValor(valorTotal);
        pagamento.setStatus(PagamentoStatus.pendente);

        pagamentoRepository.persist(pagamento);

        return pagamento;
    }

    /**
     * Calcula o valor da permanência com base nas datas de entrada e saída.
     * Regra de negócio:
     * - Cobrança mínima da 1ª hora (até 60 minutos) com valor fixo da Tarifa (R$ 70,00). Sem tolerância gratuita.
     * - Após os 60 minutos, cada fração de até 30 minutos (iniciada) acrescenta o valor da fração (R$ 35,00).
     *
     * @param dataEntrada Data e hora de entrada do veículo
     * @param dataSaida   Data e hora de saída do veículo
     * @return Valor total da permanência
     */
    public BigDecimal calcularValorPermanencia(LocalDateTime dataEntrada, LocalDateTime dataSaida) {
        if (dataEntrada == null || dataSaida == null) {
            throw new IllegalArgumentException("As datas de entrada e saída são obrigatórias.");
        }
        if (dataSaida.isBefore(dataEntrada)) {
            throw new IllegalArgumentException("A data de saída não pode ser anterior à data de entrada.");
        }

        Duration duracao = Duration.between(dataEntrada, dataSaida);
        long segundosTotais = duracao.getSeconds();

        BigDecimal valorHora = Tarifa.getValorHora();
        BigDecimal valorFracao = Tarifa.getValorFracaoHora();

        // Até 60 minutos (inclusive 0s, pois qualquer permanência inicia a cobrança da 1ª hora)
        if (segundosTotais <= SEGUNDOS_PRIMEIRA_HORA) {
            return valorHora.setScale(2, RoundingMode.HALF_UP);
        }

        // Tempo excedente além da primeira hora: frações de 30 minutos (arredondando para cima qualquer fração iniciada)
        long segundosExcedentes = segundosTotais - SEGUNDOS_PRIMEIRA_HORA;
        long fracoesAdicionais = (segundosExcedentes + SEGUNDOS_POR_FRACAO - 1) / SEGUNDOS_POR_FRACAO;

        BigDecimal valorAdicional = valorFracao.multiply(BigDecimal.valueOf(fracoesAdicionais));
        BigDecimal valorTotal = valorHora.add(valorAdicional).setScale(2, RoundingMode.HALF_UP);

        return valorTotal;
    }

}
