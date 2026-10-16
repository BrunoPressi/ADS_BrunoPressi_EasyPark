package ads.upf.integration.asaas.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasCustomerRequest(
        String name,
        String cpfCnpj,
        String email,
        String mobilePhone) {}
