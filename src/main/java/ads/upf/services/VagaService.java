package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.mappers.VagaMapper;
import ads.upf.repositories.VagaRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class VagaService {

    private final VagaRepository vagaRepository;

    @Inject
    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    @Transactional
    public void salvarVaga(VagaCreateDTO vagaDTO) {

        if (vagaDTO.getId() == null) {
            checkVagaExists(vagaDTO.getNome(), null);
            Vaga vaga = VagaMapper.INSTANCE.toVaga(vagaDTO);
            vagaRepository.persist(vaga);
        }
        else {
            checkVagaExists(vagaDTO.getNome(), vagaDTO.getId());
            Vaga vaga = vagaRepository.findByIdOptional(vagaDTO.getId())
                    .orElseThrow( () -> new EntityNotFoundException("Vaga não encontrada."));

            if (vaga.getStatus() == VagaStatus.ocupada)
                throw new InvalidEditException("Vagas ocupadas não podem ser alteradas.");

            VagaMapper.INSTANCE.toUpdateFromVagaDto(vagaDTO, vaga);
        }
    }

    public List<VagaResponseDTO> listarVagas() {
        List<Vaga> vagaList = vagaRepository.listAll(Sort.by("nome"));
        return VagaMapper.INSTANCE.toDtoList(vagaList);
    }

    public VagaResponseDTO buscarPeloNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return vagaRepository.findByNome(nome.trim())
                .firstResultOptional()
                .map(VagaMapper.INSTANCE::toDto)
                .orElse(null);
    }

    public int contar() {
        return (int) vagaRepository.count();
    }

    public List<VagaResponseDTO> listarPaginado(int first, int pageSize) {
        //  se pageSize <= 0 ou first < 0, a consulta estoura erro em tempo de execução.
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Vaga> vagas = vagaRepository.findAll(Sort.by("nome"))
                .range(first, first + pageSize - 1)
                .list();
        return VagaMapper.INSTANCE.toDtoList(vagas);
    }

    private void checkVagaExists(String nome, Long id) {
        if (nome != null && vagaRepository.existsByNome(nome, id)) {
            throw new EntityExistsException("Já existe uma vaga com esse nome.");
        }
    }

}
