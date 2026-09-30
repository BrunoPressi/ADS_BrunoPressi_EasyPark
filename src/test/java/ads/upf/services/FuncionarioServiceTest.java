package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.enums.UsuarioRole;
import ads.upf.repositories.FuncionarioRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * =========================================================================================
 * @ExtendWith(MockitoExtension.class) - JUnit 5:
 * Habilita a extensão do Mockito no JUnit 5.
 * Inicializa automaticamente os campos anotados com @Mock e gerencia o ciclo de vida dos dublês.
 * =========================================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionarioService - Suíte de Testes Unitários")
public class FuncionarioServiceTest {

    /**
     * @Mock - Mockito:
     * Dublê do FuncionarioRepository para simular operações de banco de dados
     * sem conectar ao banco real PostgreSQL.
     */
    @Mock
    private FuncionarioRepository funcionarioRepository;

    /**
     * @Mock - Mockito:
     * Dublê do PanacheQuery para simular consultas fluentes e paginadas do Hibernate Panache.
     */
    @Mock
    private PanacheQuery<Funcionario> panacheQuery;

    /**
     * Objeto real sob teste (SUT - System Under Test).
     */
    private FuncionarioService funcionarioService;

    /**
     * @BeforeEach - JUnit 5:
     * Executado antes de CADA teste. Cria uma nova instância isolada de FuncionarioService,
     * garantindo independência total entre os testes.
     */
    @BeforeEach
    void setUp() {
        funcionarioService = new FuncionarioService(funcionarioRepository);
    }

    // -------------------------------------------------------------------------------------
    // Métodos utilitários auxiliares (Helpers) para montar cenários de teste
    // -------------------------------------------------------------------------------------
    private FuncionarioCreateDTO criarDto(Long id, String nome, String email, String telefone) {
        FuncionarioCreateDTO dto = new FuncionarioCreateDTO();
        dto.setId(id);
        dto.setNomeCompleto(nome);
        dto.setEmail(email);
        dto.setTelefone(telefone);
        return dto;
    }

