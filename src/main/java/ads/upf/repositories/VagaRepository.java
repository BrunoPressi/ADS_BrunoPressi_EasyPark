package ads.upf.repositories;

import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VagaRepository implements PanacheRepository<Vaga> {

    public boolean existsByNome(String nome, Long ignoreId) {
        if (ignoreId == null) {
            return count("nome = ?1", nome) > 0;
        }
        return count("nome = ?1 and id != ?2", nome, ignoreId) > 0;
    }

    public Optional<Vaga> findByNome(String nome) {
        return find("nome = ?1", nome).firstResultOptional();
    }

    public Optional<Vaga> findByStatusAndTipo(VagaTipo vagaTipo) {
        return find("status = ?1 and tipoVaga = ?2", VagaStatus.disponivel, vagaTipo).firstResultOptional();

    }

    public List<Vaga> listarPaginado(int first, int pageSize) {
        return findAll(Sort.by("nome"))
                .range(first, first + pageSize - 1)
                .list();
    }

}
