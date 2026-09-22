package ads.upf.repositories;

import ads.upf.model.entities.Vaga;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VagaRepository implements PanacheRepository<Vaga> {

    public boolean existsByNome(String nome, Long ignoreId) {
        if (ignoreId == null) {
            return count("nome = ?1", nome) > 0;
        }
        return count("nome = ?1 and id != ?2", nome, ignoreId) > 0;
    }

}
