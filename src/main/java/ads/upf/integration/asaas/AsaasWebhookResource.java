package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.AsaasWebhookEvent;
import ads.upf.services.ParcelaService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/webhooks/asaas")
public class AsaasWebhookResource {

    @ConfigProperty(name = "asaas.webhook-token") String token;
    @Inject
    ParcelaService parcelaService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response receber(@HeaderParam("asaas-access-token") String recebido, AsaasWebhookEvent evt) {
        if (!token.equals(recebido)) return Response.status(401).build();

        String paymentId = evt.payment().id();
        switch (evt.event()) {
            case "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> parcelaService.marcarComoPago(evt.payment());
            case "PAYMENT_OVERDUE" -> parcelaService.marcarComoAtrasada(paymentId);
            default -> {} // ignora os outros eventos
        }
        return Response.ok().build();
    }
}
