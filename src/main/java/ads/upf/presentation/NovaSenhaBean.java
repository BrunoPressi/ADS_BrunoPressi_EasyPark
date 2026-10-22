package ads.upf.presentation;

import ads.upf.exceptions.TokenInvalidoException;
import ads.upf.model.DTOs.funcionario.DefinirSenhaDTO;
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
import java.util.Objects;
import java.util.UUID;

@ViewScoped
@Named("senhaBean")
@Getter @Setter
public class NovaSenhaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String PAGINA_NAO_ENCONTRADA = "/pages/public/nao-encontrado.xhtml?faces-redirect=true";
    private static final String LOGIN = "/login.xhtml?faces-redirect=true";

    @Inject
    protected AlterarSenhaUseCase alterarSenhaUseCase;

    private DefinirSenhaDTO senhaDTO;
    private String token;

    @PostConstruct
    protected void postConstruct() {
        this.senhaDTO = new DefinirSenhaDTO();
    }

    /**
     * Executado pelo f:viewAction. Retorna o outcome de navegacao:
     * redireciona para "nao encontrado" se o token for nulo, inexistente ou expirado.
     */
    public String init() {
        try {
            alterarSenhaUseCase.validarToken(tokenAsUuid());
            return null;
        } catch (TokenInvalidoException e) {
            return PAGINA_NAO_ENCONTRADA;
        }
    }

    /**
     * O token chega como String porque o EL do Quarkus falha ao converter
     * String para UUID automaticamente no f:viewParam.
     */
    private UUID tokenAsUuid() {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(token.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String definirSenha() {
        try {
            if (!Objects.equals(senhaDTO.getNovaSenha(), senhaDTO.getNovaSenhaConfirmar())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erro ao definir senha",
                                "As senhas não coincidem!"));
                return null;
            }
            alterarSenhaUseCase.alterarSenha(senhaDTO.getNovaSenha(), tokenAsUuid());

            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Sucesso!", "Senha definida. Faça login para continuar."));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return LOGIN;
        } catch (TokenInvalidoException e) {
            return PAGINA_NAO_ENCONTRADA;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao definir senha",
                            "Não foi possível definir a senha. Tente novamente."));
            return null;
        }
    }

}
