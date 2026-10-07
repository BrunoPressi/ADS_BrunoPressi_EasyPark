package ads.upf.presentation;

import ads.upf.model.DTOs.permanencia.EntradaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.EntradaUseCase;
import ads.upf.services.PermanenciaService;
import ads.upf.services.SaidaUseCase;
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

import static jakarta.faces.application.FacesMessage.SEVERITY_ERROR;

@ViewScoped
@Named("permanenciaBean")
@Getter @Setter
public class PermanenciaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntradaUseCase entradaService;

    @Inject
    private SaidaUseCase saidaService;

    @Inject
    private PermanenciaService permanenciaService;

    @Inject
    private UserSessionBean userSessionBean;

    private GenericLazyDataModel<PermanenciaResponseDTO> lazyDataModel;

    private EntradaCreateDTO entrada;

    @PostConstruct
    protected void postConstruct() {
        this.entrada = new EntradaCreateDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> permanenciaService.contar(),
                ( first, pageSize) -> permanenciaService.listarPaginado(first, pageSize),
                PermanenciaResponseDTO::getId,
                null
        );
    }

    public void processarEntrada() {
        try {
            entrada.setFuncionarioEmail(userSessionBean.getUsername());
            entradaService.novaEntrada(entrada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Sucesso", "Entrada registrada!"));
            PrimeFaces.current().executeScript("PF('dialogEntrada').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR,"Erro ao registrar entrada", e.getMessage()));
        }
    }

    public void processarSaida(String placa) {
        try {
            saidaService.novaSaida(placa);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Sucesso", "Saída registrada!"));
            PrimeFaces.current().executeScript("PF('dialogEntrada').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR,"Erro ao registrar saída", e.getMessage()));
        }
    }

    public void novaEntrada() {
        this.entrada = new EntradaCreateDTO();
    }

}
