package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.repositories.ClienteRepository;
import ads.upf.repositories.UsuarioRepository;
import ads.upf.utils.SecurityUtil;
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
@DisplayName("ClienteService - Suíte de Testes Unitários")
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository, usuarioRepository);
    }

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
            ClienteCreateDTO dto = criarDto(null, "João da Silva", "joao@email.com", "54999998888", "123.456.789-01");

            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(false);
            when(usuarioRepository.checkEmail("joao@email.com", null)).thenReturn(false);

            clienteService.salvarCliente(dto);

            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository, times(1)).persist(captor.capture());

            Cliente clienteSalvo = captor.getValue();
            assertThat(clienteSalvo.getNomeCompleto()).isEqualTo("João da Silva");
            assertThat(clienteSalvo.getEmail()).isEqualTo("joao@email.com");
            assertThat(clienteSalvo.getTelefone()).isEqualTo("54999998888");
            assertThat(clienteSalvo.getCpf()).isEqualTo("12345678901");
            assertThat(clienteSalvo.getCpfHash()).isNotBlank();
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o CPF já estiver cadastrado")
        void deveLancarExcecaoQuandoCpfJaExistir() {
            ClienteCreateDTO dto = criarDto(null, "João da Silva", "joao@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(true);

            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse CPF já está cadastrado");

            verify(clienteRepository, never()).persist(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o Email já estiver cadastrado")
        void deveLancarExcecaoQuandoEmailJaExistir() {
            ClienteCreateDTO dto = criarDto(null, "João da Silva", "joao@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), isNull())).thenReturn(false);
            when(usuarioRepository.checkEmail("joao@email.com", null)).thenReturn(true);

            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse Email já está cadastrado");

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
            Cliente clienteExistente = criarEntidade(10L, "João Antigo", "antigo@email.com", "54111111111", "12345678901");
            ClienteCreateDTO dtoEdicao = criarDto(10L, "João Novo", "novo@email.com", "54222222222", "98765432100");

            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(false);
            when(usuarioRepository.checkEmail("novo@email.com", 10L)).thenReturn(false);
            when(clienteRepository.findByIdOptional(10L)).thenReturn(Optional.of(clienteExistente));

            clienteService.salvarCliente(dtoEdicao);

            assertThat(clienteExistente.getNomeCompleto()).isEqualTo("João Novo");
            assertThat(clienteExistente.getEmail()).isEqualTo("novo@email.com");
            assertThat(clienteExistente.getTelefone()).isEqualTo("54222222222");
            assertThat(clienteExistente.getCpf()).isEqualTo("98765432100");

            verify(clienteRepository, never()).persist(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve atualizar mantendo o mesmo CPF e Email sem acusar duplicidade contra si mesmo")
        void deveAtualizarMantendoMesmoCpfEEmail() {
            Cliente clienteExistente = criarEntidade(10L, "João Silva", "joao@email.com", "54111111111", "12345678901");
            ClienteCreateDTO dtoEdicao = criarDto(10L, "João Silva", "joao@email.com", "54999998888", "12345678901");

            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(false);
            when(usuarioRepository.checkEmail("joao@email.com", 10L)).thenReturn(false);
            when(clienteRepository.findByIdOptional(10L)).thenReturn(Optional.of(clienteExistente));

            clienteService.salvarCliente(dtoEdicao);

            assertThat(clienteExistente.getTelefone()).isEqualTo("54999998888");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException ao tentar atualizar cliente com ID inexistente")
        void deveLancarEntityNotFoundExceptionQuandoIdNaoExistir() {
            ClienteCreateDTO dto = criarDto(999L, "João Silva", "joao@email.com", "54999998888", "12345678901");

            when(clienteRepository.checkCpf(anyString(), eq(999L))).thenReturn(false);
            when(usuarioRepository.checkEmail("joao@email.com", 999L)).thenReturn(false);
            when(clienteRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o CPF já pertencer a outro cliente na edição")
        void deveLancarExcecaoQuandoCpfColidirComOutroId() {
            ClienteCreateDTO dto = criarDto(10L, "João Silva", "joao@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(true);

            assertThatThrownBy(() -> clienteService.salvarCliente(dto))
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessageContaining("Esse CPF já está cadastrado");

            verify(clienteRepository, never()).findByIdOptional(anyLong());
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o Email já pertencer a outro cliente na edição")
        void deveLancarExcecaoQuandoEmailColidirComOutroId() {
            ClienteCreateDTO dto = criarDto(10L, "João Silva", "maria@email.com", "54999998888", "12345678901");
            when(clienteRepository.checkCpf(anyString(), eq(10L))).thenReturn(false);
            when(usuarioRepository.checkEmail("maria@email.com", 10L)).thenReturn(true);

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
            Cliente cliente = criarEntidade(1L, "João Silva", "joao@email.com", "54999998888", "12345678901");

            when(clienteRepository.buscarPorEmail("joao@email.com")).thenReturn(Optional.of(cliente));

            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("  JOAO@EMAIL.COM  ");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
            assertThat(resultado.getNomeCompleto()).isEqualTo("João Silva");

            verify(clienteRepository, times(1)).buscarPorEmail("joao@email.com");
        }

        @Test
        @DisplayName("Deve buscar por CPF via Blind Index quando o termo contiver 11 dígitos numéricos")
        void deveBuscarPorCpfComOnzeDigitosFormatadoOuNao() {
            Cliente cliente = criarEntidade(2L, "Maria Souza", "maria@email.com", "54988887777", "12345678901");
            String cpfHashEsperado = SecurityUtil.generateBlindIndex("12345678901");

            when(clienteRepository.buscarPorCpf(cpfHashEsperado)).thenReturn(Optional.of(cliente));

            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("123.456.789-01");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(2L);
            assertThat(resultado.getNomeCompleto()).isEqualTo("Maria Souza");

            verify(clienteRepository, times(1)).buscarPorCpf(cpfHashEsperado);
        }

        @Test
        @DisplayName("Deve buscar por ID numérico quando o termo for puramente numérico (menor que 11 dígitos)")
        void deveBuscarPorIdQuandoTermoForNumerico() {
            Cliente cliente = criarEntidade(42L, "Cliente 42", "cliente42@email.com", "54999998888", "12345678901");
            when(clienteRepository.findByIdOptional(42L)).thenReturn(Optional.of(cliente));

            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("42");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(42L);

            verify(clienteRepository, times(1)).findByIdOptional(42L);
        }

        @Test
        @DisplayName("Não deve lançar NumberFormatException para números com mais de 18 dígitos (proteção contra overflow)")
        void deveProtegerContraOverflowNumericoSemQuebrar() {
            String numeroGigante = "9999999999999999999999999999";

            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica(numeroGigante);

            assertThat(resultado).isNull();
            verify(clienteRepository, never()).findByIdOptional(anyLong());
        }

        @ParameterizedTest(name = "Entrada [{0}] deve retornar null com segurança")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", " \t \n "})
        @DisplayName("Deve retornar null sem consultar o repositório para termos nulos ou em branco")
        void deveRetornarNullParaTermosNulosOuVazios(String entradaInvalida) {
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica(entradaInvalida);

            assertThat(resultado).isNull();
            verifyNoInteractions(clienteRepository);
        }

        @Test
        @DisplayName("Deve retornar null para texto arbitrário sem arroba e sem dígitos suficientes")
        void deveRetornarNullParaTextoArbitrarioSemDigitos() {
            ClienteResponseDTO resultado = clienteService.buscarPorChaveUnica("João da Silva");

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
            List<Cliente> pagina = List.of(
                    criarEntidade(1L, "Ana Souza", "ana@email.com", "54111111111", "12345678901")
            );

            when(clienteRepository.listarTodosPaginado(0, 9)).thenReturn(pagina);

            List<ClienteResponseDTO> resultado = clienteService.listarPaginado(0, 9);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getNomeCompleto()).isEqualTo("Ana Souza");

            verify(clienteRepository).listarTodosPaginado(0, 9);
        }

        @Test
        @DisplayName("Deve retornar a quantidade total de clientes registrados")
        void deveRetornarContagemTotal() {
            when(clienteRepository.count()).thenReturn(100L);

            int total = clienteService.contar();

            assertThat(total).isEqualTo(100);
            verify(clienteRepository).count();
        }

        @Test
        @DisplayName("Deve retornar lista vazia imediatamente para parâmetros de paginação inválidos")
        void deveRetornarListaVaziaParaPaginacaoInvalida() {
            assertThat(clienteService.listarPaginado(-1, 10)).isEmpty();
            assertThat(clienteService.listarPaginado(0, 0)).isEmpty();
            assertThat(clienteService.listarPaginado(0, -5)).isEmpty();

            verifyNoInteractions(clienteRepository);
        }
    }

    /*
     * =====================================================================================
     * 5. CENÁRIOS DE VERIFICAÇÃO DE CLIENTE EXISTENTE (verificarClienteExiste)
     * =====================================================================================
     */
    @Nested
    @DisplayName("Cenários de Verificação de Existência")
    class VerificarClienteExisteCenarios {

        @Test
        @DisplayName("Deve retornar o cliente quando existir pelo CPF")
        void deveRetornarClienteQuandoExistir() {
            Cliente cliente = criarEntidade(1L, "Ana", "ana@email.com", "54111111111", "12345678901");
            String cpfHash = SecurityUtil.generateBlindIndex("12345678901");
            when(clienteRepository.buscarPorCpf(cpfHash)).thenReturn(Optional.of(cliente));

            Cliente resultado = clienteService.verificarClienteExiste("12345678901");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNomeCompleto()).isEqualTo("Ana");
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando cliente não for encontrado pelo CPF")
        void deveLancarExcecaoQuandoClienteNaoExistir() {
            String cpfHash = SecurityUtil.generateBlindIndex("12345678901");
            when(clienteRepository.buscarPorCpf(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.verificarClienteExiste(cpfHash))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }
    }
}
