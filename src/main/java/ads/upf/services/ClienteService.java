package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
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
        if (clienteCreateDTO.getId() == null) {
            checkClienteExists(null, clienteCreateDTO.getCpf(), clienteCreateDTO.getEmail());

            Cliente cliente = ClienteMapper.INSTANCE.toCliente(clienteCreateDTO);
            clienteRepository.persist(cliente);
        }
        else {
            checkClienteExists(clienteCreateDTO.getId(), clienteCreateDTO.getCpf(), clienteCreateDTO.getEmail());

            Cliente cliente = clienteRepository
                    .findByIdOptional(clienteCreateDTO.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));

            cliente.setNomeCompleto(clienteCreateDTO.getNomeCompleto());
            cliente.setCpf(clienteCreateDTO.getCpf());
            cliente.setEmail(clienteCreateDTO.getEmail());
            cliente.setTelefone(clienteCreateDTO.getTelefone());
        }
    }

    public ClienteResponseDTO buscarPorChaveUnica(String termo) {
        if (termo == null || termo.isBlank()) return null;

        String clean = termo.replaceAll("\\D", "");

        // Se tem 11 dígitos, é CPF
        if (clean.length() == 11) {
            String cpfHash = SecurityUtil.generateBlindIndex(clean);
            return clienteRepository.findByCpf(cpfHash)
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

    private void checkClienteExists(Long id, String cpf, String email) {
        if (clienteRepository.checkCpf(SecurityUtil.generateBlindIndex(cpf), id)) {
            throw new EntityExistsException("Esse CPF já está cadastrado");
        }
        if (clienteRepository.checkEmail(email, id)) {
            throw new EntityExistsException("Esse Email já está cadastrado");
        }
    }

}
