package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.vaga.VagaFormDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import ads.upf.model.mappers.VagaMapper;
import ads.upf.repositories.VagaRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class VagaService {

    private final VagaRepository vagaRepository;

    @Inject
    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    @Transactional
    public void salvarVaga(VagaFormDTO vagaDTO) {

        if (vagaDTO.getId() == null) {
            checkVagaExists(vagaDTO.getNome(), null);
            Vaga vaga = VagaMapper.INSTANCE.toVaga(vagaDTO);
            vagaRepository.persist(vaga);
        }
        else {
            checkVagaExists(vagaDTO.getNome(), vagaDTO.getId());
            Vaga vaga = vagaRepository.findByIdOptional(vagaDTO.getId())
                    .orElseThrow( () -> new EntityNotFoundException("Vaga não encontrada."));

            vaga.editar();
            VagaMapper.INSTANCE.toUpdateFromVagaDto(vagaDTO, vaga);
        }
    }

    public List<VagaResponseDTO> listarVagas() {
        return vagaRepository.listAll(Sort.by("nome"))
                .stream()
                .map(VagaMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    public VagaResponseDTO buscarPeloNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        Vaga vaga = vagaRepository.findByNome(nome.trim()).orElse(null);
        return VagaMapper.INSTANCE.toDto(vaga);
    }

    public int contar() {
        return (int) vagaRepository.count();
    }

    public List<VagaResponseDTO> listarPaginado(int first, int pageSize) {
        //  se pageSize <= 0 ou first < 0, a consulta estoura erro em tempo de execução.
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        return vagaRepository.listarPaginado(first, pageSize)
                .stream()
                .map((v) -> VagaMapper.INSTANCE.toDto(v))
                .collect(Collectors.toList());
    }

    private void checkVagaExists(String nome, Long id) {
        if (nome != null && vagaRepository.existsByNome(nome, id)) {
            throw new EntityExistsException("Já existe uma vaga com esse nome.");
        }
    }

    @Transactional
    public Vaga ocuparVaga(VagaTipo vagaTipo) {
        Vaga vaga = vagaRepository.findByStatusAndTipo(vagaTipo)
                .orElseThrow(
                        () -> new EntityNotFoundException("Vaga não encontrada")
                );
        vaga.setStatus(VagaStatus.ocupada);
        return vaga;
    }

    @Transactional
    public void liberarVaga(long id) {
        Vaga vaga = vagaRepository.findByIdOptional(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Vaga não encontrada")
                        );
        vaga.setStatus(VagaStatus.disponivel);
    }
}
