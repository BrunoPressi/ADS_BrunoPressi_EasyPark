package ads.upf.services;

import ads.upf.model.DTOs.cliente.ClienteCreateDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.mappers.ClienteMapper;
import ads.upf.repositories.ClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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

}
