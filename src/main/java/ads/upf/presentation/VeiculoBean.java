package ads.upf.presentation;

import ads.upf.model.DTOs.veiculo.VeiculoCreateDTO;
import ads.upf.services.VeiculoService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

import java.io.Serializable;

@Named("veiculoBean")
@ApplicationScoped
@Getter @Setter
public class VeiculoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private VeiculoService veiculoService;

    private VeiculoCreateDTO veiculo;

    @PostConstruct
    void postConstruct() {
        this.veiculo = new VeiculoCreateDTO();
    }

    public void processarVeiculo() {
        try {
            veiculoService.salvarVeiculo(veiculo);
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage("Sucesso", "Veículo salvo com sucesso!"));
            PrimeFaces.current().executeScript("PF('dialogVeiculo').hide()");
            this.veiculo = new VeiculoCreateDTO();
        } catch (Exception e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar Veículo",
                            e.getMessage()));
        }
    }

}
