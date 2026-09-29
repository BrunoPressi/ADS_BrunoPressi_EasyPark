package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.repositories.ClienteRepository;
import ads.upf.utils.SecurityUtil;
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

// AssertJ Core: Asserções fluentes e legíveis
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Mockito: Simulação (stubbing) e auditoria de chamadas
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * =========================================================================================
 * @ExtendWith(MockitoExtension.class) - JUnit 5:
 * Habilita a integração do Mockito com o ciclo de vida do JUnit 5.
 * Inicializa automaticamente os campos anotados com @Mock e gerencia o isolamento dos dublês.
 * =========================================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - Suíte de Testes Unitários")
public class ClienteServiceTest {

    /**
     * @Mock - Mockito:
     * Dublê do ClienteRepository para simular operações de banco de dados
     * sem conectar ao banco real PostgreSQL.
     */
    @Mock
    private ClienteRepository clienteRepository;

    /**
     * @Mock - Mockito:
     * Dublê do PanacheQuery para simular consultas fluentes e paginação do Hibernate Panache.
     */
    @Mock
    private PanacheQuery<Cliente> panacheQuery;

    /**
     * Objeto real sob teste (SUT - System Under Test).
     */
    private ClienteService clienteService;

    /**
     * @BeforeEach - JUnit 5:
     * Executado antes de CADA teste. Cria uma nova instância de ClienteService
     * injetando o mock do repositório, garantindo testes isolados e reproduzíveis.
     */
    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository);
    }

    // -------------------------------------------------------------------------------------
    // Métodos auxiliares (Helpers) para montagem dos cenários
    // -------------------------------------------------------------------------------------
    private ClienteCreateDTO criarDto(Long id, String nome, String email, String telefone, String cpf) {
        ClienteCreateDTO dto = new ClienteCreateDTO();
        dto.setId(id);
        dto.setNomeCompleto(nome);
        dto.setEmail(email);
        dto.setTelefone(telefone);
        dto.setCpf(cpf);
        return dto;
    }

    private Cliente criarEntidade(Long id, String nome, String email, String telefone, String cpf) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNomeCompleto(nome);
        c.setEmail(email);
        c.setTelefone(telefone);
        c.setCpf(cpf);
        return c;
    }

    /*
     * =====================================================================================
     * 1. CENÁRIOS DE CRIAÇÃO (salvarCliente com id == null)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Criação de Cliente (id == null)")
    class CriarClienteCenarios {

        @Test
        @DisplayName("Deve persistir um novo cliente com sucesso quando CPF e Email forem únicos")
        void deveSalvarNovoClienteComSucesso() {
            // ARRANGE:
            ClienteCreateDTO dto = criarDto(null, "João da Silva", "joao@email.com", "54999998888", "123.456.789-01");

            // Mockito: when(...).thenReturn(...)
            // Ensina ao mock que não existe duplicidade de CPF nem de E-mail
            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(false);
            when(clienteRepository.checkEmail("joao@email.com", null)).thenReturn(false);

            // ACT:
            clienteService.salvarCliente(dto);

            // ASSERT & VERIFY:
            // Mockito: ArgumentCaptor permite interceptar a entidade enviada ao persist
            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository, times(1)).persist(captor.capture());

            Cliente clienteSalvo = captor.getValue();

            // AssertJ: assertThat(...) validações da integridade dos atributos
            assertThat(clienteSalvo.getNomeCompleto()).isEqualTo("João da Silva");
            assertThat(clienteSalvo.getEmail()).isEqualTo("joao@email.com");
            assertThat(clienteSalvo.getTelefone()).isEqualTo("54999998888");
            // Garante que a máscara foi removida e o CPF contém apenas dígitos
            assertThat(clienteSalvo.getCpf()).isEqualTo("12345678901");
            // Garante que o Blind Index (cpfHash) foi gerado automaticamente
            assertThat(clienteSalvo.getCpfHash()).isNotBlank();
        }

        @Test
        @DisplayName("Deve sanitizar com trim, lowercase e remoção de máscara no CPF antes de persistir")
        void deveSanitizarDadosAoSalvar() {
            // ARRANGE: Dados enviados com espaços acidentais e email com letras maiúsculas
            ClienteCreateDTO dto = criarDto(null, "  Maria Souza  ", "  MARIA@EMAIL.COM  ", "  54988887777  ", " 987.654.321-99 ");

            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(false);
            when(clienteRepository.checkEmail("maria@email.com", null)).thenReturn(false);

            // ACT:
            clienteService.salvarCliente(dto);

            // ASSERT:
            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).persist(captor.capture());

            Cliente salvo = captor.getValue();
            assertThat(salvo.getNomeCompleto()).isEqualTo("Maria Souza");
            assertThat(salvo.getEmail()).isEqualTo("maria@email.com");
            assertThat(salvo.getTelefone()).isEqualTo("54988887777");
            assertThat(salvo.getCpf()).isEqualTo("98765432199");
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o CPF já estiver cadastrado")
        void deveLancarExcecaoQuandoCpfJaExistir() {
            // ARRANGE: Repositório reporta que o CPF já existe
            ClienteCreateDTO dto = criarDto(null, "João da Silva", "joao@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(true);

            // ACT & ASSERT:
            // AssertJ: assertThatThrownBy valida a captura da exceção esperada
            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse CPF já está cadastrado");

            // Mockito: verify(..., never()) garante que persist NUNCA foi chamado
            verify(clienteRepository, never()).persist(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o Email já estiver cadastrado")
        void deveLancarExcecaoQuandoEmailJaExistir() {
            // ARRANGE: CPF livre, mas e-mail duplicado
            ClienteCreateDTO dto = criarDto(null, "João da Silva", "joao@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(false);
            when(clienteRepository.checkEmail("joao@email.com", null)).thenReturn(true);

            // ACT & ASSERT:
            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse Email já está cadastrado");

            verify(clienteRepository, never()).persist(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar NullPointerException com mensagens explicativas para payload ou atributos nulos")
        void deveValidarCamposObrigatoriosNulos() {
            // 1. Payload DTO nulo
            assertThatThrownBy(() -> clienteService.salvarCliente(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Os dados do cliente não podem ser nulos");

            // 2. Nome completo nulo
            ClienteCreateDTO semNome = criarDto(null, null, "email@email.com", "54999998888", "12345678901");
            assertThatThrownBy(() -> clienteService.salvarCliente(semNome))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("O nome é obrigatório");

            // 3. Email nulo
            ClienteCreateDTO semEmail = criarDto(null, "Nome", null, "54999998888", "12345678901");
            assertThatThrownBy(() -> clienteService.salvarCliente(semEmail))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("O email é obrigatório");

            // 4. Telefone nulo
            ClienteCreateDTO semTelefone = criarDto(null, "Nome", "email@email.com", null, "12345678901");
            assertThatThrownBy(() -> clienteService.salvarCliente(semTelefone))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("O telefone é obrigatório");

            // 5. CPF nulo
            ClienteCreateDTO semCpf = criarDto(null, "Nome", "email@email.com", "54999998888", null);
            assertThatThrownBy(() -> clienteService.salvarCliente(semCpf))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("O CPF é obrigatório");

            // Mockito: Assegura que persistência nunca foi chamada
            verify(clienteRepository, never()).persist(any(Cliente.class));
        }
    }

    /*
     * =====================================================================================
     * 2. CENÁRIOS DE ATUALIZAÇÃO (salvarCliente com id != null)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Atualização de Cliente (id != null)")
    class AtualizarClienteCenarios {

        @Test
        @DisplayName("Deve atualizar os dados do cliente com sucesso para ID existente")
        void deveAtualizarClienteComSucesso() {
            // ARRANGE:
            Cliente clienteExistente = criarEntidade(10L, "João Antigo", "antigo@email.com", "54111111111", "12345678901");
            ClienteCreateDTO dtoEdicao = criarDto(10L, "João Novo", "novo@email.com", "54222222222", "98765432100");

            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(false);
            when(clienteRepository.checkEmail("novo@email.com", 10L)).thenReturn(false);
            when(clienteRepository.findByIdOptional(10L)).thenReturn(Optional.of(clienteExistente));

            // ACT:
            clienteService.salvarCliente(dtoEdicao);

            // ASSERT:
            // A entidade gerenciada pelo Hibernate reflete as alterações
            assertThat(clienteExistente.getNomeCompleto()).isEqualTo("João Novo");
            assertThat(clienteExistente.getEmail()).isEqualTo("novo@email.com");
            assertThat(clienteExistente.getTelefone()).isEqualTo("54222222222");
            assertThat(clienteExistente.getCpf()).isEqualTo("98765432100");

            // Em edições, persist não deve ser invocado novamente
            verify(clienteRepository, never()).persist(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve atualizar mantendo o mesmo CPF e Email sem acusar duplicidade contra si mesmo")
        void deveAtualizarMantendoMesmoCpfEEmail() {
            // ARRANGE:
            Cliente clienteExistente = criarEntidade(10L, "João Silva", "joao@email.com", "54111111111", "12345678901");
            // Atualiza apenas o telefone mantendo os dados identificadores
            ClienteCreateDTO dtoEdicao = criarDto(10L, "João Silva", "joao@email.com", "54999998888", "12345678901");

            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(false);
            when(clienteRepository.checkEmail("joao@email.com", 10L)).thenReturn(false);
            when(clienteRepository.findByIdOptional(10L)).thenReturn(Optional.of(clienteExistente));

            // ACT:
            clienteService.salvarCliente(dtoEdicao);

            // ASSERT:
            assertThat(clienteExistente.getTelefone()).isEqualTo("54999998888");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar atualizar cliente com ID inexistente")
        void deveLancarEntityNotFoundExceptionQuandoIdNaoExistir() {
            // ARRANGE:
            ClienteCreateDTO dto = criarDto(999L, "João Silva", "joao@email.com", "54999998888", "12345678901");

            when(clienteRepository.checkCpf(anyString(), eq(999L))).thenReturn(false);
            when(clienteRepository.checkEmail("joao@email.com", 999L)).thenReturn(false);
            when(clienteRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

            // ACT & ASSERT:
            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o CPF já pertencer a outro cliente na edição")
        void deveLancarExcecaoQuandoCpfColidirComOutroId() {
            // ARRANGE:
            ClienteCreateDTO dto = criarDto(10L, "João Silva", "joao@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(true);

            // ACT & ASSERT:
            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse CPF já está cadastrado");

            verify(clienteRepository, never()).findByIdOptional(anyLong());
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o Email já pertencer a outro cliente na edição")
        void deveLancarExcecaoQuandoEmailColidirComOutroId() {
            // ARRANGE:
            ClienteCreateDTO dto = criarDto(10L, "João Silva", "maria@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(false);
            when(clienteRepository.checkEmail("maria@email.com", 10L)).thenReturn(true);

            // ACT & ASSERT:
            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse Email já está cadastrado");

            verify(clienteRepository, never()).findByIdOptional(anyLong());
        }
    }

    /*
     * =====================================================================================
     * 3. CENÁRIOS DE BUSCA POR CHAVE ÚNICA (buscarPorChaveUnica)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Busca por Chave Única (CPF, Email ou ID)")
    class BuscarPorChaveUnicaCenarios {

        @Test
        @DisplayName("Deve buscar por Email quando o termo contiver o caractere '@'")
        void deveBuscarPorEmailQuandoTermoContiverArroba() {
            // ARRANGE:
            Cliente cliente = criarEntidade(1L, "João Silva", "joao@email.com", "54999998888", "12345678901");

            when(clienteRepository.find(eq("email = ?1"), eq("joao@email.com"))).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(cliente));

            // ACT:
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("  JOAO@EMAIL.COM  ");

            // ASSERT:
            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
            assertThat(resultado.getNomeCompleto()).isEqualTo("João Silva");

            verify(clienteRepository, times(1)).find(eq("email = ?1"), eq("joao@email.com"));
        }

        @Test
        @DisplayName("Deve buscar por CPF via Blind Index quando o termo contiver 11 dígitos numéricos")
        void deveBuscarPorCpfComOnzeDigitosFormatadoOuNao() {
            // ARRANGE:
            Cliente cliente = criarEntidade(2L, "Maria Souza", "maria@email.com", "54988887777", "12345678901");

            // O blind index do CPF 12345678901
            String cpfHashEsperado = SecurityUtil.generateBlindIndex("12345678901");

            when(clienteRepository.find(eq("cpfHash = ?1"), eq(cpfHashEsperado))).thenReturn(panacheQuery);
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(cliente));

            // ACT: Passamos CPF com máscara para testar remoção de formatação
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("123.456.789-01");

            // ASSERT:
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(2L);
            assertThat(resultado.getNomeCompleto()).isEqualTo("Maria Souza");

            verify(clienteRepository, times(1)).find(eq("cpfHash = ?1"), eq(cpfHashEsperado));
        }

        @Test
        @DisplayName("Deve buscar por ID numérico quando o termo for puramente numérico (menor que 11 dígitos)")
        void deveBuscarPorIdQuandoTermoForNumerico() {
            // ARRANGE:
            Cliente cliente = criarEntidade(42L, "Cliente 42", "cliente42@email.com", "54999998888", "12345678901");
            when(clienteRepository.findByIdOptional(42L)).thenReturn(Optional.of(cliente));

            // ACT:
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("42");

            // ASSERT:
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(42L);

            verify(clienteRepository, times(1)).findByIdOptional(42L);
        }

        @Test
        @DisplayName("Não deve lançar NumberFormatException para números com mais de 18 dígitos (proteção contra overflow)")
        void deveProtegerContraOverflowNumericoSemQuebrar() {
            // ARRANGE: Termo numérico gigante (28 dígitos)
            String numeroGigante = "9999999999999999999999999999";

            // ACT:
            // A regex ^\\d{1,18}$ evita Long.valueOf para entradas além de 18 dígitos, retornando null com segurança
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica(numeroGigante);

            // ASSERT:
            assertThat(resultado).isNull();
            verify(clienteRepository, never()).findByIdOptional(anyLong());
        }

        /**
         * @ParameterizedTest - JUnit 5:
         * Executa o teste múltiplas vezes para cobrir casos de borda sem duplicar código.
         */
        @ParameterizedTest(name = "Entrada [{0}] deve retornar null com segurança")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", " \t \n "})
        @DisplayName("Deve retornar null sem consultar o repositório para termos nulos ou em branco")
        void deveRetornarNullParaTermosNulosOuVazios(String entradaInvalida) {
            // ACT:
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica(entradaInvalida);

            // ASSERT:
            assertThat(resultado).isNull();

            // Mockito: Assegura que nenhuma consulta ao banco foi tentada
            verifyNoInteractions(clienteRepository);
        }

        @Test
        @DisplayName("Deve retornar null para texto arbitrário sem arroba e sem dígitos suficientes")
        void deveRetornarNullParaTextoArbitrarioSemDigitos() {
            // ACT:
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("João da Silva");

            // ASSERT:
            assertThat(resultado).isNull();
            verifyNoInteractions(clienteRepository);
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
        @DisplayName("Deve listar clientes paginados com sucesso dentro dos limites informados")
        void deveListarPaginadoComSucesso() {
            // ARRANGE:
            List<Cliente> pagina = List.of(
                    criarEntidade(1L, "Ana Souza", "ana@email.com", "54111111111", "12345678901")
            );

            when(clienteRepository.findAll(any(Sort.class))).thenReturn(panacheQuery);
            when(panacheQuery.range(0, 9)).thenReturn(panacheQuery);
            when(panacheQuery.list()).thenReturn(pagina);

            // ACT: first=0, pageSize=10 -> range(0, 9)
            List<ClienteResponseDTO> resultado = clienteService.listarPaginado(0, 10);

            // ASSERT:
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getNomeCompleto()).isEqualTo("Ana Souza");

            verify(panacheQuery).range(0, 9);
            verify(panacheQuery).list();
        }

        @Test
        @DisplayName("Deve retornar a quantidade total de clientes registrados")
        void deveRetornarContagemTotal() {
            // ARRANGE:
            when(clienteRepository.count()).thenReturn(100L);

            // ACT:
            int total = clienteService.contar();

            // ASSERT:
            assertThat(total).isEqualTo(100);
            verify(clienteRepository).count();
        }

        @Test
        @DisplayName("Deve retornar lista vazia imediatamente para parâmetros de paginação inválidos")
        void deveRetornarListaVaziaParaPaginacaoInvalida() {
            // ACT & ASSERT:
            // 1. first < 0
            assertThat(clienteService.listarPaginado(-1, 10)).isEmpty();

            // 2. pageSize == 0
            assertThat(clienteService.listarPaginado(0, 0)).isEmpty();

            // 3. pageSize < 0
            assertThat(clienteService.listarPaginado(0, -5)).isEmpty();

            // Mockito: Assegura que nenhuma consulta de paginação foi realizada no banco
            verifyNoInteractions(clienteRepository);
        }
    }
}
