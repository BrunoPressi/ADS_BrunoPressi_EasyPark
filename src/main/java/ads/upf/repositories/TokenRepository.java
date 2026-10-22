package ads.upf.repositories;

import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.SenhaToken;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TokenRepository implements PanacheRepository<SenhaToken> {
    public long excluirPorFuncionario(Funcionario funcionario) {
        return delete("funcionario", funcionario);
    }

    public Optional<SenhaToken> buscarPorToken(UUID token) {
        return find("token = ?1", token).firstResultOptional();
    }
}
