package ads.upf.model.mappers;

import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface VagaMapper {

    VagaMapper INSTANCE = Mappers.getMapper( VagaMapper.class );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Vaga toVaga(VagaCreateDTO vagaDTO);

    VagaResponseDTO toDto(Vaga vaga);

    List<VagaResponseDTO> toDtoList(List<Vaga> vagaList);
}
