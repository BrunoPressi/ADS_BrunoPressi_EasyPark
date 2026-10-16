package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.*;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RegisterRestClient(configKey = "asaas")
@ClientHeaderParam(name = "access_token", value = "${asaas.api-key}")
@ClientHeaderParam(name = "User-Agent", value = "EasyPark")
@Path("/")
public interface AsaasClient {

    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @POST
    @Path("/customers")
    AsaasCustomerResponse criarCliente(AsaasCustomerRequest req);

    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @POST @Path("/payments")
    AsaasPaymentResponse criarCobranca(AsaasPaymentRequest req);

    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @GET
    @Path("/payments/{id}/pixQrCode")
    AsaasPixQrCodeResponse obterPixQrCode(@PathParam("id") String id);

    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @GET @Path("/payments/{id}")
    AsaasPaymentResponse consultarCobranca(@PathParam("id") String id);
}