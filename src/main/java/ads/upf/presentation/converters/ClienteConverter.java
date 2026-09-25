package ads.upf.presentation.converters;

import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.services.ClienteService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("clienteConverter")
@ApplicationScoped
@FacesConverter(value = "clienteConverter", managed = true)
public class ClienteConverter implements Converter<ClienteResponseDTO> {

    @Inject
    ClienteService clienteService;

    @Override
    public ClienteResponseDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return clienteService.buscarPorChaveUnica(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ClienteResponseDTO value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}