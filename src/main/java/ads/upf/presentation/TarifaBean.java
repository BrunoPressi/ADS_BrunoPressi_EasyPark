package ads.upf.presentation;

import ads.upf.model.DTOs.tarifa.TarifaCreateDTO;
import ads.upf.model.DTOs.tarifa.TarifaResponseDTO;
import ads.upf.services.TarifaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ViewScoped
@Named("tarifaBean")
@Getter @Setter
public class TarifaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TarifaService tarifaService;

    private List<TarifaResponseDTO> tarifasList = new ArrayList<>();
    private TarifaCreateDTO novaTarifa;

    @PostConstruct
    protected void postConstruct() {
        this.tarifasList = tarifaService.buscarTodasTarifas();
        this.novaTarifa = new TarifaCreateDTO();
    }

    public void prepararNovaTarifa() {
        this.novaTarifa = new TarifaCreateDTO();
    }

    public void selecionarTarifa(TarifaResponseDTO tarifa) {
        this.novaTarifa = new TarifaCreateDTO();
        this.novaTarifa.setId(tarifa.getId());
        this.novaTarifa.setValorHora(tarifa.getValorHora());
        this.novaTarifa.setValorFracaoHora(tarifa.getValorFracaoHora());
    }

    public void atualizarFracaoHora() {
        if (this.novaTarifa != null) {
            this.novaTarifa.calcularValorFracaoHora();
        }
    }

    public void processarTarifa() {
        try {
            boolean editando = this.novaTarifa.getId() != null;
            this.novaTarifa.calcularValorFracaoHora();

            if (!this.novaTarifa.verificarValorFracaoHora()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erro ao salvar tarifa",
                                "O valor da fração hora deve ser a metade do valor hora total!"));
                return;
            }

            tarifaService.criarNovaTarifa(this.novaTarifa);
            this.tarifasList = tarifaService.buscarTodasTarifas();
            this.novaTarifa = new TarifaCreateDTO();
            PrimeFaces.current().executeScript("PF('dialogTarifa').hide()");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Sucesso",
                            editando ? "Tarifa atualizada com sucesso!" : "Tarifa criada com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao salvar tarifa", e.getMessage()));
        }
    }

    public void desativarTarifa(TarifaResponseDTO tarifa) {
        try {
            tarifaService.desativarTarifa(tarifa.getId());
            this.tarifasList = tarifaService.buscarTodasTarifas();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Sucesso",
                            "A tarifa foi desativada!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao desativar tarifa", e.getMessage()));
        }
    }

}