    private Funcionario criarEntidade(Long id, String nome, String email, String telefone) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNomeCompleto(nome);
        f.setEmail(email);
        f.setTelefone(telefone);
        f.setSenha("$2a$10$abcdefghijklmnopqrstuvwx"); // Hash BCrypt fictício
        f.setRole(UsuarioRole.atendente);
        return f;
    }

    /*
     * =====================================================================================
     * 1. CENÁRIOS DE CRIAÇÃO (salvarFuncionario com id == null)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Criação de Funcionário (id == null)")
    class CriarFuncionarioCenarios {

        @Test
        @DisplayName("Deve persistir com sucesso um novo funcionário com dados válidos e senha gerada")
        void deveSalvarNovoFuncionarioComSucesso() {
            // ARRANGE:
            FuncionarioCreateDTO dto = criarDto(null, "Carlos Silva", "carlos@email.com", "54999999999");

            // Mockito: when(...).thenReturn(...)
            // Ensina ao mock que não existe funcionário prévio com esse email nem com esse nome
            when(funcionarioRepository.existsByEmail("carlos@email.com", null)).thenReturn(false);

            // ACT:
            funcionarioService.salvarFuncionario(dto);

            // ASSERT & VERIFY:
            // Mockito: ArgumentCaptor intercepta a entidade exata que foi enviada ao persist
            ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);

            // Mockito: verify(...) garante que persist foi chamado exatamente 1 vez
            verify(funcionarioRepository, times(1)).persist(captor.capture());

            Funcionario funcionarioSalvo = captor.getValue();

            // AssertJ: assertThat(...) validações de integridade dos dados gravados
            assertThat(funcionarioSalvo.getNomeCompleto()).isEqualTo("Carlos Silva");
            assertThat(funcionarioSalvo.getEmail()).isEqualTo("carlos@email.com");
            assertThat(funcionarioSalvo.getTelefone()).isEqualTo("54999999999");

            // Valida que uma senha com hash não nulo foi gerada pelo SenhaGenerator
            assertThat(funcionarioSalvo.getSenha())
                    .isNotNull()
                    .isNotBlank();
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o email já estiver em uso")
        void deveLancarExcecaoQuandoEmailJaExistir() {
            // ARRANGE: Repositório reporta que o email já pertence a outro usuário
            FuncionarioCreateDTO dto = criarDto(null, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.existsByEmail("carlos@email.com", null)).thenReturn(true);

            // ACT & ASSERT:
            // AssertJ: assertThatThrownBy captura a exceção e valida tipo e mensagem
            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe um funcionário com esse email");

            // Mockito: verify(..., never()) garante que persist NUNCA foi chamado (proteção de banco)
            verify(funcionarioRepository, never()).persist(any(Funcionario.class));
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o nome já estiver em uso")
        void deveLancarExcecaoQuandoNomeJaExistir() {
            // ARRANGE: Email livre, mas nome já cadastrado
            FuncionarioCreateDTO dto = criarDto(null, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.existsByEmail("carlos@email.com", null)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Silva", null)).thenReturn(true);

            // ACT & ASSERT:
            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe um funcionário com esse nome");

            verify(funcionarioRepository, never()).persist(any(Funcionario.class));
        }

    }

    /*
     * =====================================================================================
     * 2. CENÁRIOS DE ATUALIZAÇÃO (salvarFuncionario com id != null)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Atualização de Funcionário (id != null)")
    class AtualizarFuncionarioCenarios {

        @Test
        @DisplayName("Deve atualizar os dados do funcionário com sucesso para ID existente")
        void deveAtualizarFuncionarioComSucesso() {
            // ARRANGE:
            Funcionario funcionarioExistente = criarEntidade(10L, "Carlos Antigo", "antigo@email.com", "54111111111");
            FuncionarioCreateDTO dtoEdicao = criarDto(10L, "Carlos Novo", "novo@email.com", "54222222222");

            when(funcionarioRepository.existsByEmail("novo@email.com", 10L)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Novo", 10L)).thenReturn(false);
            when(funcionarioRepository.findByIdOptional(10L)).thenReturn(Optional.of(funcionarioExistente));

            // ACT:
            funcionarioService.salvarFuncionario(dtoEdicao);

            // ASSERT:
            // O objeto gerenciado pelo Hibernate reflete as alterações
            assertThat(funcionarioExistente.getNomeCompleto()).isEqualTo("Carlos Novo");
            assertThat(funcionarioExistente.getEmail()).isEqualTo("novo@email.com");
            assertThat(funcionarioExistente.getTelefone()).isEqualTo("54222222222");

            // Em atualizações, persist não deve ser invocado novamente
            verify(funcionarioRepository, never()).persist(any(Funcionario.class));
        }

        @Test
        @DisplayName("Deve atualizar mantendo o mesmo email e nome sem acusar duplicidade contra si mesmo")
        void deveAtualizarMantendoMesmoEmailENome() {
            // ARRANGE:
            Funcionario funcionarioExistente = criarEntidade(10L, "Carlos Silva", "carlos@email.com", "54111111111");
            // Altera apenas o telefone mantendo nome e email
            FuncionarioCreateDTO dtoEdicao = criarDto(10L, "Carlos Silva", "carlos@email.com", "54999999999");

            // existsByEmail e existsByNome recebem o ignoreId = 10L e retornam false (não colide com outros)
            when(funcionarioRepository.existsByEmail("carlos@email.com", 10L)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Silva", 10L)).thenReturn(false);
            when(funcionarioRepository.findByIdOptional(10L)).thenReturn(Optional.of(funcionarioExistente));

            // ACT:
            funcionarioService.salvarFuncionario(dtoEdicao);

            // ASSERT:
            assertThat(funcionarioExistente.getTelefone()).isEqualTo("54999999999");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar atualizar funcionário inexistente")
        void deveLancarEntityNotFoundExceptionQuandoIdNaoExistir() {
            // ARRANGE:
            FuncionarioCreateDTO dto = criarDto(999L, "Carlos Silva", "carlos@email.com", "54999999999");

            when(funcionarioRepository.existsByEmail("carlos@email.com", 999L)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Silva", 999L)).thenReturn(false);
            when(funcionarioRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

            // ACT & ASSERT:
            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Funcionário não encontrado");
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o email pertencer a outro funcionário na atualização")
        void deveLancarExcecaoQuandoEmailColidirComOutroId() {
            // ARRANGE:
            FuncionarioCreateDTO dto = criarDto(10L, "Carlos Silva", "maria@email.com", "54999999999");
            // Mock indica que o email já pertence a outro ID
            when(funcionarioRepository.existsByEmail("maria@email.com", 10L)).thenReturn(true);

            // ACT & ASSERT:
            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe um funcionário com esse email");

            // Mockito: Garante que nem buscou pelo ID pois falhou na validação de unicidade
            verify(funcionarioRepository, never()).findByIdOptional(anyLong());
        }
    }

    /*
     * =====================================================================================
     * 3. CENÁRIOS DE BUSCA POR TERMO E CASOS DE BORDA (buscarPorTermo)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Busca por Termo")
    class BuscarPorTermoCenarios {

        @Test
        @DisplayName("Deve buscar por ID numérico quando o termo for composto apenas por números")
        void deveBuscarPorIdQuandoTermoForNumerico() {
            // ARRANGE:
            Funcionario funcionario = criarEntidade(15L, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.findByIdOptional(15L)).thenReturn(Optional.of(funcionario));

            // ACT:
            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo("15");

            // ASSERT:
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(15L);
            assertThat(resultado.getNomeCompleto()).isEqualTo("Carlos Silva");

            verify(funcionarioRepository, times(1)).findByIdOptional(15L);
            verify(funcionarioRepository, never()).findByNome(anyString());
        }

        @Test
        @DisplayName("Deve buscar por nome quando o termo for textual")
        void deveBuscarPorNomeQuandoTermoForTexto() {
            // ARRANGE:
            Funcionario funcionario = criarEntidade(1L, "Carlos Silva", "carlos@email.com", "54999999999");

            // Mock da consulta fluente: repository.findByNome(...) -> panacheQuery.firstResultOptional()
            when(funcionarioRepository.findByNome("Carlos")).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(funcionario));

            // ACT:
            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo("  Carlos  ");

            // ASSERT:
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNomeCompleto()).isEqualTo("Carlos Silva");

            verify(funcionarioRepository, times(1)).findByNome("Carlos");
        }

        @Test
        @DisplayName("Não deve lançar NumberFormatException quando o termo numérico exceder a capacidade de Long")
        void deveProtegerContraOverflowNumericoSemQuebrar() {
            // ARRANGE: Número com 29 dígitos (excede Long.MAX_VALUE)
            String numeroGigante = "99999999999999999999999999999";

            when(funcionarioRepository.findByNome(numeroGigante)).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());

            // ACT:
            // A regex ^\\d{1,18}$ evita Long.valueOf em números maiores que 18 dígitos, redirecionando para busca por texto
            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo(numeroGigante);

            // ASSERT:
            assertThat(resultado).isNull();
            verify(funcionarioRepository, never()).findByIdOptional(anyLong());
            verify(funcionarioRepository, times(1)).findByNome(numeroGigante);
        }

        /**
         * @ParameterizedTest - JUnit 5:
         * Executa o teste múltiplas vezes para cobrir casos de borda sem repetição de código.
         */
        @ParameterizedTest(name = "Entrada [{0}] deve retornar null com segurança")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", " \t \n "})
        @DisplayName("Deve retornar null sem consultar repositório para entradas vazias, nulas ou com espaços")
        void deveRetornarNullParaTermosNulosOuVazios(String entradaInvalida) {
            // ACT:
            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo(entradaInvalida);

            // ASSERT:
            assertThat(resultado).isNull();

            // Mockito: Assegura que nenhuma consulta foi disparada ao banco
            verifyNoInteractions(funcionarioRepository);
        }

        @Test
        @DisplayName("Deve retornar null quando não encontrar nenhum funcionário pelo termo pesquisado")
        void deveRetornarNullQuandoNaoEncontrarPorNome() {
            // ARRANGE:
            when(funcionarioRepository.findByNome("inexistente")).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());

            // ACT:
            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo("inexistente");

            // ASSERT:
            assertThat(resultado).isNull();
        }
    }

    /*
     * =====================================================================================
     * 4. CENÁRIOS DE LISTAGEM, CONTAGEM E PAGINAÇÃO
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Listagem e Paginação")
    class ListagemEPaginacaoCenarios {

        @Test
        @DisplayName("Deve listar todos os funcionários ordenados por nomeCompleto")
        void deveListarTodosOsFuncionariosOrdenados() {
            // ARRANGE:
            List<Funcionario> lista = List.of(
                    criarEntidade(1L, "Ana Souza", "ana@email.com", "54111111111"),
                    criarEntidade(2L, "Bruno Lima", "bruno@email.com", "54222222222")
            );

            when(funcionarioRepository.listAll(any(Sort.class))).thenReturn(lista);

            // ACT:
            List<FuncionarioResponseDTO> resultado = funcionarioService.listarFuncionarios();

            // ASSERT:
            assertThat(resultado)
                    .isNotNull()
                    .hasSize(2);
            assertThat(resultado.get(0).getNomeCompleto()).isEqualTo("Ana Souza");
            assertThat(resultado.get(1).getNomeCompleto()).isEqualTo("Bruno Lima");

            verify(funcionarioRepository).listAll(any(Sort.class));
        }

        @Test
        @DisplayName("Deve retornar a quantidade total de funcionários")
        void deveRetornarContagemTotal() {
            // ARRANGE:
            when(funcionarioRepository.count()).thenReturn(15L);

            // ACT:
            int total = funcionarioService.contar();

            // ASSERT:
            assertThat(total).isEqualTo(15);
            verify(funcionarioRepository).count();
        }

        @Test
        @DisplayName("Deve listar funcionários paginados com sucesso dentro dos limites informados")
        void deveListarPaginadoComSucesso() {
            // ARRANGE:
            List<Funcionario> pagina = List.of(
                    criarEntidade(1L, "Ana Souza", "ana@email.com", "54111111111")
            );

            when(funcionarioRepository.findAll(any(Sort.class))).thenReturn(panacheQuery);
            when(panacheQuery.range(0, 9)).thenReturn(panacheQuery);
            when(panacheQuery.list()).thenReturn(pagina);

            // ACT: first=0, pageSize=10 -> range(0, 9)
            List<FuncionarioResponseDTO> resultado = funcionarioService.listarPaginado(0, 10);

            // ASSERT:
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getNomeCompleto()).isEqualTo("Ana Souza");

            verify(panacheQuery).range(0, 9);
            verify(panacheQuery).list();
        }

        @Test
        @DisplayName("Deve retornar lista vazia imediatamente para parâmetros de paginação inválidos")
        void deveRetornarListaVaziaParaPaginacaoInvalida() {
            // ACT & ASSERT:
            // 1. first < 0
            assertThat(funcionarioService.listarPaginado(-1, 10)).isEmpty();

            // 2. pageSize == 0
            assertThat(funcionarioService.listarPaginado(0, 0)).isEmpty();

            // 3. pageSize < 0
            assertThat(funcionarioService.listarPaginado(0, -5)).isEmpty();

            // Mockito: Nenhuma consulta de banco foi realizada
            verifyNoInteractions(funcionarioRepository);
        }
    }
}
