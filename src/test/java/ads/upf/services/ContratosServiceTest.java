package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.StatusParcela;
import ads.upf.repositories.ClienteRepository;
import ads.upf.repositories.ContratoRepository;
import ads.upf.repositories.ParcelaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private ParcelaRepository parcelaRepository;

    @InjectMocks
    private ContratoService contratoService;

    @Nested
    @DisplayName("Cenários de Salvar / Criar Contrato")
    class SalvarContratoTests {

        @Test
        @DisplayName("Deve lançar exceção se período for inferior a 1 mês")
        void deveLancarExcecaoQuandoPeriodoInvalido() {
            ContratoCreateDTO dto = new ContratoCreateDTO(null, LocalDate.now(), LocalDate.now().plusDays(10), 1L);
            assertThatThrownBy(() -> contratoService.salvarContrato(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duração mínima de 1 mês");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            ContratoCreateDTO dto = new ContratoCreateDTO(null, LocalDate.now(), LocalDate.now().plusMonths(3), 99L);
            when(clienteRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

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
            ContratoCreateDTO dto = new ContratoCreateDTO(10L, LocalDate.now(), LocalDate.now().plusMonths(6), 1L);
            Cliente cliente = new Cliente();

            Contrato contratoExistente = new Contrato();
            contratoExistente.setStatus(ContratoStatus.ativo);

            Parcela parcelaPaga = new Parcela();
            parcelaPaga.setStatus(StatusParcela.paga);
            contratoExistente.getParcelas().add(parcelaPaga);

            when(clienteRepository.findByIdOptional(1L)).thenReturn(Optional.of(cliente));
            when(contratoRepository.findByIdOptional(10L)).thenReturn(Optional.of(contratoExistente));
            when(parcelaService.gerarParcelas(any(), any())).thenReturn(List.of(new Parcela()));

            assertThatThrownBy(() -> contratoService.salvarContrato(dto))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("parcelas já pagas");
        }
    }

}
