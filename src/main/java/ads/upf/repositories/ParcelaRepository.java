package ads.upf.repositories;

import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ParcelaStatus;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ParcelaRepository implements PanacheRepository<Parcela> {

    public PanacheQuery<Parcela> findByAsaasPaymentId(String asaasPaymentId) {
        return find("asaasPaymentId = ?1", asaasPaymentId);
    }

    public List<Parcela> buscarComVencimentoEm7Dias() {
        LocalDate dataVencimento = LocalDate.now().plusDays(7);
        return find("where dataVencimento <= ?1 and status = ?2 and asaasPaymentId is null",
                dataVencimento,
                ParcelaStatus.pendente)
                .list();
    }
}
