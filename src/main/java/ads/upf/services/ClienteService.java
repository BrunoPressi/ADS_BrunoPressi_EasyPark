package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.DTOs.cliente.ClienteResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Usuario;
import ads.upf.model.mappers.ClienteMapper;
import ads.upf.repositories.ClienteRepository;
import ads.upf.repositories.UsuarioRepository;
import ads.upf.utils.SecurityUtil;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Inject
    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void salvarCliente(ClienteCreateDTO clienteCreateDTO) {

        if (clienteCreateDTO.getId() == null) {
            checkClienteExists(
                    null,
                    clienteCreateDTO.getCpf(),
                    clienteCreateDTO.getEmail(),
                    clienteCreateDTO.getNomeCompleto()
            );
            Cliente cliente = ClienteMapper.INSTANCE.toCliente(clienteCreateDTO);
            clienteRepository.persist(cliente);
        }
        else {
            checkClienteExists(
                    clienteCreateDTO.getId(),
                    clienteCreateDTO.getCpf(),
                    clienteCreateDTO.getEmail(),
                    clienteCreateDTO.getNomeCompleto()
            );

            Cliente cliente = clienteRepository
                    .findByIdOptional(clienteCreateDTO.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));

            ClienteMapper.INSTANCE.updateClienteFromDto(clienteCreateDTO, cliente);
        }
    }

    public ClienteResponseDTO buscarPorChaveUnica(String termo) {
        if (termo == null || termo.isBlank()) {
            return null;
        }
        String termoTratado = termo.trim();

        // 1. Busca por E-mail (Chave Única textual)
        if (termoTratado.contains("@")) {
            return clienteRepository.find("email = ?1", termoTratado.toLowerCase())
                    .firstResultOptional()
                    .map(ClienteMapper.INSTANCE::toClienteDto)
                    .orElse(null);
        }

        String digitos = termoTratado.replaceAll("\\D", "");

        // 2. Se possuir exatamente 11 dígitos, busca por CPF via Blind Index
        if (digitos.length() == 11) {
            String cpfHash = SecurityUtil.generateBlindIndex(digitos);
            return clienteRepository.find("cpfHash = ?1", cpfHash)
                    .firstResultOptional()
                    .map(ClienteMapper.INSTANCE::toClienteDto)
                    .orElse(null);
        }

        // 3. Se for puramente numérico e de tamanho compatível com ID (1 a 18 dígitos)
        if (termoTratado.matches("^\\d{1,18}$")) {
            try {
                Long id = Long.valueOf(termoTratado);
                return clienteRepository.findByIdOptional(id)
                        .map(ClienteMapper.INSTANCE::toClienteDto)
                        .orElse(null);
            } catch (NumberFormatException ignored) {
                // Proteção defensiva adicional
            }
        }

        return null;
    }

    public int contar() {
        return (int) clienteRepository.count();
    }

    public List<ClienteResponseDTO> listarPaginado(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Cliente> clientes = clienteRepository.findAll(Sort.by("nomeCompleto"))
                .range(first, first + pageSize - 1)
                .list();

        return ClienteMapper.INSTANCE.toClienteDtoList(clientes);
    }

    private void checkClienteExists(Long id, String cpf, String email, String nomeCompleto) {
        if (clienteRepository.checkCpf(SecurityUtil.generateBlindIndex(cpf), id)) {
            throw new EntityExistsException("Esse CPF já está cadastrado");
        }
        if (usuarioRepository.checkEmail(email, id)) {
            throw new EntityExistsException("Esse Email já está cadastrado");
        }
        if (usuarioRepository.checkNomeCompleto(nomeCompleto, id)) {
            throw new EntityExistsException("Já existe um usuário com este nome.");
        }
    }

}
