package ads.upf.integration.asaas.DTOs;

import java.math.BigDecimal;

public record AsaasPaymentRequest(
        String customer,
        String billingType,
        BigDecimal value,
        String dueDate,
        String description,
        String externalReference) {}