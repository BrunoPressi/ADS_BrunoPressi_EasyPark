package ads.upf.presentation;

import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaEditDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.enums.VagaStatus;
import ads.upf.services.VagaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.exception.ConstraintViolationException;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
@Getter @Setter
public class VagaBean implements Serializable {

    @Inject
    VagaService vagaService;
    private List<VagaResponseDTO> vagasList = new ArrayList<>();
    private VagaEditDTO vagaSelecionada;
    private VagaCreateDTO vagaNova;
    private boolean emManutencao;
    private List<VagaResponseDTO> vagasListFiltrado = new ArrayList<>();

    @PostConstruct
    protected void postConstruct() {
        vagaNova = new VagaCreateDTO();
        vagaSelecionada = new VagaEditDTO();
        vagasList = vagaService.listarVagas();
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
            vagasList = vagaService.listarVagas();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Vaga editada com sucesso!"));
            PrimeFaces.current().executeScript("PF('editarVagaDialog').hide()");
       }
        catch (Exception e) {
            if (e instanceof ConstraintViolationException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao editar", "Essa vaga já está cadastrada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", e.getMessage()));
            }
        }
    }

    public void processarVaga() {
        try {
            VagaCreateDTO vaga = new VagaCreateDTO(vagaNova.getNome());
            vagaService.criarNovaVaga(vaga);
            vagasList = vagaService.listarVagas();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Vaga registrada!"));
        }
        catch (Exception e) {
            if (e instanceof ConstraintViolationException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao cadastrar", "Essa vaga já está cadastrada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", e.getMessage()));
            }
        }
    }

}
