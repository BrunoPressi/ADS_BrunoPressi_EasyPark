package ads.upf.repositories;

import ads.upf.model.entities.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public boolean checkCpf(String cpfHash, Long id) {
        if (id == null) {
            return count("cpfHash = ?1", cpfHash) > 0;
        }
        return count("cpfHash = ?1 and id != ?2", cpfHash, id) > 0;
    }
}
