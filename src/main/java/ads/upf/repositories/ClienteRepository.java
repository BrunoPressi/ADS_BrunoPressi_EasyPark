package ads.upf.repositories;

import ads.upf.model.entities.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public boolean checkCpf(String cpfHash, Long id) {
        if (id == null) {
            return count("cpfHash = ?1", cpfHash) > 0;
        }
        return count("cpfHash = ?1 and id != ?2", cpfHash, id) > 0;
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        return find("email = ?1", email).firstResultOptional();
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        return find("cpfHash = ?1", cpf).firstResultOptional();
    }

    public List<Cliente> listarTodosPaginado(int first, int pageSize) {
        return findAll(Sort.by("nomeCompleto"))
                .range(first, first + pageSize - 1)
                .list();
    }
}
