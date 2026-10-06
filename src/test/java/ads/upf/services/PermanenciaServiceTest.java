package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.permanencia.PermanenciaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import ads.upf.model.enums.VeiculoTipo;
import ads.upf.repositories.PermanenciaRepository;
import ads.upf.repositories.VagaRepository;
import ads.upf.repositories.VeiculoRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermanenciaService - Suíte de Testes Unitários")
class PermanenciaServiceTest {

    @Mock
    private PermanenciaRepository permanenciaRepository;
    @Mock
    private VagaRepository vagaRepository;
    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private PermanenciaService permanenciaService;

    @Test
    @DisplayName("Deve registrar nova entrada com sucesso para veículo existente")
    void deveRegistrarNovaEntradaComSucesso() {
        PermanenciaCreateDTO dto = new PermanenciaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro);
        Veiculo veiculoExistente = new Veiculo();
        veiculoExistente.setPlaca("ABC1D23");

        Vaga vagaDisponivel = new Vaga();
        vagaDisponivel.setStatus(VagaStatus.disponivel);

        PanacheQuery<Veiculo> veiculoQuery = mock(PanacheQuery.class);
        when(veiculoRepository.find(anyString(), eq("ABC1D23"))).thenReturn(veiculoQuery);
        when(veiculoQuery.firstResultOptional()).thenReturn(Optional.of(veiculoExistente));

        PanacheQuery<Vaga> vagaQuery = mock(PanacheQuery.class);
        when(vagaRepository.find(anyString(), eq(VagaStatus.disponivel), eq(VagaTipo.comum))).thenReturn(vagaQuery);
        when(vagaQuery.firstResultOptional()).thenReturn(Optional.of(vagaDisponivel));

        when(permanenciaRepository.count(anyString(), eq("ABC1D23"), eq(PermanenciaStatus.em_andamento))).thenReturn(0L);

        PermanenciaResponseDTO resultado = permanenciaService.novaEntrada(dto);

        assertThat(resultado).isNotNull();
        assertThat(vagaDisponivel.getStatus()).isEqualTo(VagaStatus.ocupada);
        verify(permanenciaRepository).persist(any(ads.upf.model.entities.Permanencia.class));
    }

    @Test
    @DisplayName("Deve falhar ao tentar registrar veículo já estacionado")
    void deveFalharQuandoVeiculoJaEstacionado() {
        PermanenciaCreateDTO dto = new PermanenciaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro);
        when(permanenciaRepository.count(anyString(), eq("ABC1D23"), eq(PermanenciaStatus.em_andamento))).thenReturn(1L);

        assertThatThrownBy(() -> permanenciaService.novaEntrada(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está com uma entrada em andamento");

        verifyNoInteractions(vagaRepository);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando não houver vaga disponível")
    void deveLancarExcecaoQuandoNaoHouverVaga() {
        PermanenciaCreateDTO dto = new PermanenciaCreateDTO("ABC1D23", VagaTipo.comum, VeiculoTipo.carro);
        when(permanenciaRepository.count(anyString(), eq("ABC1D23"), eq(PermanenciaStatus.em_andamento))).thenReturn(0L);

        PanacheQuery<Veiculo> veiculoQuery = mock(PanacheQuery.class);
        when(veiculoRepository.find(anyString(), eq("ABC1D23"))).thenReturn(veiculoQuery);
        when(veiculoQuery.firstResultOptional()).thenReturn(Optional.of(new Veiculo()));

        PanacheQuery<Vaga> vagaQuery = mock(PanacheQuery.class);
        when(vagaRepository.find(anyString(), eq(VagaStatus.disponivel), eq(VagaTipo.comum))).thenReturn(vagaQuery);
        when(vagaQuery.firstResultOptional()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permanenciaService.novaEntrada(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }
}