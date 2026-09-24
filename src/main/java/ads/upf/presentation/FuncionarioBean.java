package ads.upf.presentation;

import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.FuncionarioService;
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
public class FuncionarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FuncionarioService funcionarioService;

    private FuncionarioCreateDTO funcionario;
    private GenericLazyDataModel<FuncionarioResponseDTO> lazyDataModel;
    private String chaveUnicaFiltro;

    @PostConstruct()
    protected void postConstruct() {
        this.funcionario = new FuncionarioCreateDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> funcionarioService.contar(),
                (first, pageSize) -> funcionarioService.listarPaginado(first, pageSize),
                FuncionarioResponseDTO::getId,
                (termo) -> funcionarioService.buscarPorTermo(termo)
        );
    }

    public void selecionarFuncionario(FuncionarioResponseDTO funcionarioResponseDTO) {
        limpar();
        funcionario.setId(funcionarioResponseDTO.getId());
        funcionario.setNomeCompleto(funcionarioResponseDTO.getNomeCompleto());
        funcionario.setEmail(funcionarioResponseDTO.getEmail());
        funcionario.setTelefone(funcionarioResponseDTO.getTelefone());
    }

    public void processarFuncionario() {
        try {
            funcionarioService.salvarFuncionario(funcionario);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Funcionário salvo com sucesso!"));
            limpar();
            PrimeFaces.current().executeScript("PF('dialogFuncionario').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao cadastrar", e.getMessage()));
        }
    }

    public void pesquisar() {
        lazyDataModel.buscar(chaveUnicaFiltro);
    }

    public void limparFiltro() {
        this.chaveUnicaFiltro = null;
        lazyDataModel.limpar();
    }

    public void limpar() {
        this.funcionario = new FuncionarioCreateDTO();
    }
}
