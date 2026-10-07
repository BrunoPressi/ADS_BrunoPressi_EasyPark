package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Pagamento;
import ads.upf.model.entities.Permanencia;
import ads.upf.model.entities.Vaga;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.repositories.PermanenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaidaUseCase - Suíte de Testes Unitários")
class SaidaUseCaseTest {

    @Mock
    private PermanenciaRepository permanenciaRepository;

    @Mock
    private VagaService vagaService;

    @Mock
    private ContratoService contratoService;

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private SaidaUseCase saidaUseCase;

    @Test
    @DisplayName("Deve registrar saída com sucesso gerando pagamento quando veículo não tiver contrato ativo")
    void deveRegistrarSaidaComGeracaoDePagamento() {
        String placa = "ABC1D23";

        Vaga vaga = new Vaga();
        vaga.setId(10L);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placa);

        Permanencia permanencia = new Permanencia();
        permanencia.setId(1L);
        permanencia.setStatus(PermanenciaStatus.em_andamento);
        permanencia.setDataEntrada(LocalDateTime.now().minusHours(2));
        permanencia.setVaga(vaga);
        permanencia.setVeiculo(veiculo);

        Pagamento pagamento = new Pagamento();
        pagamento.setValor(BigDecimal.valueOf(140.00));

        when(permanenciaRepository.verificarVeiculoEstacionado(placa)).thenReturn(Optional.of(permanencia));
        when(contratoService.verificarPossuiContratoAtivo(placa)).thenReturn(false);
        when(pagamentoService.gerarPagamento(eq(permanencia.getDataEntrada()), any(LocalDateTime.class)))
                .thenReturn(pagamento);

        PermanenciaResponseDTO resultado = saidaUseCase.novaSaida(placa);

        assertThat(resultado).isNotNull();
        assertThat(permanencia.getDataSaida()).isNotNull();
        assertThat(permanencia.getPagamento()).isEqualTo(pagamento);

        verify(vagaService).liberarVaga(10L);
    }

    @Test
    @DisplayName("Deve registrar saída sem gerar cobrança quando veículo possuir contrato ativo")
    void deveRegistrarSaidaSemPagamentoQuandoPossuirContrato() {
        String placa = "ABC1D23";

        Vaga vaga = new Vaga();
        vaga.setId(10L);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placa);

        Permanencia permanencia = new Permanencia();
        permanencia.setId(1L);
        permanencia.setStatus(PermanenciaStatus.em_andamento);
        permanencia.setDataEntrada(LocalDateTime.now().minusHours(2));
        permanencia.setVaga(vaga);
        permanencia.setVeiculo(veiculo);

        when(permanenciaRepository.verificarVeiculoEstacionado(placa)).thenReturn(Optional.of(permanencia));
        when(contratoService.verificarPossuiContratoAtivo(placa)).thenReturn(true);

        PermanenciaResponseDTO resultado = saidaUseCase.novaSaida(placa);

        assertThat(resultado).isNotNull();
        assertThat(permanencia.getDataSaida()).isNotNull();
        verifyNoInteractions(pagamentoService);
        verify(vagaService).liberarVaga(10L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando permanência ativa não for encontrada pela placa")
    void deveLancarExcecaoQuandoPermanenciaNaoEncontrada() {
        when(permanenciaRepository.verificarVeiculoEstacionado("XYZ9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saidaUseCase.novaSaida("XYZ9999"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Permanência não encontrada");

        verifyNoInteractions(pagamentoService);
        verifyNoInteractions(vagaService);
    }
}
