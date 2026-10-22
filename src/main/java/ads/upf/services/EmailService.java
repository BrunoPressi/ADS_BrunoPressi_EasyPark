package ads.upf.services;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class EmailService {

    @Inject
    TransactionSynchronizationRegistry txRegistry;

    @CheckedTemplate(basePath = "emails")
    static class Emails {
        public static native MailTemplate.MailTemplateInstance definirSenhaEmail(UUID token, String emailUsuario);
    }

    /**
     * Envia o e-mail somente apos o commit da transacao corrente (se houver),
     * evitando enviar um link para um token que nao foi persistido.
     */
    public void enviarEmailSenha(UUID token, String emailUsuario) {
        if (txRegistry.getTransactionStatus() == Status.STATUS_ACTIVE) {
            txRegistry.registerInterposedSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == Status.STATUS_COMMITTED) {
                        enviar(token, emailUsuario);
                    }
                }
            });
        } else {
            enviar(token, emailUsuario);
        }
    }

    private void enviar(UUID token, String emailUsuario) {
        Emails.definirSenhaEmail(token, emailUsuario)
                .to(emailUsuario)
                .subject("EasyPark - Defina sua Senha!")
                .send()
                .await().atMost(Duration.ofSeconds(10));
    }

}
