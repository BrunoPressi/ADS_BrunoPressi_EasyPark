package ads.upf.services;

import ads.upf.model.DTOs.dashboard.*;
import ads.upf.repositories.DashboardRepository;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheManager;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DashboardService {

    @Inject
    private DashboardRepository dashboardRepository;

    @Inject
    private CacheManager cacheManager;

    public void limparTodosCaches() {
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).ifPresent(c -> c.invalidateAll().await().indefinitely());
        }
    }

    @CacheResult(cacheName = "kpi-ocupacao")
    public OcupacaoKpiDTO obterKpiOcupacao() {
        return dashboardRepository.buscarKpiOcupacao();
    }

    @CacheResult(cacheName = "kpi-veiculos-atuais")
    public VeiculosAtuaisKpiDTO obterKpiVeiculosAtuais() {
        return dashboardRepository.buscarKpiVeiculosAtuais();
    }

    @CacheResult(cacheName = "kpi-tempo-medio")
    public TempoMedioKpiDTO obterKpiTempoMedio() {
        return dashboardRepository.buscarKpiTempoMedio();
    }

    @CacheResult(cacheName = "kpi-giro-vagas")
    public GiroVagasKpiDTO obterKpiGiroVagas() {
        return dashboardRepository.buscarKpiGiroVagas();
    }

    @CacheResult(cacheName = "kpi-receita-hoje")
    public ReceitaHojeKpiDTO obterKpiReceitaHoje() {
        return dashboardRepository.buscarKpiReceitaHoje();
    }

    @CacheResult(cacheName = "kpi-receita-mes")
    public ReceitaMesKpiDTO obterKpiReceitaMes() {
        return dashboardRepository.buscarKpiReceitaMes();
    }

    @CacheResult(cacheName = "kpi-contratos")
    public ContratosKpiDTO obterKpiContratos() {
        return dashboardRepository.buscarKpiContratos();
    }

    @CacheResult(cacheName = "kpi-churn")
    public ChurnKpiDTO obterKpiChurn() {
        return dashboardRepository.buscarKpiChurn();
    }

    @CacheResult(cacheName = "kpi-inadimplencia")
    public InadimplenciaKpiDTO obterKpiInadimplencia() {
        return dashboardRepository.buscarKpiInadimplencia();
    }

    @CacheResult(cacheName = "kpi-estadias-excessivas")
    public List<EstadiaExcessivaDTO> obterEstadiasExcessivas() {
        return dashboardRepository.buscarEstadiasExcessivas();
    }

    @CacheResult(cacheName = "kpi-parcelas-vencer")
    public List<ParcelaVencerDTO> obterParcelasAVencer() {
        return dashboardRepository.buscarParcelasAVencer();
    }
}
