package ads.upf.services;

import ads.upf.integration.asaas.AsaasService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class CobrancaService {

    @Inject
    AsaasService asaasService;

    @Inject
    ParcelaService parcelaService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void enviarCobranca() {
        Log.info("Enviando cobrança a parcelas que vencem em 7 dias...");
        List<Long> ids = parcelaService.buscarIdsParaCobrar();

        int enviadas = 0;
        int falhas = 0;
        for (Long id : ids) {
            try {
                asaasService.cobrarParcela(id); // transação própria por parcela
                enviadas++;
            } catch (Exception e) {
                falhas++;
                Log.errorf(e, "Falha ao enviar cobrança da parcela %d", id);
            }
        }

        if (!ids.isEmpty()) {
            Log.infof("Envio finalizado: %d enviadas, %d com falha.", enviadas, falhas);
        }
    }

    void onStart(@Observes StartupEvent ev) {
        Log.info("Startup da aplicação: enviando cobrança de parcelas com vencimento em 7 dias...");
        enviarCobranca();
    }

}
