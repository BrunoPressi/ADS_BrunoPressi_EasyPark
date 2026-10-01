package ads.upf.presentation.converters;

import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.services.VeiculoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("veiculoConverter")
@ApplicationScoped
@FacesConverter(value = "veiculoConverter", managed = true)
public class VeiculoConverter implements Converter<VeiculoResponseDTO> {

    @Inject
    private VeiculoService veiculoService;

    @Override
    public VeiculoResponseDTO getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null || value.isBlank()) {
            return null;
        }
        return veiculoService.buscarVeiculoPelaPlaca(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, VeiculoResponseDTO value) throws ConverterException {
        if (value == null || value.getPlaca() == null) {
            return "";
        }
        return value.getPlaca();
    }
}
