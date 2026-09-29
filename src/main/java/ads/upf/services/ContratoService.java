package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.contrato.ContratoCreateDTO;
import ads.upf.model.DTOs.contrato.ContratoResponseDTO;
import ads.upf.model.DTOs.parcela.ParcelaResponseDTO;
import ads.upf.model.entities.Cliente;
import ads.upf.model.entities.Contrato;
import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.StatusParcela;
import ads.upf.model.mappers.ContratoMapper;
import ads.upf.model.mappers.ParcelaMapper;
import ads.upf.repositories.ClienteRepository;
import ads.upf.repositories.ContratoRepository;
import ads.upf.repositories.ParcelaRepository;
import ads.upf.utils.SecurityUtil;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ContratoService {

    private final ContratoRepository contratoRepository;

    private final ClienteRepository clienteRepository;

    private final ParcelaService parcelaService;

    private final ParcelaRepository parcelaRepository;

    @Inject
    public ContratoService(ContratoRepository contratoRepository,
                           ClienteRepository clienteRepository,
                           ParcelaService parcelaService,
                           ParcelaRepository parcelaRepository) {
        this.contratoRepository = contratoRepository;
        this.clienteRepository = clienteRepository;
        this.parcelaService = parcelaService;
        this.parcelaRepository = parcelaRepository;
    }

    @Transactional
    public void salvarContrato(ContratoCreateDTO contratoCreateDTO) {
        Objects.requireNonNull(contratoCreateDTO, "Os dados do contrato não podem ser nulos");
        Objects.requireNonNull(contratoCreateDTO.getClienteId(), "O ID do cliente não pode ser nulo");
        Objects.requireNonNull(contratoCreateDTO.getDataInicio(), "A data de início não pode ser nula");
        Objects.requireNonNull(contratoCreateDTO.getDataTermino(), "A data de término não pode ser nula");

        if (contratoCreateDTO.getDataTermino().isBefore(contratoCreateDTO.getDataInicio().plusMonths(1))) {
            throw new IllegalArgumentException("O contrato deve ter a duração mínima de 1 mês.");
        }

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

            if (contrato.getStatus() != ContratoStatus.ativo) {
                throw new InvalidEditException("Não é permitido editar um contrato que não esteja ativo.");
            }

            // Impede corrupção de parcelas pagas
            boolean possuiParcelaPaga = contrato.getParcelas().stream()
                    .anyMatch(p -> p.getStatus() == StatusParcela.paga || p.getDataPagamento() != null);
            if (possuiParcelaPaga) {
                throw new InvalidEditException("Não é possível alterar as datas de um contrato com parcelas já pagas.");
            }

            contrato.setCliente(cliente);
            contrato.setDataInicio(contratoCreateDTO.getDataInicio());
            contrato.setDataTermino(contratoCreateDTO.getDataTermino());

            List<Parcela> parcelasAtuais = new ArrayList<>(contrato.getParcelas());
            parcelasAtuais.forEach(contrato::removerParcela);

            parcelas.forEach(contrato::adicionarParcela);
        }
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
