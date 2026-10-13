package ads.upf.presentation;

import ads.upf.model.DTOs.dashboard.*;
import ads.upf.services.DashboardService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@ViewScoped
@Named("dashBean")
@Getter @Setter
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DashboardService dashboardService;

    // Indicadores Operacionais e Tempo Real
    private OcupacaoKpiDTO ocupacaoKpiDTO;
    private VeiculosAtuaisKpiDTO veiculosAtuaisKpiDTO;
    private TempoMedioKpiDTO tempoMedioKpiDTO;
    private GiroVagasKpiDTO giroVagasKpiDTO;
    private ReceitaHojeKpiDTO receitaHojeKpiDTO;

    // Indicadores do Mês e Contratos
    private ReceitaMesKpiDTO receitaMesKpiDTO;
    private ContratosKpiDTO contratosKpiDTO;
    private ChurnKpiDTO churnKpiDTO;
    private InadimplenciaKpiDTO inadimplenciaKpiDTO;

    // Alertas e Exceções Operacionais
    private List<EstadiaExcessivaDTO> estadiasExcessivas;
    private List<ParcelaVencerDTO> parcelasAVencer;

    @PostConstruct
    public void carregarDados() {
        carregarIndicadores();
    }

    public void atualizarIndicadores() {
        dashboardService.limparTodosCaches();
        carregarIndicadores();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Atualizado", "Indicadores atualizados com sucesso!"));
    }

    private void carregarIndicadores() {
        this.ocupacaoKpiDTO = dashboardService.obterKpiOcupacao();
        this.veiculosAtuaisKpiDTO = dashboardService.obterKpiVeiculosAtuais();
        this.tempoMedioKpiDTO = dashboardService.obterKpiTempoMedio();
        this.giroVagasKpiDTO = dashboardService.obterKpiGiroVagas();
        this.receitaHojeKpiDTO = dashboardService.obterKpiReceitaHoje();

        this.receitaMesKpiDTO = dashboardService.obterKpiReceitaMes();
        this.contratosKpiDTO = dashboardService.obterKpiContratos();
        this.churnKpiDTO = dashboardService.obterKpiChurn();
        this.inadimplenciaKpiDTO = dashboardService.obterKpiInadimplencia();

        this.estadiasExcessivas = dashboardService.obterEstadiasExcessivas();
        this.parcelasAVencer = dashboardService.obterParcelasAVencer();
    }
}
