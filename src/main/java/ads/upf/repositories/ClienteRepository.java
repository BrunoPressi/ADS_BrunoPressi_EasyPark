package ads.upf.repositories;

import ads.upf.model.entities.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public PanacheQuery<Cliente> findByCpf(String cpfHash) {
        return find("cpfHash = ?1", cpfHash);
    }

}
