package ads.upf.repositories;

import ads.upf.model.entities.Contrato;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContratoRepository implements PanacheRepository<Contrato> {}
