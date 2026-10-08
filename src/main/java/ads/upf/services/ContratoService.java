package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.DTOs.parcela.ParcelaResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.mappers.ContratoMapper;
import ads.upf.model.mappers.ParcelaMapper;
import ads.upf.repositories.ContratoRepository;
import ads.upf.utils.SecurityUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ParcelaService parcelaService;
    private final VeiculoService veiculoService;
    private final ClienteService clienteService;

    @Inject
    public ContratoService(ContratoRepository contratoRepository,
                           ParcelaService parcelaService,
                           VeiculoService veiculoService,
                           ClienteService clienteService) {
        this.contratoRepository = contratoRepository;
        this.parcelaService = parcelaService;
        this.veiculoService = veiculoService;
        this.clienteService = clienteService;
    }

    @Transactional
    public void salvarContrato(ContratoCreateDTO contratoCreateDTO) {

        String placa = contratoCreateDTO.getVeiculo().getPlaca();
        String cpf = contratoCreateDTO.getCliente().getCpfNormal();

        Cliente cliente = clienteService.verificarClienteExiste(cpf);
        Veiculo veiculo = veiculoService.verificarVeiculoExiste(placa);

        List<Parcela> parcelas = parcelaService.gerarParcelas(
                contratoCreateDTO.getDataInicio(),
                contratoCreateDTO.getDataTermino()
        );

        if (contratoCreateDTO.getContratoId() == null) {
            if (verificarPossuiContratoAtivo(placa))
                throw new IllegalArgumentException("Esse veículo já pertence a outro contrato ativo");

            Contrato contrato = ContratoMapper.INSTANCE.toContrato(contratoCreateDTO);
            contrato.setCliente(cliente);
            contrato.setVeiculo(veiculo);
            parcelas.forEach(parcela -> contrato.adicionarParcela(parcela));
            contratoRepository.persist(contrato);
        }
        else {
            Contrato contrato = contratoRepository.findByIdOptional(contratoCreateDTO.getContratoId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Contrato não encontrado.")
                    );

            if (!verificarSeContratoAtivo(contratoCreateDTO.getContratoId()))
                throw new InvalidEditException("Contratos cancelados ou encerrados não podem ser editados");

            contrato.setCliente(cliente);
            contrato.setVeiculo(veiculo);
            contrato.atualizarVigenciaEParcelas(
                    contratoCreateDTO.getDataInicio(),
                    contratoCreateDTO.getDataTermino(),
                    parcelas);
        }
    }

    @Transactional
    public void cancelarContrato(Long id) {
        Contrato contrato = verificarContratoExiste(id);
        contrato.cancelar();
    }

    public ContratoResponseDTO buscarPorCliente(String cpf) {
        String cpfHash = SecurityUtil.generateBlindIndex(cpf);
        Contrato contrato = contratoRepository.buscarPeloCpf(cpfHash).orElse(null);
        return ContratoMapper.INSTANCE.toContratoDto(contrato);
    }

    public List<ParcelaResponseDTO> buscarParcelasDoContrato(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do contrato inválido.");
        }

        Contrato contrato = verificarContratoExiste(id);
        return ParcelaMapper.INSTANCE.toParcelasDtoList(contrato.getParcelas());
    }

    public int contar() {
        return (int) contratoRepository.count();
    }

    public List<ContratoResponseDTO> listarPaginado(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Contrato> contratos = contratoRepository.buscarTodosPaginado(first, pageSize);
        return ContratoMapper.INSTANCE.toContratoDtoList(contratos);
    }

    public Boolean verificarPossuiContratoAtivo(String placa) {
        return contratoRepository.verificarPossuiContratoAtivo(placa);
    }

    private Contrato verificarContratoExiste(Long id) {
        return contratoRepository.findByIdOptional(id).orElseThrow(
                () -> new EntityNotFoundException("Contrato não encontrado")
        );
    }

    private Boolean verificarSeContratoAtivo(Long id) {
        return contratoRepository.findByIdOptional(id)
                .stream()
                .filter((c) -> c.getStatus().equals(ContratoStatus.ativo)).count() > 0;
    }

}
