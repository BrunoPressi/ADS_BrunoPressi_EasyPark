package ads.upf.presentation;

import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.services.ClienteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@ViewScoped
@Named("contratoBean")
@Getter @Setter
public class ContratoBean {

    @Inject
    ClienteService clienteService;
    private ClienteCreateDTO cliente;

    @PostConstruct
    protected void postConstruct() {
        cliente = new ClienteCreateDTO();
    }

    public void processarCliente() {
        try {
            clienteService.salvarCliente(cliente);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Cliente salvo com sucesso."));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro ao salvar cliente.", e.getMessage()));
        }
    }

}
