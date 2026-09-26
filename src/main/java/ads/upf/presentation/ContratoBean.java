package ads.upf.presentation;

import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.ClienteService;
import ads.upf.services.ContratoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named("contratoBean")
@ViewScoped
@Getter @Setter
public class ContratoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    protected ContratoService contratoService;

    @Inject
    protected ClienteService clienteService;

    private GenericLazyDataModel<ContratoResponseDTO> lazyDataModel;
    private ContratoCreateDTO contrato;

    @NotNull(message = "O cliente é obrigatório")
    private ClienteResponseDTO clienteSelecionado;

    private String chaveUnicaFiltro;

    @PostConstruct
    protected void postConstruct() {
        this.contrato = new ContratoCreateDTO();
        this.clienteSelecionado = new ClienteResponseDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> contratoService.contar(),
                (first, pageSize) -> contratoService.listarPaginado(first, pageSize),
                ContratoResponseDTO::getId,
                (termo) -> contratoService.buscarPorCliente(termo)
        );
    }

    public void selecionarCliente(ContratoResponseDTO contratoResponseDTO) {
        this.clienteSelecionado = contratoResponseDTO.getCliente();
    }

    public void processarContrato() {
        try {

            if (!contrato.isPeriodoMinimoValido()) {
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erro ao criar contrato",
                                "O contrato deve ter a duração mínima de um mês"));
                return;
            }

            contrato.setClienteId(clienteSelecionado.getId());
            contratoService.salvarContrato(contrato);
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage( "Contrato criado com sucesso!"));
            limpar();
            PrimeFaces.current().executeScript("PF('dialogContrato').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao criar contrato", e.getMessage()));
        }
    }

    public List<ClienteResponseDTO> buscarClientePorCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return Collections.emptyList();
        }
        ClienteResponseDTO cliente = clienteService.buscarPorChaveUnica(cpf);
        return cliente != null ? List.of(cliente) : Collections.emptyList();
    }

    public void pesquisar() {
        lazyDataModel.buscar(chaveUnicaFiltro);
    }

    public void limpar() {
        this.contrato = new ContratoCreateDTO();
        this.clienteSelecionado = new ClienteResponseDTO();
        this.chaveUnicaFiltro = null;
        lazyDataModel.limpar();
    }

}
