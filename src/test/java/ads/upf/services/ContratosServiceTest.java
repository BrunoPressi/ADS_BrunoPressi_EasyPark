package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.repositories.ContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Contratos Service - Suíte de Testes Unitários")
public class ContratosServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private ParcelaService parcelaService;

    @InjectMocks
    private ContratoService contratoService;

    private ContratoCreateDTO dtoContrato = new ContratoCreateDTO();
    private ClienteResponseDTO dtoCliente = new ClienteResponseDTO();
    private VeiculoResponseDTO dtoVeiculo = new VeiculoResponseDTO();

    @BeforeEach
    void setUp() {
        dtoCliente.setCpf("12345678901");
        dtoVeiculo.setPlaca("ABC1D23");

        dtoContrato.setContratoId(1L);
        dtoContrato.setDataInicio(LocalDate.now());
        dtoContrato.setDataTermino(LocalDate.now().plusMonths(6));
        dtoContrato.setCliente(dtoCliente);
        dtoContrato.setVeiculo(dtoVeiculo);
    }

    @Nested
    @DisplayName("Cenários de Salvar / Criar Contrato")
    class SalvarContratoTests {

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(clienteService.verificarClienteExiste(any()))
                    .thenThrow(new EntityNotFoundException("Cliente não encontrado"));

            assertThatThrownBy(() -> contratoService.salvarContrato(dtoContrato))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando veiculo não existir")
        void deveLancarExcecaoQuandoveiculoNaoExiste() {
            when(clienteService.verificarClienteExiste(any())).thenReturn(new Cliente());
            when(veiculoService.verificarVeiculoExiste(any()))
                    .thenThrow(new EntityNotFoundException("Veículo não encontrado"));

            assertThatThrownBy(() -> contratoService.salvarContrato(dtoContrato))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Veículo não encontrado");
        }
    }

    @Nested
    @DisplayName("Cenários de Edição de Contrato")
    class EdicaoContratoTests {

        @Test
        @DisplayName("Não deve permitir edição quando contrato possuir parcelas já pagas")
        void naoDeveEditarContratoComParcelaPaga() {
            Cliente cliente = new Cliente();
            Veiculo veiculo = new Veiculo();

            Contrato contratoExistente = new Contrato();
            contratoExistente.setStatus(ContratoStatus.ativo);

            Parcela parcelaPaga = new Parcela();
            parcelaPaga.setStatus(ParcelaStatus.paga);
            contratoExistente.getParcelas().add(parcelaPaga);

            when(clienteService.verificarClienteExiste(any())).thenReturn(cliente);
            when(veiculoService.verificarVeiculoExiste(any())).thenReturn(veiculo);
            when(parcelaService.gerarParcelas(any(), any())).thenReturn(List.of(new Parcela()));
            when(contratoRepository.findByIdOptional(any())).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.salvarContrato(dtoContrato))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("parcelas já pagas");
        }

        @ParameterizedTest(name = "Entrada com status [{0}] não deve permitir edição")
        @ValueSource(strings = { "encerrado", "inadimplente", "cancelado" })
        @DisplayName("Não deve permitir edição quando contrato possuir status diferente de ativo")
        void naoDeveEditarContratoComStatusNaoAtivo(String status) {
            Cliente cliente = new Cliente();
            Veiculo veiculo = new Veiculo();

            Contrato contratoExistente = new Contrato();
            contratoExistente.setStatus(ContratoStatus.valueOf(status));

            when(clienteService.verificarClienteExiste(any())).thenReturn(cliente);
            when(veiculoService.verificarVeiculoExiste(any())).thenReturn(veiculo);
            when(parcelaService.gerarParcelas(any(), any())).thenReturn(List.of(new Parcela()));
            when(contratoRepository.findByIdOptional(any())).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.salvarContrato(dtoContrato))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Contratos cancelados ou encerrados não podem ser editados");
        }

        @Test
        @DisplayName("Não deve permitir cancelar um contrato quando o mesmo já estiver cancelado")
        void naoDeveCancelarContratoJaCancelado() {
            Contrato contratoExistente = new Contrato();
            contratoExistente.setStatus(ContratoStatus.cancelado);

            when(contratoRepository.findByIdOptional(any())).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.cancelarContrato(1L))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Contratos encerrados ou cancelados não podem ser cancelados!");
        }

        @Test
        @DisplayName("Não deve permitir o cancelamento quando o contrato estiver encerrado")
        void naoDeveCancelarContratoComStatusEncerrado() {
            Contrato contratoExistente = new Contrato();
            contratoExistente.setStatus(ContratoStatus.encerrado);

            when(contratoRepository.findByIdOptional(any())).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.cancelarContrato(1L))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Contratos encerrados ou cancelados não podem ser cancelados!");
        }

        @Test
        @DisplayName("Não deve permitir edição quando o contrato tiver parcelas atrasada")
        void naoDeveCancelarContratoComParcelasAtrasadas() {
            Contrato contratoExistente = new Contrato();
            contratoExistente.setStatus(ContratoStatus.ativo);

            Parcela parcelaAtrasada = new Parcela();
            parcelaAtrasada.setStatus(ParcelaStatus.atrasada);
            contratoExistente.getParcelas().add(parcelaAtrasada);

            when(contratoRepository.findByIdOptional(any())).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.cancelarContrato(1L))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("cancelar um contrato com parcelas atrasadas");
        }

    }

}
