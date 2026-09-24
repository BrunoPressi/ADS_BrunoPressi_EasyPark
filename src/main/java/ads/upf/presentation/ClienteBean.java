package ads.upf.presentation;

import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.ClienteService;
import ads.upf.services.VagaService;
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

@ViewScoped
@Named("clienteBean")
@Getter @Setter
public class ClienteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    ClienteService clienteService;
    @Inject
    VagaService vagaService;

    private GenericLazyDataModel<ClienteResponseDTO> lazyDataModel;
    private String chaveUnicaFiltro;
    private ClienteCreateDTO cliente;

    @PostConstruct
    protected void postConstruct() {
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> clienteService.contar(),
                (first, pageSize) -> clienteService.listarPaginado(first, pageSize),
                ClienteResponseDTO::getId,
                termo -> clienteService.buscarPorChaveUnica(termo)
        );
        this.cliente = new ClienteCreateDTO();
    }

    public void selecionarCliente(ClienteResponseDTO clienteResponseDTO) {
        limparCliente();
        this.cliente.setId(clienteResponseDTO.getId());
        this.cliente.setNomeCompleto(clienteResponseDTO.getNomeCompleto());
        this.cliente.setCpf(clienteResponseDTO.getCpfNormal());
        this.cliente.setTelefone(clienteResponseDTO.getTelefone());
        this.cliente.setEmail(clienteResponseDTO.getEmail());
    }

    public void processarCliente() {
        try {
            clienteService.salvarCliente(cliente);
            limparCliente();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Cliente salvo com sucesso."));
            PrimeFaces.current().executeScript("PF('dialogCliente').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro ao salvar cliente.", e.getMessage()));
        }
    }

    public void pesquisar() {
        lazyDataModel.buscar(chaveUnicaFiltro);
    }

    public void limparFiltro() {
        this.chaveUnicaFiltro = null;
        lazyDataModel.limpar();
    }

    public void limparCliente() {
        this.cliente = new ClienteCreateDTO();
    }
}
