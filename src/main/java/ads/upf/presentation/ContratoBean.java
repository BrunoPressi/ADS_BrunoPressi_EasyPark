package ads.upf.presentation;

import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.DTOs.parcela.ParcelaResponseDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.ClienteService;
import ads.upf.services.ContratoService;
import ads.upf.services.VeiculoService;
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

    @Inject
    protected VeiculoService veiculoService;

    private GenericLazyDataModel<ContratoResponseDTO> lazyDataModel;
    private ContratoCreateDTO contrato;

    private List<ParcelaResponseDTO> parcelas = new ArrayList<>();
    private ClienteResponseDTO cliente;
    private VeiculoResponseDTO veiculo;

    private String chaveUnicaFiltro;

    @PostConstruct
    protected void postConstruct() {
        this.contrato = new ContratoCreateDTO();
        this.cliente = new ClienteResponseDTO();
        this.veiculo = new VeiculoResponseDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> contratoService.contar(),
                (first, pageSize) -> contratoService.listarPaginado(first, pageSize),
                ContratoResponseDTO::getId,
                (termo) -> contratoService.buscarPorCliente(termo)
        );
    }

    public void selecionarParcelas(ContratoResponseDTO contratoResponseDTO) {
        this.parcelas = contratoService.buscarParcelasDoContrato(contratoResponseDTO.getId());
    }

    public void selecionarCliente(ContratoResponseDTO contratoResponseDTO) {
        this.cliente = contratoResponseDTO.getCliente();
    }

    public void selecionarVeiculo(ContratoResponseDTO contratoResponseDTO) {
        this.veiculo = contratoResponseDTO.getVeiculo();
    }

    public void selecionarContrato(ContratoResponseDTO contratoResponseDTO) {
        this.contrato.setContratoId(contratoResponseDTO.getId());
        this.contrato.setDataInicio(contratoResponseDTO.getDataInicio());
        this.contrato.setDataTermino(contratoResponseDTO.getDataTermino());
        this.contrato.setCliente(contratoResponseDTO.getCliente());
        this.contrato.setVeiculo(contratoResponseDTO.getVeiculo());
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

            contratoService.salvarContrato(contrato);
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage( "Contrato salvo com sucesso!"));
            limpar();
            PrimeFaces.current().executeScript("PF('dialogContrato').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao salvar contrato", e.getMessage()));
        }
    }

    public void cancelarContrato(ContratoResponseDTO contratoResponseDTO) {
        try {
            contratoService.cancelarContrato(contratoResponseDTO.getId());
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage( "Sucesso", "O contrato foi cancelado!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao Cancelar Contrato",
                            e.getMessage()));
        }
    }

    public void novoContrato() {
        this.contrato = new ContratoCreateDTO();
    }

    public List<ClienteResponseDTO> buscarClientePorCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return Collections.emptyList();
        }
        ClienteResponseDTO cliente = clienteService.buscarPorChaveUnica(cpf);
        return cliente != null ? List.of(cliente) : Collections.emptyList();
    }

    public List<VeiculoResponseDTO> buscarVeiculoPelaPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return Collections.emptyList();
        }
        VeiculoResponseDTO veiculo = veiculoService.buscarVeiculoPelaPlaca(placa);
        return veiculo != null ? List.of(veiculo) : Collections.emptyList();
    }

    public void pesquisar() {
        lazyDataModel.buscar(chaveUnicaFiltro);
    }

    public void limpar() {
        this.contrato = new ContratoCreateDTO();
        this.chaveUnicaFiltro = null;
        this.cliente = new ClienteResponseDTO();
        this.parcelas = new ArrayList<>();
        lazyDataModel.limpar();
    }

}
