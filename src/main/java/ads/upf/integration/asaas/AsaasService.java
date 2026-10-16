package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.AsaasCustomerRequest;
import ads.upf.integration.asaas.DTOs.AsaasCustomerResponse;
import ads.upf.integration.asaas.DTOs.AsaasPaymentRequest;
import ads.upf.integration.asaas.DTOs.AsaasPaymentResponse;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Parcela;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AsaasService {

    @Inject
    @RestClient
    AsaasClient asaas;

    @Transactional
    public String garantirCliente(Cliente c) {
        if (c.getAsaasCustomerId() != null) {
            return c.getAsaasCustomerId();
        }

        String cpfSoDigitos = c.getCpf().replaceAll("\\D", "");
        AsaasCustomerResponse resp = asaas.criarCliente(new AsaasCustomerRequest(
           c.getNomeCompleto(),
           cpfSoDigitos,
           c.getEmail(),
           c.getTelefone()
        ));

        c.setAsaasCustomerId(resp.id());
        return resp.id();
    }

    @Transactional
    public void cobrarParcela(Parcela p ) {
        String customer = garantirCliente(p.getContrato().getCliente());

        AsaasPaymentResponse resp = asaas.criarCobranca(new AsaasPaymentRequest(
                customer,
                "UNDEFINED",
                p.getValor(),
                p.getDataVencimento().toString(),
                "Cobrança da parcela: " + p.getNumeroParcela(),
                "parcela ID: " + p.getId()
        ));

        p.setInvoiceUrl(resp.invoiceUrl());
        p.setAsaasPaymentId(resp.id());
        p.setCobrancaEnviada(true);
    }
}
