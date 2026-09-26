package ads.upf.repositories;

import ads.upf.model.entities.Contrato;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContratoRepository implements PanacheRepository<Contrato> {

    public PanacheQuery<Contrato> buscarPorCliente(String cpf) {
        return find("cliente.cpfHash = ?1", cpf);
    }

}
