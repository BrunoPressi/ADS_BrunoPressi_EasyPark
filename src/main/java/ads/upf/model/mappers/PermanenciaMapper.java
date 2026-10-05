package ads.upf.model.mappers;

import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Permanencia;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface PermanenciaMapper {

    PermanenciaMapper INSTANCE = Mappers.getMapper( PermanenciaMapper.class );

    PermanenciaResponseDTO toPermanenciaDto(Permanencia permanencia);
    List<PermanenciaResponseDTO> toPermanenciaDtoList(List<Permanencia> permanenciaList);

}
