package ads.upf.model.mappers;

import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.entities.Contrato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ContratoMapper {

    ContratoMapper INSTANCE = Mappers.getMapper( ContratoMapper.class );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "criadoPor", ignore = true)
    @Mapping(target = "atualizadoPor", ignore = true)
    @Mapping(target = "valorContratado", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "parcelas", ignore = true)
    @Mapping(target = "contratoTipo", ignore = true)
    @Mapping(target = "status", ignore = true)
    Contrato toContrato(ContratoCreateDTO contratoCreateDTO);
    ContratoResponseDTO toContratoDto(Contrato contrato);
    List<ContratoResponseDTO> toContratoDtoList(List<Contrato> contratoList);

}
