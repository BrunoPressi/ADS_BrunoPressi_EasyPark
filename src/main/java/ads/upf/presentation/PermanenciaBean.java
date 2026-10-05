package ads.upf.presentation;

import ads.upf.model.DTOs.permanencia.PermanenciaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.PermanenciaService;
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
    private PermanenciaService permanenciaService;

    private GenericLazyDataModel<PermanenciaResponseDTO> lazyDataModel;

    private PermanenciaCreateDTO entrada;

    @PostConstruct
    protected void postConstruct() {
        this.entrada = new PermanenciaCreateDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> permanenciaService.contar(),
                ( first, pageSize) -> permanenciaService.listarPaginado(first, pageSize),
                PermanenciaResponseDTO::getId,
                null
        );
    }

    public void processarEntrada() {
        try {
            permanenciaService.novaEntrada(entrada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Sucesso", "Entrada registrada!"));
            PrimeFaces.current().executeScript("PF('dialogEntrada').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR,"Erro ao registrar entrada", e.getMessage()));
        }
    }

    public void novaEntrada() {
        this.entrada = new PermanenciaCreateDTO();
    }

}
