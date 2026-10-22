package ads.upf.services;

import ads.upf.exceptions.TokenInvalidoException;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.SenhaToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlterarSenhaUseCase - Testes Unitários")
class AlterarSenhaUseCaseTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AlterarSenhaUseCase useCase;

    private SenhaToken tokenCom(LocalDateTime expiracao, Funcionario funcionario) {
        SenhaToken t = new SenhaToken();
        t.setExpirationDate(expiracao);
        t.setFuncionario(funcionario);
        return t;
    }

    @Test
    @DisplayName("alterarSenha grava hash bcrypt e exclui o token")
    void alterarSenhaComSucesso() {
        UUID uuid = UUID.randomUUID();
        Funcionario funcionario = new Funcionario();
        SenhaToken token = tokenCom(LocalDateTime.now().plusHours(1), funcionario);
        when(tokenService.buscarPorToken(uuid)).thenReturn(token);

        useCase.alterarSenha("senha1234", uuid);

        assertThat(funcionario.getSenha()).isNotBlank().isNotEqualTo("senha1234");
        verify(tokenService).excluirToken(token);
    }

    @Test
    @DisplayName("alterarSenha com token expirado lança exceção e não altera a senha")
    void alterarSenhaTokenExpirado() {
        UUID uuid = UUID.randomUUID();
        Funcionario funcionario = new Funcionario();
        when(tokenService.buscarPorToken(uuid))
                .thenReturn(tokenCom(LocalDateTime.now().minusMinutes(1), funcionario));

        assertThatThrownBy(() -> useCase.alterarSenha("senha1234", uuid))
                .isInstanceOf(TokenInvalidoException.class);

        assertThat(funcionario.getSenha()).isNull();
        verify(tokenService, never()).excluirToken(any());
    }

    @Test
    @DisplayName("alterarSenha com token inexistente propaga a exceção")
    void alterarSenhaTokenInexistente() {
        UUID uuid = UUID.randomUUID();
        when(tokenService.buscarPorToken(uuid)).thenThrow(new TokenInvalidoException("inválido"));

        assertThatThrownBy(() -> useCase.alterarSenha("senha1234", uuid))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    @DisplayName("validarToken aceita token válido")
    void validarTokenValido() {
        UUID uuid = UUID.randomUUID();
        when(tokenService.buscarPorToken(uuid))
                .thenReturn(tokenCom(LocalDateTime.now().plusHours(1), new Funcionario()));

        assertThatCode(() -> useCase.validarToken(uuid)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarToken rejeita token nulo")
    void validarTokenNulo() {
        assertThatThrownBy(() -> useCase.validarToken(null))
                .isInstanceOf(TokenInvalidoException.class);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("validarToken rejeita token expirado")
    void validarTokenExpirado() {
        UUID uuid = UUID.randomUUID();
        when(tokenService.buscarPorToken(uuid))
                .thenReturn(tokenCom(LocalDateTime.now().minusSeconds(1), new Funcionario()));

        assertThatThrownBy(() -> useCase.validarToken(uuid))
                .isInstanceOf(TokenInvalidoException.class);
    }
}
