package ads.upf.repositories;

import ads.upf.model.entities.Tarifa;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TarifaRepository implements PanacheRepository<Tarifa> {

    public PanacheQuery<Tarifa> buscarTarifaAtiva() {
        return find("where ativo");
    }

    public boolean existeTarifaAtiva() {
        return count("ativo = true") > 0;
    }

}
