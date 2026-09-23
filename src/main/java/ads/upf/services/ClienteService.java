package ads.upf.services;

import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.mappers.ClienteMapper;
import ads.upf.repositories.ClienteRepository;
import ads.upf.utils.SecurityUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ClienteService {

    @Inject
    protected ClienteRepository clienteRepository;

    @Transactional
    public void salvarCliente(ClienteCreateDTO clienteCreateDTO) {
        try {
            Cliente cliente = ClienteMapper.INSTANCE.toCliente(clienteCreateDTO);
            clienteRepository.persist(cliente);
        }
        catch (Exception e) {
            throw new RuntimeException("Erro ao salvar cliente " + e.getMessage());
        }
    }

    public ClienteResponseDTO buscarPorChaveUnica(String termo) {
        if (termo == null || termo.isBlank()) return null;

        String clean = termo.replaceAll("\\D", "");

        // Se tem 11 dígitos, é CPF
        if (clean.length() == 11) {
            String hash = SecurityUtil.generateBlindIndex(clean);
            return clienteRepository.find("cpfHash", hash)
                    .firstResultOptional()
                    .map(ClienteMapper.INSTANCE::toClienteDto)
                    .orElse(null);
        }

        // Se for numérico menor, pode ser ID
        if (clean.matches("\\d+")) {
            return clienteRepository.findByIdOptional(Long.parseLong(clean))
                    .map(ClienteMapper.INSTANCE::toClienteDto)
                    .orElse(null);
        }

        return null;
    }

    public int contar() {
        return (int) clienteRepository.count();
    }

    public List<ClienteResponseDTO> listarPaginado(int first, int pageSize) {
        List<Cliente> clientes = clienteRepository.findAll()
                .range(first, first + pageSize - 1)
                .list();
        return ClienteMapper.INSTANCE.toClienteDtoList(clientes);
    }

}
