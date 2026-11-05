package ads.upf.services;

import ads.upf.integration.asaas.AsaasService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CobrancaService - job de envio, reconciliação e limpeza de cobranças")
class CobrancaServiceTest {

    @Mock
    AsaasService asaasService;

    @Mock
    ParcelaService parcelaService;

    @InjectMocks
    CobrancaService cobrancaService;

    @Test
    @DisplayName("Cobra todas as parcelas elegíveis")
    void cobraTodas() {
        when(parcelaService.buscarIdsParaCobrar()).thenReturn(List.of(1L, 2L, 3L));

        cobrancaService.enviarCobranca();

        verify(asaasService).cobrarParcela(1L);
        verify(asaasService).cobrarParcela(2L);
        verify(asaasService).cobrarParcela(3L);
    }

    @Test
    @DisplayName("Falha em uma parcela não interrompe as demais")
    void falhaIsolada() {
        when(parcelaService.buscarIdsParaCobrar()).thenReturn(List.of(1L, 2L, 3L));
        doThrow(new RuntimeException("Asaas indisponível")).when(asaasService).cobrarParcela(2L);

        assertThatCode(() -> cobrancaService.enviarCobranca()).doesNotThrowAnyException();

        verify(asaasService).cobrarParcela(1L);
        verify(asaasService).cobrarParcela(2L);
        verify(asaasService).cobrarParcela(3L);
    }

    @Test
    @DisplayName("Não chama o Asaas quando não há parcelas")
    void semParcelas() {
        when(parcelaService.buscarIdsParaCobrar()).thenReturn(List.of());

        cobrancaService.enviarCobranca();

        verify(asaasService, never()).cobrarParcela(anyLong());
    }

    @Test
    @DisplayName("Sincroniza status de parcelas abertas com isolamento de falha por parcela")
    void sincronizaParcelasAbertasComIsolamentoDeFalha() {
        when(parcelaService.buscarIdsAbertasParaSincronizar()).thenReturn(List.of(10L, 20L));
        doThrow(new RuntimeException("Timeout Asaas")).when(asaasService).sincronizarParcela(10L);
        when(asaasService.sincronizarParcela(20L)).thenReturn(true);

        assertThatCode(() -> cobrancaService.sincronizarPagamentosPendentes()).doesNotThrowAnyException();

        verify(asaasService).sincronizarParcela(10L);
        verify(asaasService).sincronizarParcela(20L);
    }

    @Test
    @DisplayName("Remove cobranças de parcelas canceladas pendentes no Asaas com isolamento de falha")
    void removeCobrancasCanceladasPendentes() {
        when(parcelaService.buscarIdsCanceladasParaExcluirNoAsaas()).thenReturn(List.of(5L, 6L));
        doThrow(new RuntimeException("Timeout Asaas")).when(asaasService).removerCobrancaCancelada(5L);

        assertThatCode(() -> cobrancaService.removerCobrancasCanceladas()).doesNotThrowAnyException();

        verify(asaasService).removerCobrancaCancelada(5L);
        verify(asaasService).removerCobrancaCancelada(6L);
    }
}
