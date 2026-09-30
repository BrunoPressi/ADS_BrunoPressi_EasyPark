package ads.upf.model.mappers;

import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.entities.Cliente;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ClienteMapper {

    ClienteMapper INSTANCE = Mappers.getMapper( ClienteMapper.class );

    Cliente toCliente(ClienteCreateDTO clienteCreateDTO);
    void updateClienteFromDto(ClienteCreateDTO clienteCreateDTO, @MappingTarget Cliente cliente);
    ClienteResponseDTO toClienteDto(Cliente cliente);
    List<ClienteResponseDTO> toClienteDtoList(List<Cliente> clienteList);

    @AfterMapping
    default void normalizarDados(@MappingTarget Cliente cliente) {
        if (cliente.getNomeCompleto() != null) {
            cliente.setNomeCompleto(cliente.getNomeCompleto().trim());
        }
        if (cliente.getEmail() != null) {
            cliente.setEmail(cliente.getEmail().trim().toLowerCase());
        }
        if (cliente.getTelefone() != null) {
            cliente.setTelefone(cliente.getTelefone().trim());
        }
        if (cliente.getCpf() != null) {
            cliente.setCpf(cliente.getCpf().replaceAll("\\D", ""));
        }
    }

}
