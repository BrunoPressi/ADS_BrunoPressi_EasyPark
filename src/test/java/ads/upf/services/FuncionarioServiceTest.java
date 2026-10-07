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

@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionarioService - Suíte de Testes Unitários")
public class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PanacheQuery<Funcionario> panacheQuery;

    private FuncionarioService funcionarioService;

    @BeforeEach
    void setUp() {
        funcionarioService = new FuncionarioService(funcionarioRepository);
    }

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
        f.setSenha("$2a$10$abcdefghijklmnopqrstuvwx");
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
            FuncionarioCreateDTO dto = criarDto(null, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.existsByEmail("carlos@email.com", null)).thenReturn(false);

            funcionarioService.salvarFuncionario(dto);

            ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
            verify(funcionarioRepository, times(1)).persist(captor.capture());

            Funcionario funcionarioSalvo = captor.getValue();
            assertThat(funcionarioSalvo.getNomeCompleto()).isEqualTo("Carlos Silva");
            assertThat(funcionarioSalvo.getEmail()).isEqualTo("carlos@email.com");
            assertThat(funcionarioSalvo.getTelefone()).isEqualTo("54999999999");
            assertThat(funcionarioSalvo.getSenha()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o email já estiver em uso")
        void deveLancarExcecaoQuandoEmailJaExistir() {
            FuncionarioCreateDTO dto = criarDto(null, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.existsByEmail("carlos@email.com", null)).thenReturn(true);

            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe um funcionário com esse email");

            verify(funcionarioRepository, never()).persist(any(Funcionario.class));
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o nome já estiver em uso")
        void deveLancarExcecaoQuandoNomeJaExistir() {
            FuncionarioCreateDTO dto = criarDto(null, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.existsByEmail("carlos@email.com", null)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Silva", null)).thenReturn(true);

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
            Funcionario funcionarioExistente = criarEntidade(10L, "Carlos Antigo", "antigo@email.com", "54111111111");
            FuncionarioCreateDTO dtoEdicao = criarDto(10L, "Carlos Novo", "novo@email.com", "54222222222");

            when(funcionarioRepository.existsByEmail("novo@email.com", 10L)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Novo", 10L)).thenReturn(false);
            when(funcionarioRepository.findByIdOptional(10L)).thenReturn(Optional.of(funcionarioExistente));

            funcionarioService.salvarFuncionario(dtoEdicao);

            assertThat(funcionarioExistente.getNomeCompleto()).isEqualTo("Carlos Novo");
            assertThat(funcionarioExistente.getEmail()).isEqualTo("novo@email.com");
            assertThat(funcionarioExistente.getTelefone()).isEqualTo("54222222222");

            verify(funcionarioRepository, never()).persist(any(Funcionario.class));
        }

        @Test
        @DisplayName("Deve atualizar mantendo o mesmo email e nome sem acusar duplicidade contra si mesmo")
        void deveAtualizarMantendoMesmoEmailENome() {
            Funcionario funcionarioExistente = criarEntidade(10L, "Carlos Silva", "carlos@email.com", "54111111111");
            FuncionarioCreateDTO dtoEdicao = criarDto(10L, "Carlos Silva", "carlos@email.com", "54999999999");

            when(funcionarioRepository.existsByEmail("carlos@email.com", 10L)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Silva", 10L)).thenReturn(false);
            when(funcionarioRepository.findByIdOptional(10L)).thenReturn(Optional.of(funcionarioExistente));

            funcionarioService.salvarFuncionario(dtoEdicao);

            assertThat(funcionarioExistente.getTelefone()).isEqualTo("54999999999");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar atualizar funcionário inexistente")
        void deveLancarEntityNotFoundExceptionQuandoIdNaoExistir() {
            FuncionarioCreateDTO dto = criarDto(999L, "Carlos Silva", "carlos@email.com", "54999999999");

            when(funcionarioRepository.existsByEmail("carlos@email.com", 999L)).thenReturn(false);
            when(funcionarioRepository.existsByNome("Carlos Silva", 999L)).thenReturn(false);
            when(funcionarioRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Funcionário não encontrado");
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o email pertencer a outro funcionário na atualização")
        void deveLancarExcecaoQuandoEmailColidirComOutroId() {
            FuncionarioCreateDTO dto = criarDto(10L, "Carlos Silva", "maria@email.com", "54999999999");
            when(funcionarioRepository.existsByEmail("maria@email.com", 10L)).thenReturn(true);

            assertThatThrownBy(() -> funcionarioService.salvarFuncionario(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Já existe um funcionário com esse email");

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
            Funcionario funcionario = criarEntidade(15L, "Carlos Silva", "carlos@email.com", "54999999999");
            when(funcionarioRepository.findByIdOptional(15L)).thenReturn(Optional.of(funcionario));

            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo("15");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(15L);
            assertThat(resultado.getNomeCompleto()).isEqualTo("Carlos Silva");

            verify(funcionarioRepository, times(1)).findByIdOptional(15L);
            verify(funcionarioRepository, never()).findByNome(anyString());
        }

        @Test
        @DisplayName("Deve buscar por nome quando o termo for textual")
        void deveBuscarPorNomeQuandoTermoForTexto() {
            Funcionario funcionario = criarEntidade(1L, "Carlos Silva", "carlos@email.com", "54999999999");

            when(funcionarioRepository.findByNome("Carlos")).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(funcionario));

            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo("  Carlos  ");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNomeCompleto()).isEqualTo("Carlos Silva");

            verify(funcionarioRepository, times(1)).findByNome("Carlos");
        }

        @Test
        @DisplayName("Não deve lançar NumberFormatException quando o termo numérico exceder a capacidade de Long")
        void deveProtegerContraOverflowNumericoSemQuebrar() {
            String numeroGigante = "99999999999999999999999999999";

            when(funcionarioRepository.findByNome(numeroGigante)).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());

            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo(numeroGigante);

            assertThat(resultado).isNull();
            verify(funcionarioRepository, never()).findByIdOptional(anyLong());
            verify(funcionarioRepository, times(1)).findByNome(numeroGigante);
        }

        @ParameterizedTest(name = "Entrada [{0}] deve retornar null com segurança")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", " \t \n "})
        @DisplayName("Deve retornar null sem consultar repositório para entradas vazias, nulas ou com espaços")
        void deveRetornarNullParaTermosNulosOuVazios(String entradaInvalida) {
            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo(entradaInvalida);

            assertThat(resultado).isNull();
            verifyNoInteractions(funcionarioRepository);
        }

        @Test
        @DisplayName("Deve retornar null quando não encontrar nenhum funcionário pelo termo pesquisado")
        void deveRetornarNullQuandoNaoEncontrarPorNome() {
            when(funcionarioRepository.findByNome("inexistente")).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());

            FuncionarioResponseDTO resultado = funcionarioService.buscarPorTermo("inexistente");

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
            List<Funcionario> lista = List.of(
                    criarEntidade(1L, "Ana Souza", "ana@email.com", "54111111111"),
                    criarEntidade(2L, "Bruno Lima", "bruno@email.com", "54222222222")
            );

            when(funcionarioRepository.listAll(any(Sort.class))).thenReturn(lista);

            List<FuncionarioResponseDTO> resultado = funcionarioService.listarFuncionarios();

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
            when(funcionarioRepository.count()).thenReturn(15L);

            int total = funcionarioService.contar();

            assertThat(total).isEqualTo(15);
            verify(funcionarioRepository).count();
        }

        @Test
        @DisplayName("Deve listar funcionários paginados com sucesso dentro dos limites informados")
        void deveListarPaginadoComSucesso() {
            List<Funcionario> pagina = List.of(
                    criarEntidade(1L, "Ana Souza", "ana@email.com", "54111111111")
            );

            when(funcionarioRepository.findAll(any(Sort.class))).thenReturn(panacheQuery);
            when(panacheQuery.range(0, 9)).thenReturn(panacheQuery);
            when(panacheQuery.list()).thenReturn(pagina);

            List<FuncionarioResponseDTO> resultado = funcionarioService.listarPaginado(0, 10);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getNomeCompleto()).isEqualTo("Ana Souza");

            verify(panacheQuery).range(0, 9);
            verify(panacheQuery).list();
        }

        @Test
        @DisplayName("Deve retornar lista vazia imediatamente para parâmetros de paginação inválidos")
        void deveRetornarListaVaziaParaPaginacaoInvalida() {
            assertThat(funcionarioService.listarPaginado(-1, 10)).isEmpty();
            assertThat(funcionarioService.listarPaginado(0, 0)).isEmpty();
            assertThat(funcionarioService.listarPaginado(0, -5)).isEmpty();

            verifyNoInteractions(funcionarioRepository);
        }
    }

    /*
     * =====================================================================================
     * 5. CENÁRIOS DE DEFINIR FUNCIONÁRIO (definirFuncionario)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Definir Funcionário")
    class DefinirFuncionarioCenarios {

        @Test
        @DisplayName("Deve retornar o funcionário quando encontrado pelo email")
        void deveRetornarFuncionarioQuandoEncontrado() {
            Funcionario f = criarEntidade(1L, "Carlos", "carlos@email.com", "54999999999");
            when(funcionarioRepository.buscarPorEmail("carlos@email.com")).thenReturn(Optional.of(f));

            Funcionario resultado = funcionarioService.definirFuncionario("carlos@email.com");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo("carlos@email.com");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando funcionário não for encontrado pelo email")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(funcionarioRepository.buscarPorEmail("inexistente@email.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> funcionarioService.definirFuncionario("inexistente@email.com"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Funcionário não encontrado.");
        }
    }
}
