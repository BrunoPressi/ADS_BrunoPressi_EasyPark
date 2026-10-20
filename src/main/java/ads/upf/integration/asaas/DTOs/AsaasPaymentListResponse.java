package ads.upf.integration.asaas.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasPaymentListResponse(List<AsaasPaymentResponse> data) {}
