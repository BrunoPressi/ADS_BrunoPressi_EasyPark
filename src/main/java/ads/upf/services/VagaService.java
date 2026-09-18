package ads.upf.services;

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

import java.util.List;

@ApplicationScoped
public class VagaService {

    @Inject
    VagaRepository vagaRepository;

    @Transactional
    public void criarNovaVaga(VagaCreateDTO vagaDTO) {
        Vaga vaga = VagaMapper.INSTANCE.toVaga(vagaDTO);
        vagaRepository.persist(vaga);
    }

    @Transactional
    public void editarVaga(Long id, VagaEditDTO vagaDTO) {
        Vaga vagaExistente = vagaRepository.find("id != ?1 and nome = ?2", id, vagaDTO.getNome()).firstResult();

        if (vagaExistente != null) {
            throw new IllegalArgumentException("Já existe uma vaga com esse nome");
        }

        Vaga vaga = vagaRepository.findById(id);

        if (vaga.getStatus().equals(VagaStatus.ocupada)) {
            throw new IllegalArgumentException("Vagas ocupadas não podem ser alteradas.");
        }

        vaga.setNome(vagaDTO.getNome());
        vaga.setStatus(vagaDTO.getStatus());
        vagaRepository.persist(vaga);
    }

    @Transactional
    public List<VagaResponseDTO> listarVagas() {
        List<Vaga> vagaList = vagaRepository.listAll();
        return VagaMapper.INSTANCE.toDtoList(vagaList);
    }

}
