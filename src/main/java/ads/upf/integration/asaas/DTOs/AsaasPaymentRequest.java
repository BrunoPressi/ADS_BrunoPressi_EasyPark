package ads.upf.integration.asaas.DTOs;

import java.math.BigDecimal;

public record AsaasPaymentRequest(
        String customer,
        String billingType,
        BigDecimal value,
        String dueDate,
        String description,
        String externalReference,
        Fine fine) {

    /** Multa por atraso cobrada pelo Asaas (percentual sobre o valor da cobrança). */
    public record Fine(BigDecimal value, String type) {
        public Fine(BigDecimal percentual) {
            this(percentual, "PERCENTAGE");
        }
    }

    public AsaasPaymentRequest(String customer, String billingType, BigDecimal value,
                               String dueDate, String description, String externalReference) {
        this(customer, billingType, value, dueDate, description, externalReference, null);
    }
}