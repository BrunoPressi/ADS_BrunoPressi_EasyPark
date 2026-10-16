package ads.upf.integration.asaas.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasPaymentResponse(
        String id,
        String status,
        String invoiceUrl) {}