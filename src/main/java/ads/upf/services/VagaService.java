package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.InvalidEditException;
import ads.upf.model.DTOs.vaga.VagaCreateDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Vaga;
import ads.upf.model.enums.TipoVaga;
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
    protected VagaRepository vagaRepository;

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
                    .orElseThrow( () -> new EntityExistsException("Vaga não encontrada."));

            if (vaga.getStatus() == VagaStatus.ocupada)
                throw new InvalidEditException("Vagas ocupadas não podem ser editadas.");

            vaga.setNome(vagaDTO.getNome());
            vaga.setStatus(vagaDTO.getStatus());
            vaga.setTipoVaga(vagaDTO.getTipoVaga());
        }
    }

    public List<VagaResponseDTO> listarVagas() {
        List<Vaga> vagaList = vagaRepository.listAll();
        return VagaMapper.INSTANCE.toDtoList(vagaList);
    }

    public VagaResponseDTO buscarPeloNome(String nome) {
        if (!nome.isBlank() && !nome.isEmpty()) {
           return vagaRepository.findByNome(nome)
                   .firstResultOptional()
                   .map(VagaMapper.INSTANCE::toDto)
                   .orElse(null);
        }
        return null;
    }

    public int contar() {
        return (int) vagaRepository.count();
    }

    public List<VagaResponseDTO> listarPaginado(int first, int pageSize) {
        List<Vaga> vagas = vagaRepository.findAll()
                .range(first, first + pageSize - 1)
                .list();
        return VagaMapper.INSTANCE.toDtoList(vagas);
    }

    private void checkVagaExists(String nome, Long id) {
        if (vagaRepository.existsByNome(nome, id)) {
            throw new EntityExistsException("Já existe uma vaga com esse nome.");
        }
    }

}
