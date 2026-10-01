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
import ads.upf.model.enums.ParcelaStatus;
import ads.upf.model.mappers.ContratoMapper;
import ads.upf.model.mappers.ParcelaMapper;
import ads.upf.repositories.ClienteRepository;
import ads.upf.repositories.ContratoRepository;
import ads.upf.repositories.ParcelaRepository;
import ads.upf.repositories.VeiculoRepository;
import ads.upf.utils.SecurityUtil;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ContratoService {

    private final ContratoRepository contratoRepository;

    private final ClienteRepository clienteRepository;

    private final ParcelaRepository parcelaRepository;

    private final VeiculoRepository veiculoRepository;

    private final ParcelaService parcelaService;

    @Inject
    public ContratoService(ContratoRepository contratoRepository,
                           ClienteRepository clienteRepository,
                           ParcelaService parcelaService,
                           ParcelaRepository parcelaRepository,
                           VeiculoRepository veiculoRepository) {
        this.contratoRepository = contratoRepository;
        this.clienteRepository = clienteRepository;
        this.parcelaService = parcelaService;
        this.parcelaRepository = parcelaRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public void salvarContrato(ContratoCreateDTO contratoCreateDTO) {

        Cliente cliente = clienteRepository.findByIdOptional(contratoCreateDTO.getCliente().getId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Cliente não encontrado.")
                );

        Veiculo veiculo = veiculoRepository.find("placa = ?1", contratoCreateDTO.getVeiculo().getPlaca())
                .firstResultOptional()
                .orElseThrow(
                        () -> new EntityNotFoundException("Veículo não encontrado.")
                );

        List<Parcela> parcelas = parcelaService.gerarParcelas(
                contratoCreateDTO.getDataInicio(),
                contratoCreateDTO.getDataTermino()
        );

        if (contratoCreateDTO.getContratoId() == null) {
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

            if (contrato.getStatus() != ContratoStatus.ativo) {
                throw new InvalidEditException("Não é permitido editar um contrato que não esteja ativo.");
            }

            // Impede corrupção de parcelas pagas
            boolean possuiParcelaPaga = contrato.getParcelas().stream()
                    .anyMatch(p -> p.getStatus() == ParcelaStatus.paga || p.getDataPagamento() != null);
            if (possuiParcelaPaga) {
                throw new InvalidEditException("Não é possível alterar as datas de um contrato com parcelas já pagas.");
            }

            contrato.setCliente(cliente);
            contrato.setVeiculo(veiculo);
            contrato.setDataInicio(contratoCreateDTO.getDataInicio());
            contrato.setDataTermino(contratoCreateDTO.getDataTermino());

            List<Parcela> parcelasAtuais = new ArrayList<>(contrato.getParcelas());

            parcelasAtuais.forEach(contrato::removerParcela);
            parcelas.forEach(contrato::adicionarParcela);
        }
    }

    @Transactional
    public void cancelarContrato(ContratoResponseDTO contratoResponseDTO) {

        Contrato contrato = contratoRepository.findByIdOptional(contratoResponseDTO.getId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Contrato não encontrado")
                );

        List<Parcela> parcelas = contrato.getParcelas();

        if (contrato.getStatus().equals(ContratoStatus.cancelado) || contrato.getStatus().equals(ContratoStatus.encerrado)) {
           throw new InvalidEditException("Contratos encerrados ou cancelados não podem ser cancelados!");
        }

        boolean possuiParcelaAtrasada = contrato.getParcelas().stream()
                .anyMatch(p -> p.getStatus() == ParcelaStatus.atrasada);
        if (possuiParcelaAtrasada) {
            throw new InvalidEditException("Não é possível cancelar um contrato com parcelas atrasadas!");
        }

       contrato.setStatus(ContratoStatus.cancelado);
    }

    @Transactional
    public ContratoResponseDTO buscarPorCliente(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }

        String cpfNormalizado = cpf.replaceAll("\\D", "");
        if (cpfNormalizado.length() != 11) {
            throw new IllegalArgumentException("CPF inválido para busca.");
        }

        String cpfHash = SecurityUtil.generateBlindIndex(cpfNormalizado);
        return contratoRepository.find("cliente.cpfHash ?1 order by id desc", cpfHash)
                .firstResultOptional()
                .map(ContratoMapper.INSTANCE::toContratoDto)
                .orElse(null);
    }

    public List<ParcelaResponseDTO> buscarParcelasDoContrato(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do contrato inválido.");
        }

        boolean contratoExiste = contratoRepository.findByIdOptional(id).isPresent();
        if (!contratoExiste) {
            throw new EntityNotFoundException("Contrato não encontrado com o ID fornecido.");
        }

        return parcelaRepository.find("contrato.id = ?1 order by numeroParcela asc", id)
                .stream()
                .map(ParcelaMapper.INSTANCE::toParcelaDto)
                .toList();
    }

    public int contar() {
        return (int) contratoRepository.count();
    }

    public List<ContratoResponseDTO> listarPaginado(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Contrato> contratos = contratoRepository.findAll(Sort.by("id"))
                .range(first, first + pageSize - 1)
                .list();
        return ContratoMapper.INSTANCE.toContratoDtoList(contratos);
    }
}
