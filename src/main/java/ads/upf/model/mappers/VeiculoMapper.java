package ads.upf.model.mappers;

import ads.upf.model.DTOs.veiculo.VeiculoCreateDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.entities.Veiculo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface VeiculoMapper {

    VeiculoMapper INSTANCE = Mappers.getMapper( VeiculoMapper.class );

    Veiculo toVeiculo(VeiculoCreateDTO veiculoCreateDTO);
    Veiculo toVeiculo(VeiculoResponseDTO veiculoResponseDTO);
    VeiculoResponseDTO toVeiculoDto(Veiculo veiculo);
    List<VeiculoResponseDTO> toVeiculoDtoList (List<Veiculo> veiculoList);

}
