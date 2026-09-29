package ads.upf.model.mappers;

import ads.upf.model.DTOs.parcela.ParcelaResponseDTO;
import ads.upf.model.entities.Parcela;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ParcelaMapper {

    ParcelaMapper INSTANCE = Mappers.getMapper( ParcelaMapper.class );

    ParcelaResponseDTO toParcelaDto(Parcela parcela);
    List<ParcelaResponseDTO> toParcelasDtoList(List<Parcela> parcelas);

}
