package ads.upf.repositories;

import ads.upf.model.entities.Funcionario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FuncionarioRepository implements PanacheRepository<Funcionario> {

    public boolean existsByEmail(String email, Long ignoreId) {
        if (ignoreId == null) {
            return count("email = ?1", email) > 0;
        }
        return count("email = ?1 and id != ?2", email, ignoreId) > 0;
    }

    public boolean existsByNome(String nome, Long ignoreId) {
        if (ignoreId == null) {
            return count("nomeCompleto = ?1", nome) > 0;
        }
        return count("nomeCompleto = ?1 and id != ?2", nome, ignoreId) > 0;
    }

}
