package ads.upf.services;

import ads.upf.model.DTOs.pagamento.PagamentoResponseDTO;
import ads.upf.model.DTOs.pagamento.ResumoCalculoPermanenciaDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Pagamento;
import ads.upf.model.entities.Tarifa;
import ads.upf.model.enums.PagamentoMeio;
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
    private final TarifaService tarifaService;

    @Inject
    public PagamentoService(PagamentoRepository pagamentoRepository, TarifaService tarifaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.tarifaService = tarifaService;
    }

    @Transactional
    public Pagamento gerarPagamento(BigDecimal valor, PagamentoMeio meio) {
        Pagamento pagamento = new Pagamento();
        pagamento.setValor(valor);
        pagamento.setStatus(PagamentoStatus.pago);
        pagamento.setMeioPagamento(meio);
        pagamento.setDataPagamento(LocalDateTime.now());

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
        return detalharCalculoPermanencia(dataEntrada, dataSaida).getValorTotal();
    }

    /**
     * Retorna a memória de cálculo completa da permanência (tempo decorrido, valor da 1ª hora,
     * quantidade e subtotal de frações adicionais de 30 minutos e valor total).
     */
    public ResumoCalculoPermanenciaDTO detalharCalculoPermanencia(LocalDateTime dataEntrada, LocalDateTime dataSaida) {
        if (dataEntrada == null || dataSaida == null) {
            throw new IllegalArgumentException("As datas de entrada e saída são obrigatórias.");
        }
        if (dataSaida.isBefore(dataEntrada)) {
            throw new IllegalArgumentException("A data de saída não pode ser anterior à data de entrada.");
        }

        Duration duracao = Duration.between(dataEntrada, dataSaida);
        long segundosTotais = duracao.getSeconds();
        long minutosTotais = Math.max(1L, (segundosTotais + 59) / 60);

        Tarifa tarifaAtiva = tarifaService.buscarTarifaAtiva();
        BigDecimal valorHora = tarifaAtiva.getValorHora().setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorFracao = tarifaAtiva.getValorFracaoHora().setScale(2, RoundingMode.HALF_UP);

        long fracoesAdicionais = 0L;
        if (segundosTotais > SEGUNDOS_PRIMEIRA_HORA) {
            long segundosExcedentes = segundosTotais - SEGUNDOS_PRIMEIRA_HORA;
            fracoesAdicionais = (segundosExcedentes + SEGUNDOS_POR_FRACAO - 1) / SEGUNDOS_POR_FRACAO;
        }

        BigDecimal valorAdicional = valorFracao.multiply(BigDecimal.valueOf(fracoesAdicionais)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorTotal = valorHora.add(valorAdicional).setScale(2, RoundingMode.HALF_UP);

        return new ResumoCalculoPermanenciaDTO(
                dataEntrada,
                dataSaida,
                minutosTotais,
                formatarDuracao(duracao),
                valorHora,
                valorFracao,
                fracoesAdicionais,
                valorAdicional,
                valorTotal
        );
    }

    public String formatarDuracao(Duration duracao) {
        long segundos = Math.max(0L, duracao.getSeconds());
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        if (horas == 0 && minutos == 0) {
            return "< 1 min";
        }
        if (horas == 0) {
            return minutos + " min";
        }
        return String.format("%dh %02dmin", horas, minutos);
    }

    /**
     * Gera o resumo de cálculo do comprovante para uma permanência já concluída,
     * preservando o valor efetivamente pago e garantindo funcionamento mesmo sem tarifa ativa.
     */
    public ResumoCalculoPermanenciaDTO gerarResumoComprovante(PermanenciaResponseDTO permanencia) {
        LocalDateTime entradaData = permanencia.getDataEntrada();
        LocalDateTime saidaData = permanencia.getDataSaida() != null ? permanencia.getDataSaida() : LocalDateTime.now();
        PagamentoResponseDTO pagamento = permanencia.getPagamento();
        BigDecimal valorPago = (pagamento != null && pagamento.getValor() != null) ? pagamento.getValor() : BigDecimal.ZERO;

        try {
            ResumoCalculoPermanenciaDTO calculo = detalharCalculoPermanencia(entradaData, saidaData);
            if (pagamento != null && pagamento.getValor() != null) {
                calculo.setValorTotal(valorPago);
            }
            return calculo;
        } catch (Exception e) {
            Duration duracao = (entradaData != null && saidaData != null)
                    ? Duration.between(entradaData, saidaData)
                    : Duration.ZERO;
            return new ResumoCalculoPermanenciaDTO(
                    entradaData,
                    saidaData,
                    Math.max(1L, duracao.toMinutes()),
                    formatarDuracao(duracao),
                    valorPago,
                    BigDecimal.ZERO,
                    0L,
                    BigDecimal.ZERO,
                    valorPago
            );
        }
    }

}
