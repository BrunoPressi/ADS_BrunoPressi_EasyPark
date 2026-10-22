package ads.upf.services;

import ads.upf.exceptions.TokenInvalidoException;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.SenhaToken;
import ads.upf.repositories.TokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService - Testes Unitários")
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    @DisplayName("criarToken apaga tokens antigos do funcionário antes de persistir o novo")
    void criarTokenApagaTokensAntigos() {
        Funcionario funcionario = new Funcionario();

        SenhaToken criado = tokenService.criarToken(funcionario);

        InOrder ordem = inOrder(tokenRepository);
        ordem.verify(tokenRepository).excluirPorFuncionario(funcionario);
        ordem.verify(tokenRepository).persist(any(SenhaToken.class));
        assertThat(criado.getFuncionario()).isSameAs(funcionario);
    }

    @Test
    @DisplayName("criarToken gera UUID e expiração de 24h")
    void criarTokenGeraUuidEExpiracao() {
        ArgumentCaptor<SenhaToken> captor = ArgumentCaptor.forClass(SenhaToken.class);

        tokenService.criarToken(new Funcionario());

        verify(tokenRepository).persist(captor.capture());
        SenhaToken salvo = captor.getValue();
        assertThat(salvo.getToken()).isNotNull();
        assertThat(salvo.getExpirationDate())
                .isAfter(LocalDateTime.now().plusHours(23))
                .isBefore(LocalDateTime.now().plusHours(25));
    }

    @Test
    @DisplayName("buscarPorToken retorna o token existente")
    void buscarPorTokenExistente() {
        UUID uuid = UUID.randomUUID();
        SenhaToken senhaToken = new SenhaToken();
        when(tokenRepository.buscarPorToken(uuid)).thenReturn(Optional.of(senhaToken));

        assertThat(tokenService.buscarPorToken(uuid)).isSameAs(senhaToken);
    }

    @Test
    @DisplayName("buscarPorToken lança TokenInvalidoException quando não existe")
    void buscarPorTokenInexistente() {
        UUID uuid = UUID.randomUUID();
        when(tokenRepository.buscarPorToken(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.buscarPorToken(uuid))
                .isInstanceOf(TokenInvalidoException.class);
    }
}
