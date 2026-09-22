package ads.upf.model.mappers;

import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.entities.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface FuncionarioMapper {

    FuncionarioMapper INSTANCE = Mappers.getMapper( FuncionarioMapper.class );

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "criadoPor", ignore = true)
    @Mapping(target = "atualizadoPor", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "role", ignore = true)
    Funcionario toFuncionario(FuncionarioCreateDTO usuarioCreateDTO);
    FuncionarioResponseDTO toFuncionarioDto(Funcionario funcionario);
    List<FuncionarioResponseDTO> toFuncionarioDtoList(List<Funcionario> funcionarioList);
}
