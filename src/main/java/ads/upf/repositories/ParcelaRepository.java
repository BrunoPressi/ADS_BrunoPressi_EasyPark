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

    public List<Parcela> buscarComVencimentoEm7DiasPorContrato(Long contratoId) {
        LocalDate dataVencimento = LocalDate.now().plusDays(7);
        return find("where contrato.id = ?1 and dataVencimento <= ?2 and status = ?3 and asaasPaymentId is null",
                contratoId,
                dataVencimento,
                ParcelaStatus.pendente)
                .list();
    }

    public List<Parcela> buscarAbertasParaSincronizar() {
        return find("where asaasPaymentId is not null and status in (?1, ?2)",
                ParcelaStatus.pendente,
                ParcelaStatus.atrasada)
                .list();
    }

    public List<Parcela> buscarAbertasParaSincronizarPorContrato(Long contratoId) {
        return find("where contrato.id = ?1 and asaasPaymentId is not null and status in (?2, ?3)",
                contratoId,
                ParcelaStatus.pendente,
                ParcelaStatus.atrasada)
                .list();
    }

    public List<Parcela> buscarCanceladasComCobrancaPendenteExclusao() {
        return find("where status = ?1 and asaasPaymentId is not null",
                ParcelaStatus.cancelada)
                .list();
    }
}
