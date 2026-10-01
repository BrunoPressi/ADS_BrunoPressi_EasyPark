package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.repositories.ClienteRepository;
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
    private ClienteRepository clienteRepository;

    @Mock
    private ParcelaService parcelaService;

    @InjectMocks
    private ContratoService contratoService;

    private ClienteResponseDTO dtoCliente = new ClienteResponseDTO();

    @BeforeEach
    void setUp() {
        dtoCliente.setId(1L);
        dtoCliente.setNomeCompleto("John Doe");
        dtoCliente.setEmail("john@email.com");
        dtoCliente.setCpf("04793026001");
    }

    @Nested
    @DisplayName("Cenários de Salvar / Criar Contrato")
    class SalvarContratoTests {

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            ContratoCreateDTO dto = new ContratoCreateDTO(2L, LocalDate.now(), LocalDate.now().plusMonths(3), dtoCliente);
            when(clienteRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contratoService.salvarContrato(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }
    }

    @Nested
    @DisplayName("Cenários de Edição de Contrato")
    class EdicaoContratoTests {

        @Test
        @DisplayName("Não deve permitir edição quando contrato possuir parcelas já pagas")
        void naoDeveEditarContratoComParcelaPaga() {

            ContratoCreateDTO dtoContrato = new ContratoCreateDTO(10L,
                    LocalDate.now(),
                    LocalDate.now().plusMonths(6),
                    dtoCliente);

            Cliente cliente = new Cliente();

            Contrato contratoExistente = new Contrato();
            contratoExistente.setId(9999L);
            contratoExistente.setStatus(ContratoStatus.ativo);
            contratoExistente.setDataInicio(LocalDate.now());
            contratoExistente.setDataTermino(LocalDate.now().plusMonths(6));

            Parcela parcelaPaga = new Parcela();
            parcelaPaga.setStatus(ParcelaStatus.paga);
            contratoExistente.getParcelas().add(parcelaPaga);

            when(clienteRepository.findByIdOptional(1L)).thenReturn(Optional.of(cliente));
            when(contratoRepository.findByIdOptional(10L)).thenReturn(Optional.of(contratoExistente));
            when(parcelaService.gerarParcelas(any(), any())).thenReturn(List.of(new Parcela()));

            assertThatThrownBy(() -> contratoService.salvarContrato(dtoContrato))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("parcelas já pagas");
        }

        @ParameterizedTest(name = "Entrada com status [{0}] não deve permitir edição")
        @ValueSource(strings = { "encerrado", "inadimplente", "cancelado" })
        @DisplayName("Não deve permitir edição quando contrato possuir status diferente de ativo")
        void naoDeveEditarContratoComStatusNaoAtivo(String status) {

            ContratoCreateDTO dtoContrato = new ContratoCreateDTO(10L,
                    LocalDate.now(),
                    LocalDate.now().plusMonths(6),
                    dtoCliente);

            Cliente cliente = new Cliente();

            Contrato contratoExistente = new Contrato();
            contratoExistente.setId(9999L);
            contratoExistente.setStatus(ContratoStatus.valueOf(status));
            contratoExistente.setDataInicio(LocalDate.now());
            contratoExistente.setDataTermino(LocalDate.now().plusMonths(6));

            Parcela parcelaPaga = new Parcela();
            parcelaPaga.setStatus(ParcelaStatus.paga);
            contratoExistente.getParcelas().add(parcelaPaga);

            when(clienteRepository.findByIdOptional(1L)).thenReturn(Optional.of(cliente));
            when(contratoRepository.findByIdOptional(10L)).thenReturn(Optional.of(contratoExistente));
            when(parcelaService.gerarParcelas(any(), any())).thenReturn(List.of(new Parcela()));

            assertThatThrownBy(() -> contratoService.salvarContrato(dtoContrato))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("contrato que não esteja ativo.");
        }

        @Test
        @DisplayName("Não deve permitir cancelar um contrato quando o mesmo já estiver cancelado")
        void naoDeveCancelarContratoJaCancelado() {

            ContratoResponseDTO contratoResponseDTO = new ContratoResponseDTO();
            contratoResponseDTO.setId(1l);
            contratoResponseDTO.setStatus(ContratoStatus.cancelado);

            Contrato contratoExistente = new Contrato();
            contratoExistente.setId(1L);
            contratoExistente.setStatus(ContratoStatus.cancelado);

            when(contratoRepository.findByIdOptional(1L)).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.cancelarContrato(contratoResponseDTO))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Contratos encerrados ou cancelados não podem ser cancelados!");
        }

        @Test
        @DisplayName("Não deve permitir o cancelamento quando o contrato estiver encerrado")
        void naoDeveCancelarContratoComStatusEncerrado() {

            ContratoResponseDTO contratoResponseDTO = new ContratoResponseDTO();
            contratoResponseDTO.setId(1l);
            contratoResponseDTO.setStatus(ContratoStatus.encerrado);

            Contrato contratoExistente = new Contrato();
            contratoExistente.setId(1L);
            contratoExistente.setStatus(ContratoStatus.encerrado);

            Parcela parcelaPaga = new Parcela();
            parcelaPaga.setStatus(ParcelaStatus.paga);
            contratoExistente.getParcelas().add(parcelaPaga);

            when(contratoRepository.findByIdOptional(1L)).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.cancelarContrato(contratoResponseDTO))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Contratos encerrados ou cancelados não podem ser cancelados!");
        }

        @Test
        @DisplayName("Não deve permitir edição quando o contrato tiver parcelas atrasada")
        void naoDeveCancelarContratoComParcelasAtrasadas() {

            ContratoResponseDTO contratoResponseDTO = new ContratoResponseDTO();
            contratoResponseDTO.setId(1l);
            contratoResponseDTO.setStatus(ContratoStatus.ativo);

            Contrato contratoExistente = new Contrato();
            contratoExistente.setId(1L);
            contratoExistente.setStatus(ContratoStatus.ativo);

            Parcela parcelaPaga = new Parcela();
            parcelaPaga.setStatus(ParcelaStatus.atrasada);
            contratoExistente.getParcelas().add(parcelaPaga);

            when(contratoRepository.findByIdOptional(1L)).thenReturn(Optional.of(contratoExistente));

            assertThatThrownBy(() -> contratoService.cancelarContrato(contratoResponseDTO))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("cancelar um contrato com parcelas atrasadas");
        }

    }

}
