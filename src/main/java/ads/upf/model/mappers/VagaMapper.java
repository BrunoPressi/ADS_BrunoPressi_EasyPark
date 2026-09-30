package ads.upf.model.mappers;

import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface VagaMapper {

    VagaMapper INSTANCE = Mappers.getMapper( VagaMapper.class );

    Vaga toVaga(VagaCreateDTO vagaDTO);
    void toUpdateFromVagaDto(VagaCreateDTO vagaCreateDTO, @MappingTarget Vaga vaga);
    VagaResponseDTO toDto(Vaga vaga);
    List<VagaResponseDTO> toDtoList(List<Vaga> vagaList);

    @AfterMapping
    default void normalizarDados(Vaga vaga) {
        if (vaga.getNome() != null) {
            vaga.setNome(vaga.getNome().toLowerCase().trim());
        }
    }

}
