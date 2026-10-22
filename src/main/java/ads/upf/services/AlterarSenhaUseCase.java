package ads.upf.services;

import ads.upf.exceptions.TokenInvalidoException;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.SenhaToken;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class AlterarSenhaUseCase {

    @Inject
    protected TokenService tokenService;

    @Inject
    protected EmailService emailService;

    @Inject
    protected FuncionarioService funcionarioService;

    @Transactional
    public void validarToken(UUID token) {
        if (token == null) {
            throw new TokenInvalidoException("O token não foi informado.");
        }
        verificarExpiracao(tokenService.buscarPorToken(token));
    }

    @Transactional
    public void alterarSenha(String senha, UUID token) {
        SenhaToken senhaToken = tokenService.buscarPorToken(token);
        verificarExpiracao(senhaToken);

        String senhaHash = BcryptUtil.bcryptHash(senha);
        senhaToken.getFuncionario().setSenha(senhaHash);

        tokenService.excluirToken(senhaToken);
    }

    private void verificarExpiracao(SenhaToken senhaToken) {
        if (senhaToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("Token expirado!");
        }
    }

    public void enviarEmailRedefinicaoSenha(String email) {
        Funcionario funcionario = funcionarioService.buscarPorEmail(email);
        SenhaToken senhaToken = tokenService.criarToken(funcionario);

        emailService.enviarEmailSenha(senhaToken.getToken(), funcionario.getEmail());
    }

}
