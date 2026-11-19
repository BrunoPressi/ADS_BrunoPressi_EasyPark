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

    /**
     * Retorna a permanência mais recente da placa, em qualquer status
     * (em andamento ou concluída). Usada na busca por placa da listagem.
     */
    public Optional<Permanencia> buscarMaisRecentePorPlaca(String placa) {
        return find(
                "select p from Permanencia p join fetch p.veiculo join fetch p.vaga left join fetch p.pagamento " +
                "where upper(p.veiculo.placa) = ?1 order by p.dataEntrada desc",
                placa.trim().toUpperCase())
                .firstResultOptional();
    }

    public List<Permanencia> listarPaginado(int first, int pageSize) {
        return find(
                "select p from Permanencia p join fetch p.veiculo join fetch p.vaga",
                Sort.descending("p.status", "dataEntrada"))
                .range(first, first + pageSize - 1)
                .list();
    }
}
