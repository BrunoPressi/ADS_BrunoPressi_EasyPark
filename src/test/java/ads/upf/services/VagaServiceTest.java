package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.vaga.VagaFormDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import ads.upf.repositories.VagaRepository;
import io.quarkus.panache.common.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VagaService - Suíte de Testes Unitários")
public class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    private VagaService vagaService;

    @BeforeEach
    void setUp() {
        vagaService = new VagaService(vagaRepository);
    }

    /*
     * -------------------------------------------------------------------------------------
     * 1. CENÁRIOS DE CRIAÇÃO (salvarVaga com id == null)
     * -------------------------------------------------------------------------------------
     */
    @Nested
    @DisplayName("Cenários de Criação de Vaga (id == null)")
    class CriarVagaCenarios {

        @Test
        @DisplayName("Deve persistir com sucesso uma nova vaga quando os dados forem válidos e o nome for único")
        void deveSalvarNovaVagaComSucesso() {
            VagaFormDTO dto = new VagaFormDTO(null, "A01", VagaStatus.disponivel, VagaTipo.comum);
            when(vagaRepository.existsByNome("A01", null)).thenReturn(false);

            vagaService.salvarVaga(dto);

            ArgumentCaptor<Vaga> captor = ArgumentCaptor.forClass(Vaga.class);
            verify(vagaRepository, times(1)).persist(captor.capture());

            Vaga vagaSalva = captor.getValue();
            assertThat(vagaSalva.getNome()).isEqualTo("A01");
            assertThat(vagaSalva.getStatus()).isEqualTo(VagaStatus.disponivel);
            assertThat(vagaSalva.getTipoVaga()).isEqualTo(VagaTipo.comum);
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se já existir vaga com o mesmo nome")
        void deveLancarExcecaoQuandoNomeJaExistir() {
            VagaFormDTO dto = new VagaFormDTO(null, "A01", VagaStatus.disponivel, VagaTipo.comum);
            when(vagaRepository.existsByNome("A01", null)).thenReturn(true);

            assertThatThrownBy(() -> vagaService.salvarVaga(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe uma vaga com esse nome");

            verify(vagaRepository, never()).persist(any(Vaga.class));
        }
    }

    /*
     * -------------------------------------------------------------------------------------
     * 2. CENÁRIOS DE ATUALIZAÇÃO / EDIÇÃO (salvarVaga com id != null)
     * -------------------------------------------------------------------------------------
     */
    @Nested
    @DisplayName("Cenários de Atualização de Vaga (id != null)")
    class AtualizarVagaCenarios {

        @Test
        @DisplayName("Deve atualizar com sucesso os dados de uma vaga existente mantendo o mesmo nome")
        void deveAtualizarVagaComSucessoMantendoMesmoNome() {
            Vaga vagaExistente = new Vaga(10L, "A01", VagaStatus.disponivel, VagaTipo.comum);
            VagaFormDTO dto = new VagaFormDTO(10L, "A01", VagaStatus.em_manutencao, VagaTipo.comum);

            when(vagaRepository.existsByNome("A01", 10L)).thenReturn(false);
            when(vagaRepository.findByIdOptional(10L)).thenReturn(Optional.of(vagaExistente));

            vagaService.salvarVaga(dto);

            assertThat(vagaExistente.getStatus()).isEqualTo(VagaStatus.em_manutencao);
            assertThat(vagaExistente.getNome()).isEqualTo("A01");

            verify(vagaRepository, never()).persist(any(Vaga.class));
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar editar vaga com ID inexistente")
        void deveLancarExcecaoQuandoIdNaoForEncontrado() {
            VagaFormDTO dto = new VagaFormDTO(999L, "A01", VagaStatus.disponivel, VagaTipo.comum);
            when(vagaRepository.existsByNome("A01", 999L)).thenReturn(false);
            when(vagaRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vagaService.salvarVaga(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Vaga não encontrada");
        }

        @Test
        @DisplayName("Deve lançar InvalidEditException ao tentar alterar uma vaga ocupada")
        void deveBloquearEdicaoDeVagaOcupada() {
            Vaga vagaOcupada = new Vaga(5L, "C01", VagaStatus.ocupada, VagaTipo.comum);
            VagaFormDTO dtoEditado = new VagaFormDTO(5L, "C02", VagaStatus.disponivel, VagaTipo.comum);

            when(vagaRepository.existsByNome("C02", 5L)).thenReturn(false);
            when(vagaRepository.findByIdOptional(5L)).thenReturn(Optional.of(vagaOcupada));

            assertThatThrownBy(() -> vagaService.salvarVaga(dtoEditado))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Vagas ocupadas não podem ser editadas");
        }
    }

    /*
     * -------------------------------------------------------------------------------------
     * 3. CENÁRIOS DE CONSULTA POR NOME (buscarPeloNome) E TESTES DE BORDA
     * -------------------------------------------------------------------------------------
     */
    @Nested
    @DisplayName("Cenários de Busca por Nome")
    class BuscarPeloNomeCenarios {

        @Test
        @DisplayName("Deve retornar VagaResponseDTO quando encontrar a vaga correspondente")
        void deveRetornarDtoAoBuscarNomeExistente() {
            Vaga vaga = new Vaga(1L, "A01", VagaStatus.disponivel, VagaTipo.comum);
            when(vagaRepository.findByNome("A01")).thenReturn(Optional.of(vaga));

            VagaResponseDTO resultado = vagaService.buscarPeloNome("  A01  ");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("A01");
            assertThat(resultado.getStatus()).isEqualTo(VagaStatus.disponivel);
            assertThat(resultado.getTipoVaga()).isEqualTo(VagaTipo.comum);
        }

        @Test
        @DisplayName("Deve retornar null quando o repositório não encontrar a vaga pelo nome")
        void deveRetornarNullQuandoNaoEncontrar() {
            when(vagaRepository.findByNome("Z99")).thenReturn(Optional.empty());

            VagaResponseDTO resultado = vagaService.buscarPeloNome("Z99");

            assertThat(resultado).isNull();
        }

        @ParameterizedTest(name = "Entrada com valor [{0}] deve retornar null com segurança")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "  \r\n  "})
        @DisplayName("Deve retornar null sem disparar NullPointerException para entradas nulas ou em branco")
        void deveRetornarNullSeguroParaValoresNulosOuVazios(String entradaInvalida) {
            VagaResponseDTO resultado = vagaService.buscarPeloNome(entradaInvalida);

            assertThat(resultado).isNull();
            verifyNoInteractions(vagaRepository);
        }
    }

    /*
     * -------------------------------------------------------------------------------------
     * 4. CENÁRIOS DE LISTAGEM E CONTAGEM (listarVagas, contar, listarPaginado)
     * -------------------------------------------------------------------------------------
     */
    @Nested
    @DisplayName("Cenários de Listagem e Paginação")
    class ListagemEPaginacaoCenarios {

        @Test
        @DisplayName("Deve listar todas as vagas ordenadas pelo nome")
        void deveListarTodasAsVagasOrdenadas() {
            List<Vaga> listaDoBanco = List.of(
                    new Vaga(1L, "A01", VagaStatus.disponivel, VagaTipo.comum),
                    new Vaga(2L, "A02", VagaStatus.ocupada, VagaTipo.moto)
            );

            when(vagaRepository.listAll(any(Sort.class))).thenReturn(listaDoBanco);

            List<VagaResponseDTO> resultado = vagaService.listarVagas();

            assertThat(resultado)
                    .isNotNull()
                    .hasSize(2);
            assertThat(resultado.get(0).getNome()).isEqualTo("A01");
            assertThat(resultado.get(1).getNome()).isEqualTo("A02");
        }

        @Test
        @DisplayName("Deve retornar a quantidade total de vagas registradas")
        void deveRetornarContagemTotal() {
            when(vagaRepository.count()).thenReturn(42L);

            int total = vagaService.contar();

            assertThat(total).isEqualTo(42);
            verify(vagaRepository).count();
        }

        @Test
        @DisplayName("Deve retornar lista paginada com sucesso dentro dos limites informados")
        void deveListarPaginadoComSucesso() {
            List<Vaga> paginaMock = List.of(
                    new Vaga(1L, "A01", VagaStatus.disponivel, VagaTipo.comum)
            );

            when(vagaRepository.listarPaginado(0, 10)).thenReturn(paginaMock);

            List<VagaResponseDTO> resultado = vagaService.listarPaginado(0, 10);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getNome()).isEqualTo("A01");
            verify(vagaRepository).listarPaginado(0, 10);
        }

        @Test
        @DisplayName("Deve retornar lista vazia imediatamente se os parâmetros de paginação forem inválidos")
        void deveRetornarListaVaziaParaPaginacaoInvalida() {
            assertThat(vagaService.listarPaginado(-1, 10)).isEmpty();
            assertThat(vagaService.listarPaginado(0, 0)).isEmpty();
            assertThat(vagaService.listarPaginado(0, -5)).isEmpty();

            verifyNoInteractions(vagaRepository);
        }
    }

    /*
     * -------------------------------------------------------------------------------------
     * 5. CENÁRIOS DE OCUPAÇÃO E LIBERAÇÃO DE VAGA
     * -------------------------------------------------------------------------------------
     */
    @Nested
    @DisplayName("Cenários de Ocupação e Liberação")
    class OcupacaoELiberacaoCenarios {

        @Test
        @DisplayName("Deve ocupar vaga disponível com sucesso")
        void deveOcuparVagaComSucesso() {
            Vaga vaga = new Vaga(1L, "A01", VagaStatus.disponivel, VagaTipo.comum);
            when(vagaRepository.findByStatusAndTipo(VagaTipo.comum)).thenReturn(Optional.of(vaga));

            Vaga ocupada = vagaService.ocuparVaga(VagaTipo.comum);

            assertThat(ocupada).isNotNull();
            assertThat(ocupada.getStatus()).isEqualTo(VagaStatus.ocupada);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando não houver vaga disponível para o tipo")
        void deveLancarExcecaoQuandoNaoHouverVagaDisponivel() {
            when(vagaRepository.findByStatusAndTipo(VagaTipo.moto)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vagaService.ocuparVaga(VagaTipo.moto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Vaga não encontrada");
        }

        @Test
        @DisplayName("Deve liberar vaga com sucesso")
        void deveLiberarVagaComSucesso() {
            Vaga vaga = new Vaga(1L, "A01", VagaStatus.ocupada, VagaTipo.comum);
            when(vagaRepository.findByIdOptional(1L)).thenReturn(Optional.of(vaga));

            vagaService.liberarVaga(1L);

            assertThat(vaga.getStatus()).isEqualTo(VagaStatus.disponivel);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar liberar vaga inexistente")
        void deveLancarExcecaoAoLiberarVagaInexistente() {
            when(vagaRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vagaService.liberarVaga(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Vaga não encontrada");
        }
    }
}
