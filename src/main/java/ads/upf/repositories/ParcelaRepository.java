package ads.upf.repositories;

import ads.upf.model.entities.Parcela;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ParcelaRepository implements PanacheRepository<Parcela> {

    public PanacheQuery<Parcela> buscarPorContrato(Long id) {
        return find("contrato.id = ?1 order by numeroParcela asc", id);
    }

}
