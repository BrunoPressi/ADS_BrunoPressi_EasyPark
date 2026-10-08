package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.permanencia.EntradaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.Permanencia;
import ads.upf.model.entities.Vaga;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import ads.upf.model.enums.VeiculoTipo;
import ads.upf.repositories.PermanenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("EntradaUseCase - Suíte de Testes Unitários")
class EntradaUseCaseTest {

    @Mock
    private PermanenciaRepository permanenciaRepository;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private VagaService vagaService;

    @Mock
    private ContratoService contratoService;

    @Mock
    private FuncionarioService funcionarioService;

    @InjectMocks
    private EntradaUseCase entradaUseCase;

    @Test
    @DisplayName("Deve registrar nova entrada com sucesso para veículo válido (rotativo)")
    void deveRegistrarNovaEntradaComSucessoRotativo() {
        EntradaCreateDTO dto = new EntradaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro, true);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca("ABC1D23");

        Vaga vaga = new Vaga();
        vaga.setStatus(VagaStatus.ocupada);

        when(permanenciaRepository.verificarVeiculoEstacionado("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoService.obterOuCriar("ABC1D23", VeiculoTipo.carro)).thenReturn(veiculo);
        when(vagaService.ocuparVaga(VagaTipo.comum)).thenReturn(vaga);

        PermanenciaResponseDTO resultado = entradaUseCase.novaEntrada(dto);

        assertThat(resultado).isNotNull();

        ArgumentCaptor<Permanencia> captor = ArgumentCaptor.forClass(Permanencia.class);
        verify(permanenciaRepository).persist(captor.capture());

        Permanencia permanenciaSalva = captor.getValue();
        assertThat(permanenciaSalva.getStatus()).isEqualTo(PermanenciaStatus.em_andamento);
        assertThat(permanenciaSalva.getVeiculo()).isEqualTo(veiculo);
        assertThat(permanenciaSalva.getVaga()).isEqualTo(vaga);
        assertThat(permanenciaSalva.getDataEntrada()).isNotNull();
        assertThat(permanenciaSalva.getIsRotativo()).isTrue();
    }

    @Test
    @DisplayName("Deve registrar nova entrada com sucesso para veículo válido (mensalista)")
    void deveRegistrarNovaEntradaComSucessoMensalista() {
        EntradaCreateDTO dto = new EntradaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro, false);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca("ABC1D23");

        Vaga vaga = new Vaga();
        vaga.setStatus(VagaStatus.ocupada);

        when(permanenciaRepository.verificarVeiculoEstacionado("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoService.obterOuCriar("ABC1D23", VeiculoTipo.carro)).thenReturn(veiculo);
        when(vagaService.ocuparVaga(VagaTipo.comum)).thenReturn(vaga);

        PermanenciaResponseDTO resultado = entradaUseCase.novaEntrada(dto);

        assertThat(resultado).isNotNull();

        ArgumentCaptor<Permanencia> captor = ArgumentCaptor.forClass(Permanencia.class);
        verify(permanenciaRepository).persist(captor.capture());

        Permanencia permanenciaSalva = captor.getValue();
        assertThat(permanenciaSalva.getStatus()).isEqualTo(PermanenciaStatus.em_andamento);
        assertThat(permanenciaSalva.getVeiculo()).isEqualTo(veiculo);
        assertThat(permanenciaSalva.getVaga()).isEqualTo(vaga);
        assertThat(permanenciaSalva.getDataEntrada()).isNotNull();
        assertThat(permanenciaSalva.getIsRotativo()).isTrue();
    }


    @Test
    @DisplayName("Deve falhar ao tentar registrar veículo que já está com entrada em andamento")
    void deveFalharQuandoVeiculoJaEstacionado() {
        EntradaCreateDTO dto = new EntradaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro, true);
        when(permanenciaRepository.verificarVeiculoEstacionado("ABC1D23")).thenReturn(Optional.of(new Permanencia()));

        assertThatThrownBy(() -> entradaUseCase.novaEntrada(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está com uma entrada em andamento");

        verifyNoInteractions(funcionarioService);
        verifyNoInteractions(veiculoService);
        verifyNoInteractions(vagaService);
    }

    @Test
    @DisplayName("Deve propagar EntityNotFoundException quando não houver vaga disponível")
    void deveLancarExcecaoQuandoNaoHouverVaga() {
        EntradaCreateDTO dto = new EntradaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro, false);

        when(permanenciaRepository.verificarVeiculoEstacionado("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoService.obterOuCriar("ABC1D23", VeiculoTipo.carro)).thenReturn(new Veiculo());
        when(vagaService.ocuparVaga(VagaTipo.comum))
                .thenThrow(new EntityNotFoundException("Vaga não encontrada"));

        assertThatThrownBy(() -> entradaUseCase.novaEntrada(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vaga não encontrada");

        verify(permanenciaRepository, never()).persist(any(Permanencia.class));
    }
}
