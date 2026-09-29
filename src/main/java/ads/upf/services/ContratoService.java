package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.DTOs.parcela.ParcelaResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.mappers.ContratoMapper;
import ads.upf.model.mappers.ParcelaMapper;
import ads.upf.repositories.ClienteRepository;
import ads.upf.repositories.ContratoRepository;
import ads.upf.repositories.ParcelaRepository;
import ads.upf.utils.SecurityUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ContratoService {

    @Inject
    protected ContratoRepository contratoRepository;
    @Inject
    protected ClienteRepository clienteRepository;
    @Inject
    protected ParcelaService parcelaService;
    @Inject
    protected ParcelaRepository parcelaRepository;

    @Transactional
    public void salvarContrato(ContratoCreateDTO contratoCreateDTO) {
        Cliente cliente = clienteRepository.findByIdOptional(contratoCreateDTO.getClienteId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Cliente não encontrado.")
                );
        List<Parcela> parcelas = parcelaService.gerarParcelas(
                contratoCreateDTO.getDataInicio(),
                contratoCreateDTO.getDataTermino()
        );
        if (contratoCreateDTO.getContratoId() == null) {
            Contrato contrato = ContratoMapper.INSTANCE.toContrato(contratoCreateDTO);
            contrato.setCliente(cliente);
            parcelas.forEach(parcela -> contrato.adicionarParcela(parcela));
            contratoRepository.persist(contrato);
        }
        else {
            Contrato contrato = contratoRepository.findByIdOptional(contratoCreateDTO.getContratoId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Contrato não encontrado.")
                    );
            contrato.setDataInicio(contratoCreateDTO.getDataInicio());
            contrato.setDataTermino(contratoCreateDTO.getDataTermino());
            parcelas.forEach(parcela -> contrato.adicionarParcela(parcela));
        }
    }

    public ContratoResponseDTO buscarPorCliente(String cpf) {
        cpf = SecurityUtil.generateBlindIndex(cpf);
        return contratoRepository.buscarPorCliente(cpf)
                .firstResultOptional()
                .map(ContratoMapper.INSTANCE::toContratoDto)
                .orElse(null);
    }

    public List<ParcelaResponseDTO> buscarParcelasDoContrato(Long id) {
        return parcelaRepository.buscarPorContrato(id)
                .stream()
                .map(ParcelaMapper.INSTANCE::toParcelaDto)
                .toList();
    }

    public int contar() {
        return (int) contratoRepository.count();
    }

    public List<ContratoResponseDTO> listarPaginado(int first, int pageSize) {
        List<Contrato> contratos = contratoRepository.findAll()
                .range(first, first + pageSize - 1)
                .list();
        return ContratoMapper.INSTANCE.toContratoDtoList(contratos);
    }
}
