package ads.upf.repositories;

import ads.upf.model.entities.Contrato;
import ads.upf.model.enums.ContratoStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ContratoRepository implements PanacheRepository<Contrato> {

    public Boolean verificarPossuiContratoAtivo(String placa) {
        return count("veiculo.placa = ?1 and status = ?2", placa, ContratoStatus.ativo) > 0;
    }

    public Optional<Contrato> buscarPeloCpf(String cpfHash) {
        return find ("cliente.cpfHash = ?1", cpfHash).firstResultOptional();
    }

    public List<Contrato> buscarTodosPaginado(int first, int pageSize) {
        return findAll(Sort.by("id"))
                .range(first, first + pageSize - 1)
                .list();
    }
}
