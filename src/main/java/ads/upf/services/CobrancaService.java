package ads.upf.services;

import ads.upf.integration.asaas.AsaasService;
import ads.upf.model.entities.Parcela;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CobrancaService {

    @Inject
    AsaasService asaasService;

    @Inject
    ParcelaService parcelaService;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void enviarCobranca() {
        Log.info("Enviando cobrança a parcelas que vencem em 7 dias...");
        List<Parcela> parcelaList = parcelaService.buscarComVencimentoEm7Dias();

        if (!parcelaList.isEmpty()) {
            parcelaList.forEach(
                    (p) -> asaasService.cobrarParcela(p)
            );
            Log.info("Envio concluído com sucesso! " + parcelaList.size() + " cobranças enviadas...");
        }
    }

    void onStart(@Observes StartupEvent ev) {
        Log.info("Startup da aplicação: enviando cobrança de parcelas com vencimento em 7 dias...");
        enviarCobranca();
    }

}
