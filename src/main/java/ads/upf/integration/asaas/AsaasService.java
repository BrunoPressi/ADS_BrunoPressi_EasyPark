package ads.upf.integration.asaas;

import ads.upf.integration.asaas.DTOs.*;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.repositories.ParcelaRepository;
import ads.upf.services.ParcelaService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AsaasService {

    @Inject
    @RestClient
    AsaasClient asaas;

    @Inject
    ParcelaRepository parcelaRepository;

    @Inject
    ParcelaService parcelaService;

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

    /**
     * Tenta excluir as cobranças no Asaas sem interromper o cancelamento local caso a API esteja indisponível.
     * As parcelas cuja exclusão no Asaas falhar permanecem com status 'cancelada' e 'asaasPaymentId != null',
     * sendo reprocessadas posteriormente pelo job de background.
     */
    public void deletarCobrancas(List<Parcela> parcelas) {
        for (Parcela p : parcelas) {
            if (p.getAsaasPaymentId() == null) continue;
            try {
                asaas.deletarCobranca(p.getAsaasPaymentId());
                p.setAsaasPaymentId(null);
                p.setInvoiceUrl(null);
                p.setCobrancaEnviada(false);
            } catch (Exception e) {
                Log.warnf(e, "Falha ao deletar imediatamente no Asaas a cobrança %s da parcela %d. Será reprocessada pelo job.",
                        p.getAsaasPaymentId(), p.getId());
            }
        }
    }

    @Transactional
    public void removerCobrancaCancelada(Long parcelaId) {
        Parcela p = parcelaRepository.findById(parcelaId);
        if (p == null || p.getAsaasPaymentId() == null || p.getStatus() != ParcelaStatus.cancelada) {
            return;
        }

        asaas.deletarCobranca(p.getAsaasPaymentId());
        p.setAsaasPaymentId(null);
        p.setInvoiceUrl(null);
        p.setCobrancaEnviada(false);
    }

    /**
     * Consulta o status atual da cobrança no Asaas e sincroniza o estado local da parcela
     * (reconciliação ativa para contornar quedas prolongadas do Webhook).
     *
     * @return true se houve atualização de status na parcela local
     */
    @Transactional
    public boolean sincronizarParcela(Long parcelaId) {
        Parcela p = parcelaRepository.findById(parcelaId);
        if (p == null || p.getAsaasPaymentId() == null) return false;
        if (p.getStatus() == ParcelaStatus.paga || p.getStatus() == ParcelaStatus.cancelada) return false;

        AsaasPaymentResponse resp = asaas.consultarCobranca(p.getAsaasPaymentId());
        if (resp == null || resp.status() == null) return false;

        LocalDate dataPgto = resp.paymentDate() != null
                ? resp.paymentDate()
                : (resp.clientPaymentDate() != null ? resp.clientPaymentDate() : LocalDate.now());
        String billingType = resp.billingType() != null ? resp.billingType() : "UNDEFINED";

        AsaasWebhookEvent.Payment paymentEvent = new AsaasWebhookEvent.Payment(
                p.getAsaasPaymentId(),
                resp.status(),
                billingType,
                dataPgto,
                resp.clientPaymentDate(),
                resp.value() != null ? resp.value() : p.getValor()
        );

        return switch (resp.status()) {
            case "RECEIVED", "CONFIRMED", "RECEIVED_IN_CASH" -> {
                parcelaService.marcarComoPago(paymentEvent);
                yield true;
            }
            case "OVERDUE" -> {
                if (p.getStatus() != ParcelaStatus.atrasada) {
                    parcelaService.marcarComoAtrasada(paymentEvent);
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }
}
