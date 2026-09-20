package ads.upf.presentation;

import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.services.FuncionarioService;
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

import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
@Getter @Setter
public class FuncionarioBean {

    @Inject
    FuncionarioService funcionarioService;

    private FuncionarioCreateDTO funcionario;
    private List<FuncionarioResponseDTO> funcionarioResponseDTOList = new ArrayList<>();
    private List<FuncionarioResponseDTO> funcionariosListFiltrado = new ArrayList<>();

    @PostConstruct()
    protected void postConstruct() {
        funcionario = new FuncionarioCreateDTO();
        funcionarioResponseDTOList = funcionarioService.listarFuncionarios();
    }

    public void selecionarFuncionario(FuncionarioResponseDTO funcionarioResponseDTO) {
        funcionario.setId(funcionarioResponseDTO.getId());
        funcionario.setNomeCompleto(funcionarioResponseDTO.getNomeCompleto());
        funcionario.setEmail(funcionarioResponseDTO.getEmail());
        funcionario.setTelefone(funcionarioResponseDTO.getTelefone());
        funcionario.setDataNascimento(funcionarioResponseDTO.getDataNascimento());
    }

    public void processarFuncionario() {
        try {
            funcionarioService.salvarUsuario(funcionario);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Funcionário salvo com sucesso!"));
            this.funcionarioResponseDTOList = funcionarioService.listarFuncionarios();
            this.funcionario = new FuncionarioCreateDTO();
            PrimeFaces.current().executeScript("PF('dialogFuncionario').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao cadastrar", e.getMessage()));
        }
    }
}
