package ads.upf.model.mappers;

import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.entities.Funcionario;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface FuncionarioMapper {

    FuncionarioMapper INSTANCE = Mappers.getMapper( FuncionarioMapper.class );

    Funcionario toFuncionario(FuncionarioCreateDTO funcionarioCreateDTO);
    void updateFuncionarioFromDto(FuncionarioCreateDTO funcionarioCreateDTO, @MappingTarget Funcionario funcionario);
    FuncionarioResponseDTO toFuncionarioDto(Funcionario funcionario);
    List<FuncionarioResponseDTO> toFuncionarioDtoList(List<Funcionario> funcionarioList);

    @AfterMapping
    default void normalizarDados(Funcionario funcionario) {
        if (funcionario.getEmail() != null) {
            funcionario.setEmail(funcionario.getEmail().trim().toLowerCase());
        }
        if (funcionario.getNomeCompleto() != null) {
            funcionario.setNomeCompleto(funcionario.getNomeCompleto().trim());
        }
        if (funcionario.getTelefone() != null) {
            funcionario.setTelefone(funcionario.getTelefone().trim());
        }
    }

}
