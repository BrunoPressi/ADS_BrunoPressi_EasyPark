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

    public boolean checkCpf(String cpfHash, Long id) {
        if (id == null) {
            return count("cpfHash = ?1", cpfHash) > 0;
        }
        return count("cpfHash = ?1 and id != ?2", cpfHash, id) > 0;
    }

    public boolean checkEmail(String email, Long id) {
        if (id == null) {
            return count("email = ?1", email) > 0;
        }
        return count("email = ?1 and id != ?2", email, id) > 0;
    }

}
