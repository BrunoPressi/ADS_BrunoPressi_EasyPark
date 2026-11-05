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
    public void executarRotinaDiaria() {
        enviarCobranca();
        sincronizarPagamentosPendentes();
        removerCobrancasCanceladas();
    }

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

    public void sincronizarPagamentosPendentes() {
        Log.info("Sincronizando status de parcelas em aberto junto ao Asaas...");
        List<Long> ids = parcelaService.buscarIdsAbertasParaSincronizar();

        int atualizadas = 0;
        int falhas = 0;
        for (Long id : ids) {
            try {
                if (asaasService.sincronizarParcela(id)) {
                    atualizadas++;
                }
            } catch (Exception e) {
                falhas++;
                Log.errorf(e, "Falha ao sincronizar status da parcela %d no Asaas", id);
            }
        }

        if (!ids.isEmpty()) {
            Log.infof("Sincronização finalizada: %d atualizadas de %d verificadas (%d com falha).",
                    atualizadas, ids.size(), falhas);
        }
    }

    public void removerCobrancasCanceladas() {
        List<Long> ids = parcelaService.buscarIdsCanceladasParaExcluirNoAsaas();
        if (ids.isEmpty()) return;

        Log.infof("Removendo no Asaas %d cobrança(s) de parcelas canceladas pendentes de exclusão...", ids.size());
        int removidas = 0;
        int falhas = 0;
        for (Long id : ids) {
            try {
                asaasService.removerCobrancaCancelada(id);
                removidas++;
            } catch (Exception e) {
                falhas++;
                Log.errorf(e, "Falha ao remover no Asaas cobrança da parcela cancelada %d", id);
            }
        }

        Log.infof("Remoção de cobranças canceladas finalizada: %d removidas, %d com falha.", removidas, falhas);
    }

    void onStart(@Observes StartupEvent ev) {
        Log.info("Startup da aplicação: executando rotina de cobrança e reconciliação com o Asaas...");
        executarRotinaDiaria();
    }

}
