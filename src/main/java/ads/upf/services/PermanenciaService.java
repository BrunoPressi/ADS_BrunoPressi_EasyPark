package ads.upf.services;

import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Permanencia;
import ads.upf.model.mappers.PermanenciaMapper;
import ads.upf.repositories.PermanenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class PermanenciaService {

    private final PermanenciaRepository permanenciaRepository;

    @Inject
    public PermanenciaService(PermanenciaRepository permanenciaRepository) {
        this.permanenciaRepository = permanenciaRepository;
    }

    public int contar() {
        return (int) permanenciaRepository.count();
    }

    public List<PermanenciaResponseDTO> listarPaginado(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Permanencia> permanencias = permanenciaRepository.listarPaginado(first, pageSize);

        return PermanenciaMapper.INSTANCE.toPermanenciaDtoList(permanencias);
    }

}
