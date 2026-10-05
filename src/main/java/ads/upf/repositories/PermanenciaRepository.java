package ads.upf.repositories;

import ads.upf.model.entities.Permanencia;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermanenciaRepository implements PanacheRepository<Permanencia> {}
