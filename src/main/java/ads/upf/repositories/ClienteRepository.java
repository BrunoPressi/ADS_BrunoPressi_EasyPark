package ads.upf.repositories;

import ads.upf.model.entities.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    /**
     * Busca por chave única (exemplo por CPF Hash / Blind Index ou ID)
     */
    public Optional<Cliente> buscarPorCpfHash(String cpfHash) {
        return find("cpfHash", cpfHash).firstResultOptional();
    }

    /**
     * Cria a query paginada com ordenação e filtros opcionais
     */
    public PanacheQuery<Cliente> buscarPaginado(String nomeFiltro, String sortField, boolean sortAsc) {
        StringBuilder query = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            query.append(" and lower(nomeCompleto) like :nome");
            params.put("nome", "%" + nomeFiltro.trim().toLowerCase() + "%");
        }

        Sort sort = Sort.by(
                sortField != null ? sortField : "id",
                sortAsc ? Sort.Direction.Ascending : Sort.Direction.Descending
        );

        return find(query.toString(), sort, params);
    }
}
