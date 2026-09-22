package ads.upf.services;

import ads.upf.exceptions.VagaExistsException;
import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaEditDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.mappers.VagaMapper;
import ads.upf.repositories.VagaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;

@ApplicationScoped
public class VagaService {

    @Inject
    protected VagaRepository vagaRepository;

    @Transactional
    public void criarNovaVaga(VagaCreateDTO vagaDTO) {
        try {
            checkVagaExists(vagaDTO.getNome(), null);
            Vaga vaga = VagaMapper.INSTANCE.toVaga(vagaDTO);
            vagaRepository.persist(vaga);
        }
        catch (ConstraintViolationException e) {
            throw new RuntimeException("Essa vaga já está cadastrada");
        }
    }

    @Transactional
    public void editarVaga(Long id, VagaEditDTO vagaDTO) {
        try {
            checkVagaExists(vagaDTO.getNome(), id);
            Vaga vaga = vagaRepository.findById(id);

            if (vaga == null) {
                throw new IllegalArgumentException("Vaga não encontrada");
            }

            if (vaga.getStatus().equals(VagaStatus.ocupada)) {
                throw new IllegalArgumentException("Vagas ocupadas não podem ser alteradas.");
            }

            vaga.setNome(vagaDTO.getNome());
            vaga.setStatus(vagaDTO.getStatus());
            vagaRepository.persist(vaga);
        }
        catch (ConstraintViolationException e) {
            throw new VagaExistsException("Já existe uma vaga com esse nome");
        }
    }

    @Transactional
    public List<VagaResponseDTO> listarVagas() {
        List<Vaga> vagaList = vagaRepository.listAll();
        return VagaMapper.INSTANCE.toDtoList(vagaList);
    }

    private void checkVagaExists(String nome, Long id) {
        if (vagaRepository.existsByNome(nome, id)) {
            throw new VagaExistsException("Já existe uma vaga com esse nome.");
        }
    }

}
