package ads.upf.model.mappers;

import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.entities.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ClienteMapper {

    ClienteMapper INSTANCE = Mappers.getMapper( ClienteMapper.class );

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "criadoPor", ignore = true)
    @Mapping(target = "atualizadoPor", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "cpfHash", ignore = true)
    Cliente toCliente(ClienteCreateDTO clienteCreateDTO);
    ClienteResponseDTO toClienteDto(Cliente cliente);
    List<ClienteResponseDTO> toClienteDtoList(List<Cliente> clienteList);

}
