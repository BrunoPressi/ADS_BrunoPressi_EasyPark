package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.AsaasPaymentListResponse;
import ads.upf.integration.asaas.DTOs.AsaasPaymentRequest;
import ads.upf.integration.asaas.DTOs.AsaasPaymentResponse;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.repositories.ParcelaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AsaasService - cobrança de parcelas")
class AsaasServiceTest {

    private static final Long PARCELA_ID = 42L;

    @Mock
    AsaasClient asaas;

    @Mock
    ParcelaRepository parcelaRepository;

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
}
