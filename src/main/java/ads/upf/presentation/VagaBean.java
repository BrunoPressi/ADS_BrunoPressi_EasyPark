package ads.upf.presentation;

import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaEditDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.enums.VagaStatus;
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

@Named
@ViewScoped
@Getter @Setter
public class VagaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    VagaService vagaService;

    private GenericLazyDataModel<VagaResponseDTO> lazyDataModel;
    private String chaveUnicaFiltro;
    private VagaEditDTO vagaSelecionada;
    private VagaCreateDTO vagaNova;
    private boolean emManutencao;

    @PostConstruct
    protected void postConstruct() {
        this.vagaNova = new VagaCreateDTO();
        this.vagaSelecionada = new VagaEditDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> vagaService.contar(),
                (first, pageSize) -> vagaService.listarPaginado(first, pageSize),
                VagaResponseDTO::getId,
                nome -> vagaService.buscarPeloNome(nome)
        );
    }

    public void selecionarVaga(VagaResponseDTO vaga) {
        vagaSelecionada.setId(vaga.getId());
        vagaSelecionada.setNome(vaga.getNome());
        vagaSelecionada.setStatus(vaga.getStatus());
        this.emManutencao = (vagaSelecionada.getStatus()) == VagaStatus.em_manutencao;
    }

    public void editarVaga() {
        try {
            VagaStatus status = (emManutencao) ? VagaStatus.em_manutencao : VagaStatus.disponivel;
            VagaEditDTO vaga = new VagaEditDTO(
                    vagaSelecionada.getId(),
                    vagaSelecionada.getNome(),
                    status
            );
            vagaService.editarVaga(vagaSelecionada.getId(), vaga);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Vaga editada com sucesso!"));
            PrimeFaces.current().executeScript("PF('editarVagaDialog').hide()");
       }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", e.getMessage()));
        }
    }

    public void processarVaga() {
        try {
            VagaCreateDTO vaga = new VagaCreateDTO(vagaNova.getNome());
            vagaService.criarNovaVaga(vaga);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Vaga registrada!"));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", e.getMessage()));
        }
    }

    public void pesquisar() {
        lazyDataModel.buscar(chaveUnicaFiltro);
    }

    public void limparFiltro() {
        this.chaveUnicaFiltro = null;
        lazyDataModel.limpar();
    }

}
