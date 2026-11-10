package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.tarifa.TarifaCreateDTO;
import ads.upf.model.DTOs.tarifa.TarifaResponseDTO;
import ads.upf.model.entities.Tarifa;
import ads.upf.model.mappers.TarifaMapper;
import ads.upf.repositories.TarifaRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    @Inject
    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional
    public void criarNovaTarifa(TarifaCreateDTO tarifaCreateDTO) {
        tarifaCreateDTO.calcularValorFracaoHora();

        if (tarifaCreateDTO.getId() == null) {
            if (tarifaRepository.existeTarifaAtiva()) {
                throw new EntityExistsException("Já existe uma tarifa ativa. Desative a tarifa atual antes de criar uma nova.");
            }
            Tarifa tarifa = TarifaMapper.INSTANCE.toTarifa(tarifaCreateDTO);
            tarifa.setAtivo(true);
            tarifaRepository.persist(tarifa);
        }
        else {
            Tarifa tarifa = tarifaRepository.findByIdOptional(tarifaCreateDTO.getId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Tarifa não encontrada.")
                    );
            tarifa.editar();
            TarifaMapper.INSTANCE.toUpdateFromTarifaDto(tarifaCreateDTO, tarifa);
        }
    }

    @Transactional
    public void desativarTarifa(Long id) {
        Tarifa tarifa = tarifaRepository.findByIdOptional(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Tarifa não encontrada.")
                );

        tarifa.desativar();
    }

    public Tarifa buscarTarifaAtiva() {
        return tarifaRepository.buscarTarifaAtiva().firstResultOptional()
                .orElseThrow(
                        () -> new EntityNotFoundException("Nenhuma tarifa ativa encontrada.")
                );
    }

    public List<TarifaResponseDTO> buscarTodasTarifas() {
        return tarifaRepository.listAll(Sort.by("id"))
                .stream()
                .map(TarifaMapper.INSTANCE::toTarifaDto)
                .toList();
    }
}
