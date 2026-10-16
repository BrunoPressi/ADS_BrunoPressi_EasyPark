package ads.upf.integration.asaas.DTOs;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasWebhookEvent(String event, Payment payment) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payment(String id,
                          String status,
                          String billingType,
                          LocalDate paymentDate,
                          LocalDate clientPaymentDate,
                          BigDecimal value) {}
}