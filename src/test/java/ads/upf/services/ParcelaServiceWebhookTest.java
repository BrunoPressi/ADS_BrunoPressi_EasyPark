package ads.upf.services;

import ads.upf.integration.asaas.DTOs.AsaasWebhookEvent;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ParcelaMeiosPagamento;
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.repositories.ParcelaRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ParcelaService - Webhook do Asaas")
class ParcelaServiceWebhookTest {

    private static final String PAYMENT_ID = "pay_123";
    private static final LocalDate DATA_PAGAMENTO = LocalDate.of(2026, 10, 15);

    @Mock
    ParcelaRepository parcelaRepository;

    @Mock
    PanacheQuery<Parcela> query;

    @InjectMocks
    ParcelaService parcelaService;

    @BeforeEach
    void setUp() {
        when(parcelaRepository.findByAsaasPaymentId(PAYMENT_ID)).thenReturn(query);
    }

    private AsaasWebhookEvent.Payment payment(String billingType) {
        return new AsaasWebhookEvent.Payment(
                PAYMENT_ID, "RECEIVED", billingType, DATA_PAGAMENTO, DATA_PAGAMENTO, BigDecimal.valueOf(180));
    }

    private Parcela parcela(ParcelaStatus status, Contrato contrato) {
        Parcela p = new Parcela();
        p.setStatus(status);
        p.setAsaasPaymentId(PAYMENT_ID);
        contrato.adicionarParcela(p);
        return p;
    }

    private Contrato contratoAtivo() {
        Contrato c = new Contrato();
        c.setStatus(ContratoStatus.ativo);
        return c;
    }

    @Nested
    @DisplayName("marcarComoPago")
    class MarcarComoPago {

        @Test
        @DisplayName("Marca a parcela como paga e registra data e meio de pagamento")
        void marcaComoPaga() {
            Contrato contrato = contratoAtivo();
            Parcela p = parcela(ParcelaStatus.pendente, contrato);
            parcela(ParcelaStatus.pendente, contrato); // mantém o contrato aberto
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoPago(payment("PIX"));

            assertThat(p.getStatus()).isEqualTo(ParcelaStatus.paga);
            assertThat(p.getDataPagamento()).isEqualTo(DATA_PAGAMENTO);
            assertThat(p.getMeioPagamento()).isEqualTo(ParcelaMeiosPagamento.PIX);
        }

        @Test
        @DisplayName("Encerra o contrato ao pagar a última parcela")
        void encerraContratoNaUltimaParcela() {
            Contrato contrato = contratoAtivo();
            parcela(ParcelaStatus.paga, contrato);
            Parcela ultima = parcela(ParcelaStatus.pendente, contrato);
            when(query.firstResultOptional()).thenReturn(Optional.of(ultima));

            parcelaService.marcarComoPago(payment("BOLETO"));

            assertThat(contrato.getStatus()).isEqualTo(ContratoStatus.encerrado);
        }

        @Test
        @DisplayName("Mantém o contrato ativo quando ainda há parcelas em aberto")
        void mantemContratoAtivoComParcelasEmAberto() {
            Contrato contrato = contratoAtivo();
            Parcela p = parcela(ParcelaStatus.pendente, contrato);
            parcela(ParcelaStatus.pendente, contrato);
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoPago(payment("PIX"));

            assertThat(contrato.getStatus()).isEqualTo(ContratoStatus.ativo);
        }

        @Test
        @DisplayName("Pagar parcela atrasada quita e pode encerrar o contrato")
        void pagaParcelaAtrasada() {
            Contrato contrato = contratoAtivo();
            Parcela p = parcela(ParcelaStatus.atrasada, contrato);
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoPago(payment("PIX"));

            assertThat(p.getStatus()).isEqualTo(ParcelaStatus.paga);
            assertThat(contrato.getStatus()).isEqualTo(ContratoStatus.encerrado);
        }

        @Test
        @DisplayName("É idempotente: evento repetido não altera uma parcela já paga")
        void idempotente() {
            Contrato contrato = contratoAtivo();
            Parcela p = parcela(ParcelaStatus.paga, contrato);
            LocalDate dataOriginal = LocalDate.of(2026, 9, 1);
            p.setDataPagamento(dataOriginal);
            p.setMeioPagamento(ParcelaMeiosPagamento.BOLETO);
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoPago(payment("PIX"));

            assertThat(p.getDataPagamento()).isEqualTo(dataOriginal);
            assertThat(p.getMeioPagamento()).isEqualTo(ParcelaMeiosPagamento.BOLETO);
        }

        @Test
        @DisplayName("Não lança exceção quando a parcela não existe")
        void parcelaInexistente() {
            when(query.firstResultOptional()).thenReturn(Optional.empty());

            assertThatCode(() -> parcelaService.marcarComoPago(payment("PIX")))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Contrato cancelado não é reaberto nem encerrado por pagamento tardio")
        void contratoCanceladoPermanece() {
            Contrato contrato = new Contrato();
            contrato.setStatus(ContratoStatus.cancelado);
            Parcela p = parcela(ParcelaStatus.cancelada, contrato);
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoPago(payment("PIX"));

            assertThat(contrato.getStatus()).isEqualTo(ContratoStatus.cancelado);
        }
    }

    @Nested
    @DisplayName("marcarComoAtrasada")
    class MarcarComoAtrasada {

        @Test
        @DisplayName("Marca parcela pendente como atrasada")
        void marcaComoAtrasada() {
            Parcela p = parcela(ParcelaStatus.pendente, contratoAtivo());
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoAtrasada(payment("PIX"));

            assertThat(p.getStatus()).isEqualTo(ParcelaStatus.atrasada);
        }

        @Test
        @DisplayName("Não regride uma parcela já paga")
        void naoRegrideParcelaPaga() {
            Parcela p = parcela(ParcelaStatus.paga, contratoAtivo());
            when(query.firstResultOptional()).thenReturn(Optional.of(p));

            parcelaService.marcarComoAtrasada(payment("PIX"));

            assertThat(p.getStatus()).isEqualTo(ParcelaStatus.paga);
        }

        @Test
        @DisplayName("Não lança exceção quando a parcela não existe")
        void parcelaInexistente() {
            when(query.firstResultOptional()).thenReturn(Optional.empty());

            assertThatCode(() -> parcelaService.marcarComoAtrasada(payment("PIX")))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("buscarIdsParaCobrar devolve apenas os ids das parcelas elegíveis")
    void buscarIdsParaCobrar() {
        Parcela a = new Parcela();
        a.setId(10L);
        Parcela b = new Parcela();
        b.setId(20L);
        when(parcelaRepository.buscarComVencimentoEm7Dias()).thenReturn(List.of(a, b));

        assertThat(parcelaService.buscarIdsParaCobrar()).containsExactly(10L, 20L);
    }
}
