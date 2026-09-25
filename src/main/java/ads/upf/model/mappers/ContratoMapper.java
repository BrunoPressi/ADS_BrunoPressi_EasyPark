package ads.upf.model.mappers;

import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.entities.Contrato;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ContratoMapper {

    ContratoMapper INSTANCE = Mappers.getMapper( ContratoMapper.class );

    Contrato toContrato(ContratoCreateDTO contratoCreateDTO);
    ContratoResponseDTO toContratoDto(Contrato contrato);
    List<ContratoResponseDTO> toContratoDtoList(List<Contrato> contratoList);

}
