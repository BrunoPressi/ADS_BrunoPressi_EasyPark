package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.AsaasCustomerRequest;
import ads.upf.integration.asaas.DTOs.AsaasCustomerResponse;
import ads.upf.integration.asaas.DTOs.AsaasPaymentRequest;
import ads.upf.integration.asaas.DTOs.AsaasPaymentResponse;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Parcela;
import ads.upf.repositories.ParcelaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class AsaasService {

    @Inject
    @RestClient
    AsaasClient asaas;

    @Inject
    ParcelaRepository parcelaRepository;

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
    public void cobrarParcela(Long parcelaId) {
        Parcela p = parcelaRepository.findById(parcelaId);
        if (p == null || p.getAsaasPaymentId() != null) return;

        String referencia = String.valueOf(p.getId());

        // Evita duplicidade: cobrança pode já existir no Asaas se um commit local anterior falhou
        AsaasPaymentResponse resp = asaas.buscarCobrancasPorReferencia(referencia).data().stream()
                .findFirst()
                .orElse(null);

        if (resp == null) {
            String customer = garantirCliente(p.getContrato().getCliente());

            resp = asaas.criarCobranca(new AsaasPaymentRequest(
                    customer,
                    "UNDEFINED",
                    p.getValor(),
                    p.getDataVencimento().toString(),
                    "Cobrança da parcela: " + p.getNumeroParcela(),
                    referencia
            ));
        }

        p.setInvoiceUrl(resp.invoiceUrl());
        p.setAsaasPaymentId(resp.id());
        p.setCobrancaEnviada(true);
    }

    public void deletarCobrancas(List<Parcela> parcelas) {
        parcelas.forEach(
                (p) -> asaas.deletarCobranca(p.getAsaasPaymentId())
        );
    }
}
