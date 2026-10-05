package ads.upf.presentation;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Expõe informações do usuário autenticado para as views JSF.
 *
 * COMO FUNCIONA:
 *   O Quarkus preenche automaticamente o SecurityIdentity após cada
 *   requisição autenticada. Ele contém: o Principal (nome do usuário),
 *   os Roles (papéis/grupos) e os atributos extras definidos no realm JDBC.
 *
 * ESCOPO @RequestScoped:
 *   Criado a cada requisição HTTP e destruído ao final dela. Garante
 *   que os dados de identidade sejam sempre lidos frescos do contexto
 *   de segurança atual, sem risco de estado obsoleto entre requisições.
 *
 * @Named("userSession"):
 *   Torna o bean acessível nas views Facelets pelo nome EL #{userSession}.
 *   Exemplo de uso: #{userSession.username}, #{userSession.admin}.
 */
@Named("userSession")
@RequestScoped
public class UserSessionBean {

    /**
     * Representa a identidade de segurança do usuário atual.
     * Injetado pelo CDI — é anônimo (isAnonymous() == true) quando
     * a requisição não está autenticada.
     */
    @Inject
    SecurityIdentity identity;

    public String getUsername() {
        if (identity.isAnonymous()) {
            return "anônimo";
        }
        return identity.getPrincipal().getName();
    }

    public String getRoles() {
        var roles = identity.getRoles();
        return roles.isEmpty() ? "sem papéis" : String.join(", ", roles);
    }

    public boolean isAuthenticated() {
        return !identity.isAnonymous();
    }


    public boolean isGerente() {
        return identity.hasRole("gerente");
    }

    public boolean isAtendente() {
        return identity.hasRole("atendente");
    }
}
