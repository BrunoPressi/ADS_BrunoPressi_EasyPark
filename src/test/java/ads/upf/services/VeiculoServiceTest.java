package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.veiculo.VeiculoCreateDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.VeiculoTipo;
import ads.upf.repositories.VeiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoService - Suíte de Testes Unitários")
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    @Nested
    @DisplayName("Cenários de Salvar Veículo")
    class SalvarVeiculoCenarios {

        @Test
        @DisplayName("Deve salvar veículo com sucesso quando a placa for inédita")
        void deveSalvarVeiculoComSucesso() {
            VeiculoCreateDTO dto = new VeiculoCreateDTO("ABC1D23", VeiculoTipo.carro, "Preto", "VW", "Gol");
            when(veiculoRepository.buscarPelaPlaca("ABC1D23")).thenReturn(Optional.empty());

            veiculoService.salvarVeiculo(dto);

            ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
            verify(veiculoRepository).persist(captor.capture());

            Veiculo salvo = captor.getValue();
            assertThat(salvo.getPlaca()).isEqualTo("ABC1D23");
            assertThat(salvo.getTipo()).isEqualTo(VeiculoTipo.carro);
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException quando o veículo já estiver cadastrado")
        void deveLancarExcecaoQuandoVeiculoJaExistir() {
            VeiculoCreateDTO dto = new VeiculoCreateDTO("ABC1D23", VeiculoTipo.carro, "Preto", "VW", "Gol");
            when(veiculoRepository.buscarPelaPlaca("ABC1D23")).thenReturn(Optional.of(new Veiculo()));

            assertThatThrownBy(() -> veiculoService.salvarVeiculo(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse veículo já está cadastrado.");

            verify(veiculoRepository, never()).persist(any(Veiculo.class));
        }
    }

    @Nested
    @DisplayName("Cenários de Busca por Placa")
    class BuscarVeiculoPelaPlacaCenarios {

        @Test
        @DisplayName("Deve retornar DTO quando encontrar o veículo pela placa")
        void deveRetornarDtoQuandoEncontrar() {
            Veiculo veiculo = new Veiculo();
            veiculo.setPlaca("ABC1D23");
            veiculo.setTipo(VeiculoTipo.carro);

            when(veiculoRepository.buscarPelaPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));

            VeiculoResponseDTO dto = veiculoService.buscarVeiculoPelaPlaca("ABC1D23");

            assertThat(dto).isNotNull();
            assertThat(dto.getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("Deve retornar null quando não encontrar o veículo pela placa")
        void deveRetornarNullQuandoNaoEncontrar() {
            when(veiculoRepository.buscarPelaPlaca("XYZ9999")).thenReturn(Optional.empty());

            VeiculoResponseDTO dto = veiculoService.buscarVeiculoPelaPlaca("XYZ9999");

            assertThat(dto).isNull();
        }

        @ParameterizedTest(name = "Entrada [{0}] deve retornar null")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Deve retornar null sem consultar repositório para placa nula ou vazia")
        void deveRetornarNullParaPlacaNulaOuVazia(String placaInvalida) {
            VeiculoResponseDTO dto = veiculoService.buscarVeiculoPelaPlaca(placaInvalida);

            assertThat(dto).isNull();
            verifyNoInteractions(veiculoRepository);
        }
    }

    @Nested
    @DisplayName("Cenários de Verificar Existência de Veículo")
    class VerificarVeiculoExisteCenarios {

        @Test
        @DisplayName("Deve retornar entidade Veiculo quando existir no banco")
        void deveRetornarVeiculoQuandoExistir() {
            Veiculo veiculo = new Veiculo();
            veiculo.setPlaca("ABC1D23");

            when(veiculoRepository.buscarPelaPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));

            Veiculo resultado = veiculoService.verificarVeiculoExiste("ABC1D23");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando veículo não existir")
        void deveLancarExcecaoQuandoVeiculoNaoExistir() {
            when(veiculoRepository.buscarPelaPlaca("XYZ9999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> veiculoService.verificarVeiculoExiste("XYZ9999"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Veículo não encontrado");
        }
    }

    @Nested
    @DisplayName("Cenários de Obter ou Criar Veículo")
    class ObterOuCriarCenarios {

        @Test
        @DisplayName("Deve retornar veículo existente sem persistir novo registro")
        void deveRetornarVeiculoExistente() {
            Veiculo veiculoExistente = new Veiculo();
            veiculoExistente.setPlaca("ABC1D23");
            veiculoExistente.setTipo(VeiculoTipo.carro);

            when(veiculoRepository.buscarPelaPlaca("ABC1D23")).thenReturn(Optional.of(veiculoExistente));

            Veiculo resultado = veiculoService.obterOuCriar("ABC1D23", VeiculoTipo.carro);

            assertThat(resultado).isSameAs(veiculoExistente);
            verify(veiculoRepository, never()).persist(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve persistir e retornar novo veículo quando não existir")
        void devePersistirENovoVeiculoQuandoNaoExistir() {
            when(veiculoRepository.buscarPelaPlaca("NEW1234")).thenReturn(Optional.empty());

            Veiculo resultado = veiculoService.obterOuCriar("NEW1234", VeiculoTipo.moto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getPlaca()).isEqualTo("NEW1234");
            assertThat(resultado.getTipo()).isEqualTo(VeiculoTipo.moto);

            verify(veiculoRepository).persist(any(Veiculo.class));
        }
    }
}
