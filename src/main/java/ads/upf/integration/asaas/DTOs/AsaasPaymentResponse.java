package ads.upf.integration.asaas.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasPaymentResponse(
        String id,
        String status,
        String invoiceUrl,
        String billingType,
        LocalDate paymentDate,
        LocalDate clientPaymentDate,
        BigDecimal value) {

    public AsaasPaymentResponse(String id, String status, String invoiceUrl) {
        this(id, status, invoiceUrl, null, null, null, null);
    }
}
