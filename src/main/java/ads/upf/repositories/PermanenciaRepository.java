package ads.upf.repositories;

import ads.upf.model.entities.Permanencia;
import ads.upf.model.enums.PermanenciaStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PermanenciaRepository implements PanacheRepository<Permanencia> {
    public Optional<Permanencia> verificarVeiculoEstacionado(String placa) {
        return find("veiculo.placa = ?1 and status = ?2", placa, PermanenciaStatus.em_andamento)
                .firstResultOptional();
    }

    public List<Permanencia> listarPaginado(int first, int pageSize) {
        return find(
                "select p from Permanencia p join fetch p.veiculo join fetch p.vaga",
                Sort.descending("dataEntrada"))
                .range(first, first + pageSize - 1)
                .list();
    }
}
