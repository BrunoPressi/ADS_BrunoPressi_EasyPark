package ads.upf.repositories;

import ads.upf.model.entities.Veiculo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class VeiculoRepository implements PanacheRepository<Veiculo> {
    public Optional<Veiculo> buscarPelaPlaca(String placa) {
        return find("placa = ?1", placa).firstResultOptional();
    }
}
