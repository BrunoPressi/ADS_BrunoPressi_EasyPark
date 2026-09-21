package ads.upf.presentation;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gerencia o fluxo de logout da aplicação.
 *
 * FLUXO DE LOGOUT (Form Authentication no Quarkus):
 *
 *   1. HttpServletRequest.logout() — remove o principal do contexto de segurança
 *      do request atual, mas NÃO apaga o cookie persistente do Quarkus.
 *
 *   2. Expiração do cookie "quarkus-credential" — passo crítico. O Quarkus form
 *      auth armazena as credenciais num cookie assinado auto-suficiente: a cada
 *      request o Quarkus valida a assinatura do cookie e reautentica o usuário,
 *      independente de haver sessão HTTP ativa. Sem apagar o cookie o usuário
 *      permanece autenticado mesmo após invalidateSession().
 *
 *   3. ExternalContext.invalidateSession() — descarta a sessão HTTP/JSF (ViewState,
 *      beans @SessionScoped).
 *
 *   4. ExternalContext.redirect() — envia o HTTP 302 diretamente. Retornar uma
 *      String de navegação JSF não funciona aqui: o navigation handler tenta
 *      acessar a sessão já destruída no passo 3.
 *
 *   5. FacesContext.responseComplete() — impede que o ciclo de vida JSF processe
 *      mais alguma coisa após o redirect.
 */
@Named("logoutBean")
@RequestScoped
public class LogoutBean {

    public void logout() {
        FacesContext fc = FacesContext.getCurrentInstance();
        var ec = fc.getExternalContext();
        try {
            HttpServletRequest  request  = (HttpServletRequest)  ec.getRequest();
            HttpServletResponse response = (HttpServletResponse) ec.getResponse();

            // Passo 1: remove o principal do contexto de segurança do request
            request.logout();

            // Passo 2: expira o cookie persistente do Quarkus form auth.
            // Max-Age=0 instrui o browser a descartar o cookie imediatamente.
            // O nome padrão é "quarkus-credential" (quarkus.http.auth.form.cookie-name).
            Cookie deleteCookie = new Cookie("quarkus-credential", "");
            deleteCookie.setPath("/");
            deleteCookie.setMaxAge(0);
            deleteCookie.setHttpOnly(true);
            response.addCookie(deleteCookie);

            // Passo 3: invalida a sessão HTTP/JSF
            ec.invalidateSession();

            // Passo 4: redireciona diretamente, sem passar pelo navigation handler
            ec.redirect(ec.getRequestContextPath() + "/login.xhtml");

            // Passo 5: encerra o ciclo de vida JSF
            fc.responseComplete();

        } catch (ServletException | IOException e) {
            Log.error("Erro ao encerrar a sessão de segurança", e);
        }
    }
}