package ads.upf.services;

import ads.upf.exceptions.TokenInvalidoException;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.SenhaToken;
import ads.upf.repositories.TokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class TokenService {

    @Inject
    private TokenRepository tokenRepository;

    @Transactional
    public SenhaToken criarToken(Funcionario funcionario) {
        tokenRepository.excluirPorFuncionario(funcionario);

        SenhaToken token = new SenhaToken();

        token.setToken(UUID.randomUUID());
        token.setExpirationDate(LocalDateTime.now().plusHours(2));
        token.setFuncionario(funcionario);

        tokenRepository.persist(token);

        return token;
    }

    public SenhaToken buscarPorToken(UUID token) {
        SenhaToken senhaToken = tokenRepository.buscarPorToken(token)
                .orElseThrow(
                        () -> new TokenInvalidoException("Token Inválido")
                );
        return senhaToken;
    }

    public void excluirToken(SenhaToken senhaToken) {
        tokenRepository.delete(senhaToken);
    }
}
