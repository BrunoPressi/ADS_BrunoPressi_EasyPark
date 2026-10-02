package ads.upf.repositories;

import ads.upf.model.entities.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public boolean checkEmail(String email, Long id) {
        if (id == null) {
            return count("email = ?1", email) > 0;
        }
        return count("email = ?1 and id != ?2", email, id) > 0;
    }

    public boolean checkNomeCompleto(String nomeCompleto, Long id) {
        if (id == null) {
            return count("nomeCompleto = ?1", nomeCompleto) > 0;
        }
        return count("nomeCompleto = ?1 and id != ?2", nomeCompleto, id) > 0;
    }

}
