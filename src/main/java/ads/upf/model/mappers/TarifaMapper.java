package ads.upf.model.mappers;

import ads.upf.model.DTOs.tarifa.TarifaCreateDTO;
import ads.upf.model.DTOs.tarifa.TarifaResponseDTO;
import ads.upf.model.entities.Tarifa;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface TarifaMapper {

    TarifaMapper INSTANCE = Mappers.getMapper( TarifaMapper.class );

    TarifaResponseDTO toTarifaDto(Tarifa tarifa);
    Tarifa toTarifa(TarifaCreateDTO tarifaCreateDTO);
    void toUpdateFromTarifaDto(TarifaCreateDTO tarifaCreateDTO, @MappingTarget Tarifa tarifa);

    List<TarifaResponseDTO> toTarifaDtoList(List<Tarifa> tarifaList);

}
