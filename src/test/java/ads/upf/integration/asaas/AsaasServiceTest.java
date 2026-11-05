package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.AsaasPaymentListResponse;
import ads.upf.integration.asaas.DTOs.AsaasPaymentRequest;
import ads.upf.integration.asaas.DTOs.AsaasPaymentResponse;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.repositories.ParcelaRepository;
import ads.upf.services.ParcelaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AsaasService - cobrança, exclusão e reconciliação de parcelas")
class AsaasServiceTest {

    private static final Long PARCELA_ID = 42L;

    @Mock
    AsaasClient asaas;

    @Mock
    ParcelaRepository parcelaRepository;

    @Mock
    ParcelaService parcelaService;

    @InjectMocks
    AsaasService asaasService;

    private Parcela parcela;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setAsaasCustomerId("cus_1");

        Contrato contrato = new Contrato();
        contrato.setCliente(cliente);

        parcela = new Parcela();
        parcela.setId(PARCELA_ID);
        parcela.setNumeroParcela(3);
        parcela.setValor(BigDecimal.valueOf(180));
        parcela.setDataVencimento(LocalDate.of(2026, 11, 10));
        parcela.setCobrancaEnviada(false);
        parcela.setStatus(ParcelaStatus.pendente);
        contrato.adicionarParcela(parcela);

        when(parcelaRepository.findById(PARCELA_ID)).thenReturn(parcela);
        when(asaas.buscarCobrancasPorReferencia(String.valueOf(PARCELA_ID)))
                .thenReturn(new AsaasPaymentListResponse(List.of()));
    }

    @Test
    @DisplayName("Cria a cobrança e grava id, link e flag na parcela")
    void criaCobranca() {
        when(asaas.criarCobranca(any()))
                .thenReturn(new AsaasPaymentResponse("pay_new", "PENDING", "https://asaas/fatura"));

        asaasService.cobrarParcela(PARCELA_ID);

        assertThat(parcela.getAsaasPaymentId()).isEqualTo("pay_new");
        assertThat(parcela.getInvoiceUrl()).isEqualTo("https://asaas/fatura");
        assertThat(parcela.getCobrancaEnviada()).isTrue();
    }

    @Test
    @DisplayName("Envia o id da parcela como externalReference")
    void enviaExternalReferenceSoComId() {
        when(asaas.criarCobranca(any()))
                .thenReturn(new AsaasPaymentResponse("pay_new", "PENDING", "url"));

        asaasService.cobrarParcela(PARCELA_ID);

        ArgumentCaptor<AsaasPaymentRequest> captor = ArgumentCaptor.forClass(AsaasPaymentRequest.class);
        verify(asaas).criarCobranca(captor.capture());
        AsaasPaymentRequest req = captor.getValue();
        assertThat(req.externalReference()).isEqualTo("42");
        assertThat(req.customer()).isEqualTo("cus_1");
        assertThat(req.value()).isEqualByComparingTo("180");
        assertThat(req.dueDate()).isEqualTo("2026-11-10");
    }

    @Test
    @DisplayName("Reaproveita cobrança já existente no Asaas em vez de criar duplicada")
    void reaproveitaCobrancaExistente() {
        when(asaas.buscarCobrancasPorReferencia("42")).thenReturn(new AsaasPaymentListResponse(
                List.of(new AsaasPaymentResponse("pay_existente", "PENDING", "https://asaas/existente"))));

        asaasService.cobrarParcela(PARCELA_ID);

        verify(asaas, never()).criarCobranca(any());
        assertThat(parcela.getAsaasPaymentId()).isEqualTo("pay_existente");
        assertThat(parcela.getInvoiceUrl()).isEqualTo("https://asaas/existente");
        assertThat(parcela.getCobrancaEnviada()).isTrue();
    }

    @Test
    @DisplayName("Não faz nada se a parcela já possui cobrança")
    void ignoraParcelaJaCobrada() {
        parcela.setAsaasPaymentId("pay_antigo");

        asaasService.cobrarParcela(PARCELA_ID);

        verify(asaas, never()).buscarCobrancasPorReferencia(any());
        verify(asaas, never()).criarCobranca(any());
        assertThat(parcela.getAsaasPaymentId()).isEqualTo("pay_antigo");
    }

    @Test
    @DisplayName("Não faz nada se a parcela não existe")
    void ignoraParcelaInexistente() {
        when(parcelaRepository.findById(99L)).thenReturn(null);

        asaasService.cobrarParcela(99L);

        verify(asaas, never()).criarCobranca(any());
    }

    @Test
    @DisplayName("deletarCobrancas não lança exceção se o Asaas estiver fora do ar e preserva asaasPaymentId para reprocessamento")
    void deletarCobrancasResilienteQuandoAsaasIndisponivel() {
        parcela.setAsaasPaymentId("pay_1");
        parcela.setInvoiceUrl("https://asaas/1");
        parcela.setCobrancaEnviada(true);
        parcela.setStatus(ParcelaStatus.cancelada);

        doThrow(new RuntimeException("Asaas fora do ar")).when(asaas).deletarCobranca("pay_1");

        assertThatCode(() -> asaasService.deletarCobrancas(List.of(parcela)))
                .doesNotThrowAnyException();

        assertThat(parcela.getAsaasPaymentId()).isEqualTo("pay_1");
    }

    @Test
    @DisplayName("deletarCobrancas limpa asaasPaymentId e invoiceUrl quando deleta com sucesso")
    void deletarCobrancasLimpaDadosQuandoSucesso() {
        parcela.setAsaasPaymentId("pay_1");
        parcela.setInvoiceUrl("https://asaas/1");
        parcela.setCobrancaEnviada(true);
        parcela.setStatus(ParcelaStatus.cancelada);

        asaasService.deletarCobrancas(List.of(parcela));

        verify(asaas).deletarCobranca("pay_1");
        assertThat(parcela.getAsaasPaymentId()).isNull();
        assertThat(parcela.getInvoiceUrl()).isNull();
        assertThat(parcela.getCobrancaEnviada()).isFalse();
    }

    @Test
    @DisplayName("sincronizarParcela marca como paga quando status no Asaas é RECEIVED ou CONFIRMED")
    void sincronizarParcelaMarcaComoPaga() {
        parcela.setAsaasPaymentId("pay_42");
        when(asaas.consultarCobranca("pay_42")).thenReturn(new AsaasPaymentResponse(
                "pay_42", "RECEIVED", "https://asaas/42", "PIX",
                LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5), BigDecimal.valueOf(180)
        ));

        boolean atualizada = asaasService.sincronizarParcela(PARCELA_ID);

        assertThat(atualizada).isTrue();
        verify(parcelaService).marcarComoPago(any());
    }

    @Test
    @DisplayName("sincronizarParcela marca como atrasada quando status no Asaas é OVERDUE")
    void sincronizarParcelaMarcaComoAtrasada() {
        parcela.setAsaasPaymentId("pay_42");
        when(asaas.consultarCobranca("pay_42")).thenReturn(new AsaasPaymentResponse(
                "pay_42", "OVERDUE", "https://asaas/42", "BOLETO",
                null, null, BigDecimal.valueOf(180)
        ));

        boolean atualizada = asaasService.sincronizarParcela(PARCELA_ID);

        assertThat(atualizada).isTrue();
        verify(parcelaService).marcarComoAtrasada(any());
    }
}
