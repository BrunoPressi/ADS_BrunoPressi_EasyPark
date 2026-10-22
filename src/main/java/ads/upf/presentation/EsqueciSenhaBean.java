package ads.upf.presentation;

import ads.upf.model.DTOs.usuario.EsqueciSenhaDTO;
import ads.upf.services.AlterarSenhaUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ViewScoped
@Named("esqueciSenhaBean")
@Getter @Setter
public class EsqueciSenhaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    protected AlterarSenhaUseCase alterarSenhaUseCase;

    private EsqueciSenhaDTO senhaDTO;

    @PostConstruct
    protected void postConstruct() {
        this.senhaDTO = new EsqueciSenhaDTO();
    }

    public void enviarEmail() {
        try {
            alterarSenhaUseCase.enviarEmailRedefinicaoSenha(senhaDTO.getEmail());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            "Sucesso!",
                            "Verifique a caixa de entrada do seu E-mail."));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erro ao enviar E-mail!",
                            e.getMessage()));
        }
    }

}
