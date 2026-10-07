package ads.upf.services;

import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Permanencia;
import ads.upf.repositories.PermanenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermanenciaService - Suíte de Testes Unitários")
class PermanenciaServiceTest {

    @Mock
    private PermanenciaRepository permanenciaRepository;

    @InjectMocks
    private PermanenciaService permanenciaService;

    @Test
    @DisplayName("Deve retornar contagem total de permanências")
    void deveRetornarContagemTotal() {
        when(permanenciaRepository.count()).thenReturn(25L);

        int total = permanenciaService.contar();

        assertThat(total).isEqualTo(25);
        verify(permanenciaRepository).count();
    }

    @Test
    @DisplayName("Deve listar permanências paginadas com sucesso")
    void deveListarPaginadoComSucesso() {
        Permanencia p = new Permanencia();
        p.setId(1L);

        when(permanenciaRepository.listarPaginado(0, 10)).thenReturn(List.of(p));

        List<PermanenciaResponseDTO> resultado = permanenciaService.listarPaginado(0, 10);

        assertThat(resultado).hasSize(1);
        verify(permanenciaRepository).listarPaginado(0, 10);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para parâmetros de paginação inválidos")
    void deveRetornarListaVaziaParaPaginacaoInvalida() {
        assertThat(permanenciaService.listarPaginado(-1, 10)).isEmpty();
        assertThat(permanenciaService.listarPaginado(0, 0)).isEmpty();
        assertThat(permanenciaService.listarPaginado(0, -5)).isEmpty();

        verifyNoInteractions(permanenciaRepository);
    }
}
