package ads.upf.repositories;

import ads.upf.model.entities.Parcela;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ParcelaRepository implements PanacheRepository<Parcela> {}
