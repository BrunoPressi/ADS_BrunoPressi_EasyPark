package ads.upf.repositories;

import ads.upf.model.entities.Veiculo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VeiculoRepository implements PanacheRepository<Veiculo> {}
