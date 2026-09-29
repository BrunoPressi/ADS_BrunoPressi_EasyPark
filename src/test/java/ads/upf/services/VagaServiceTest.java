package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.TipoVaga;
import ads.upf.model.enums.VagaStatus;
import ads.upf.repositories.VagaRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
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

// AssertJ Core: Importação estática das asserções fluentes.
// Permite ler os testes de forma natural: assertThat(resultado).isEqualTo(...)
// Mockito: Importação estática dos métodos utilitários de simulação e verificação de mocks.

/**
 * =========================================================================================
 * @ExtendWith(MockitoExtension.class) - JUnit 5:
 * Integra o ciclo de vida do JUnit 5 com o Mockito.
 * Inicializa automaticamente os campos anotados com @Mock e fecha os recursos após a execução,
 * eliminando a necessidade de chamar manualmente 'MockitoAnnotations.openMocks(this)'.
 * =========================================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VagaService - Suíte de Testes Unitários")
public class VagaServiceTest {

    /**
     * @Mock - Mockito:
     * Cria um "dublê" (mock) da interface/classe VagaRepository.
     * Nenhuma consulta real ao banco de dados PostgreSQL é realizada.
     * Toda chamada aos métodos deste repositório devolverá valores simulados por nós.
     */
    @Mock
    private VagaRepository vagaRepository;

    /**
     * @Mock - Mockito:
     * Simula a query fluente do Panache (PanacheQuery).
     * Necessário para métodos encadeados como repository.findByNome(...).firstResultOptional()
     * e repository.findAll(...).range(...).list().
     */
    @Mock
    private PanacheQuery<Vaga> panacheQuery;

    /**
     * Objeto Real sob Teste (SUT - System Under Test).
     * Como usamos injeção via construtor no VagaService, instanciamos a classe
     * diretamente passando o mock do repositório, garantindo testes 100% isolados e rápidos.
     */
    private VagaService vagaService;

    /**
     * @BeforeEach - JUnit 5:
     * Garante que antes de CADA teste executado, uma nova instância limpa de VagaService
     * seja criada. Isso evita contaminação de estado entre um teste e outro.
     */
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
            // ARRANGE (Preparação dos dados e comportamento dos mocks):
            VagaCreateDTO dto = new VagaCreateDTO(null, "A01", VagaStatus.disponivel, TipoVaga.comum);

            // Mockito: when(...).thenReturn(...)
            // Ensina ao mock: "Quando chamarem existsByNome('A01', null), retorne false (não existe duplicada)"
            when(vagaRepository.existsByNome("A01", null)).thenReturn(false);

            // ACT (Ação sendo testada):
            vagaService.salvarVaga(dto);

            // ASSERT & VERIFY (Checagem dos resultados):
            // Mockito: ArgumentCaptor permite capturar o objeto exato que foi repassado ao repositório
            // para inspecionarmos se os atributos foram mapeados e atribuídos corretamente.
            ArgumentCaptor<Vaga> captor = ArgumentCaptor.forClass(Vaga.class);

            // Mockito: verify(...)
            // Audita que o método 'persist' do repositório foi chamado exatamente 1 vez com o argumento capturado.
            verify(vagaRepository, times(1)).persist(captor.capture());

            // AssertJ: assertThat(...)
            // Verifica o valor capturado com leitura fluente e mensagens de erro descritivas se falhar.
            Vaga vagaSalva = captor.getValue();
            assertThat(vagaSalva.getNome()).isEqualTo("A01");
            assertThat(vagaSalva.getStatus()).isEqualTo(VagaStatus.disponivel);
            assertThat(vagaSalva.getTipoVaga()).isEqualTo(TipoVaga.comum);
        }

        @Test
        @DisplayName("Deve sanitizar com trim o nome da vaga antes de persistir")
        void deveSanitizarEspacosComTrimAoSalvar() {
            // ARRANGE: Nome com espaços adicionais no início e no fim
            VagaCreateDTO dto = new VagaCreateDTO(null, "  B02  ", VagaStatus.disponivel, TipoVaga.moto);
            when(vagaRepository.existsByNome("B02", null)).thenReturn(false);

            // ACT:
            vagaService.salvarVaga(dto);

            // ASSERT:
            ArgumentCaptor<Vaga> captor = ArgumentCaptor.forClass(Vaga.class);
            verify(vagaRepository).persist(captor.capture());

            // AssertJ: Confirma que os espaços foram removidos antes de salvar
            assertThat(captor.getValue().getNome()).isEqualTo("B02");
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se já existir vaga com o mesmo nome")
        void deveLancarExcecaoQuandoNomeJaExistir() {
            // ARRANGE: Repositório reporta que o nome "A01" já existe no banco
            VagaCreateDTO dto = new VagaCreateDTO(null, "A01", VagaStatus.disponivel, TipoVaga.comum);
            when(vagaRepository.existsByNome("A01", null)).thenReturn(true);

            // ACT & ASSERT:
            // AssertJ: assertThatThrownBy(() -> ...)
            // Executa a lambda e valida que ela lançou uma exceção do tipo EntityExistsException.
            assertThatThrownBy(() -> vagaService.salvarVaga(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe uma vaga com esse nome");

            // Mockito: verify(..., never())
            // Como deu erro de validação, é CRÍTICO checar que 'persist' NUNCA foi chamado (garante integridade).
            verify(vagaRepository, never()).persist(any(Vaga.class));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException se o DTO de entrada for nulo")
        void deveLancarExcecaoSeDtoForNulo() {
            // ACT & ASSERT:
            // Checa a salvaguarda do Objects.requireNonNull no início do método
            assertThatThrownBy(() -> vagaService.salvarVaga(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Os dados da vaga não podem ser nulos");

            // Mockito: verifyNoInteractions(...)
            // Assegura que nenhuma consulta ou persistência foi sequer tentada no repositório.
            verifyNoInteractions(vagaRepository);
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
            // ARRANGE:
            Vaga vagaExistente = new Vaga(10L, "A01", VagaStatus.disponivel, TipoVaga.comum, null, null);
            VagaCreateDTO dto = new VagaCreateDTO(10L, "A01", VagaStatus.em_manutencao, TipoVaga.comum);

            // Simula que o nome "A01" pertence a este mesmo ID (ignora o id 10 na contagem)
            when(vagaRepository.existsByNome("A01", 10L)).thenReturn(false);
            // Simula o encontro da entidade pelo Panache findByIdOptional
            when(vagaRepository.findByIdOptional(10L)).thenReturn(Optional.of(vagaExistente));

            // ACT:
            vagaService.salvarVaga(dto);

            // ASSERT:
            // Como a entidade é gerenciada (managed pelo Hibernate/Panache), as alterações
            // nos setters refletem no objeto encontrado:
            assertThat(vagaExistente.getStatus()).isEqualTo(VagaStatus.em_manutencao);
            assertThat(vagaExistente.getNome()).isEqualTo("A01");

            // Em edições, não deve chamar persist novamente (o Hibernate orquestra pelo flush da transação)
            verify(vagaRepository, never()).persist(any(Vaga.class));
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar editar vaga com ID inexistente")
        void deveLancarExcecaoQuandoIdNaoForEncontrado() {
            // ARRANGE:
            VagaCreateDTO dto = new VagaCreateDTO(999L, "A01", VagaStatus.disponivel, TipoVaga.comum);
            when(vagaRepository.existsByNome("A01", 999L)).thenReturn(false);
            when(vagaRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

            // ACT & ASSERT:
            // Valida que a exceção correta é EntityNotFoundException
            assertThatThrownBy(() -> vagaService.salvarVaga(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Vaga não encontrada");
        }

        @Test
        @DisplayName("Deve lançar InvalidEditException ao tentar alterar uma vaga ocupada")
        void deveBloquearEdicaoDeVagaOcupada() {
            // ARRANGE:
            // Vaga existente está ocupada
            Vaga vagaOcupada = new Vaga(5L, "C01", VagaStatus.ocupada, TipoVaga.comum, null, null);
            // DTO tenta mudar o nome para "C02" enquanto mantém status ocupada
            VagaCreateDTO dtoEditado = new VagaCreateDTO(5L, "C02", VagaStatus.disponivel, TipoVaga.comum);

            when(vagaRepository.existsByNome("C02", 5L)).thenReturn(false);
            when(vagaRepository.findByIdOptional(5L)).thenReturn(Optional.of(vagaOcupada));

            // ACT & ASSERT:
            assertThatThrownBy(() -> vagaService.salvarVaga(dtoEditado))
                    .isInstanceOf(InvalidEditException.class)
                    .hasMessageContaining("Vagas ocupadas não podem ser alteradas.");
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
            // ARRANGE:
            Vaga vaga = new Vaga(1L, "A01", VagaStatus.disponivel, TipoVaga.comum, null, null);

            // Mockito: Stubbing encadeado (fluent API)
            // 1. repository.findByNome("A01") -> retorna o mock da PanacheQuery
            // 2. panacheQuery.firstResultOptional() -> retorna Optional com a vaga
            when(vagaRepository.findByNome("A01")).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(vaga));

            // ACT: Passamos nome com espaços ao redor para testar o .trim() interno
            VagaResponseDTO resultado = vagaService.buscarPeloNome("  A01  ");

            // ASSERT:
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("A01");
            assertThat(resultado.getStatus()).isEqualTo(VagaStatus.disponivel);
            assertThat(resultado.getTipoVaga()).isEqualTo(TipoVaga.comum);
        }

        @Test
        @DisplayName("Deve retornar null quando o repositório não encontrar a vaga pelo nome")
        void deveRetornarNullQuandoNaoEncontrar() {
            // ARRANGE:
            when(vagaRepository.findByNome("Z99")).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());

            // ACT:
            VagaResponseDTO resultado = vagaService.buscarPeloNome("Z99");

            // ASSERT:
            assertThat(resultado).isNull();
        }

        /**
         * @ParameterizedTest - JUnit 5:
         * Executa este mesmo teste várias vezes, uma para cada valor fornecido pelos provedores.
         *
         * @NullAndEmptySource - JUnit 5:
         * Injeta automaticamente 2 valores no teste: null e "" (string vazia).
         *
         * @ValueSource - JUnit 5:
         * Injeta casos de borda adicionais compostos apenas por espaços, tabulações e quebras de linha.
         */
        @ParameterizedTest(name = "Entrada com valor [{0}] deve retornar null com segurança")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "  \r\n  "})
        @DisplayName("Deve retornar null sem disparar NullPointerException para entradas nulas ou em branco")
        void deveRetornarNullSeguroParaValoresNulosOuVazios(String entradaInvalida) {
            // ACT:
            // Se o código tiver o bug 'nome.isBlank() || nome == null', este teste falha com NullPointerException quando entrada == null!
            VagaResponseDTO resultado = vagaService.buscarPeloNome(entradaInvalida);

            // ASSERT:
            assertThat(resultado).isNull();

            // Mockito: Assegura que o repositório nem foi consultado para entradas inválidas
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
            // ARRANGE:
            List<Vaga> listaDoBanco = List.of(
                    new Vaga(1L, "A01", VagaStatus.disponivel, TipoVaga.comum, null, null),
                    new Vaga(2L, "A02", VagaStatus.ocupada, TipoVaga.moto, null, null)
            );

            // Mockito: when com Sort.by("nome")
            when(vagaRepository.listAll(any(Sort.class))).thenReturn(listaDoBanco);

            // ACT:
            List<VagaResponseDTO> resultado = vagaService.listarVagas();

            // ASSERT:
            assertThat(resultado)
                    .isNotNull()
                    .hasSize(2);
            assertThat(resultado.get(0).getNome()).isEqualTo("A01");
            assertThat(resultado.get(1).getNome()).isEqualTo("A02");
        }

        @Test
        @DisplayName("Deve retornar a quantidade total de vagas registradas")
        void deveRetornarContagemTotal() {
            // ARRANGE:
            when(vagaRepository.count()).thenReturn(42L);

            // ACT:
            int total = vagaService.contar();

            // ASSERT:
            assertThat(total).isEqualTo(42);
            verify(vagaRepository).count();
        }

        @Test
        @DisplayName("Deve retornar lista paginada com sucesso dentro dos limites informados")
        void deveListarPaginadoComSucesso() {
            // ARRANGE:
            List<Vaga> paginaMock = List.of(
                    new Vaga(1L, "A01", VagaStatus.disponivel, TipoVaga.comum, null, null)
            );

            when(vagaRepository.findAll(any(Sort.class))).thenReturn(panacheQuery);
            when(panacheQuery.range(0, 9)).thenReturn(panacheQuery);
            when(panacheQuery.list()).thenReturn(paginaMock);

            // ACT: Solicitando página com first=0 e pageSize=10 (range 0 a 9)
            List<VagaResponseDTO> resultado = vagaService.listarPaginado(0, 10);

            // ASSERT:
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getNome()).isEqualTo("A01");

            verify(panacheQuery).range(0, 9);
            verify(panacheQuery).list();
        }

        @Test
        @DisplayName("Deve retornar lista vazia imediatamente se os parâmetros de paginação forem inválidos")
        void deveRetornarListaVaziaParaPaginacaoInvalida() {
            // ACT & ASSERT:
            // 1. first negativo (-1)
            assertThat(vagaService.listarPaginado(-1, 10)).isEmpty();

            // 2. pageSize zerado (0)
            assertThat(vagaService.listarPaginado(0, 0)).isEmpty();

            // 3. pageSize negativo (-5)
            assertThat(vagaService.listarPaginado(0, -5)).isEmpty();

            // Mockito: Garante que nenhuma busca foi executada no banco para parâmetros sem sentido
            verifyNoInteractions(vagaRepository);
        }
    }
}
