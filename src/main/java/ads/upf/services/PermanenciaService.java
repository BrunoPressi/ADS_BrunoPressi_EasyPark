package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.permanencia.PermanenciaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.entities.Permanencia;
import ads.upf.model.entities.Vaga;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.model.enums.VagaStatus;
import ads.upf.model.enums.VagaTipo;
import ads.upf.model.mappers.PermanenciaMapper;
import ads.upf.model.mappers.VeiculoMapper;
import ads.upf.repositories.PermanenciaRepository;
import ads.upf.repositories.VagaRepository;
import ads.upf.repositories.VeiculoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class PermanenciaService {

    private final PermanenciaRepository permanenciaRepository;
    private final VagaRepository vagaRepository;
    private final VeiculoRepository veiculoRepository;
    private final VeiculoService veiculoService;

    @Inject
    public PermanenciaService(PermanenciaRepository permanenciaRepository,
                              VagaRepository vagaRepository,
                              VeiculoService veiculoService,
                              VeiculoRepository veiculoRepository) {
        this.permanenciaRepository = permanenciaRepository;
        this.vagaRepository = vagaRepository;
        this.veiculoService = veiculoService;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public void novaEntrada(PermanenciaCreateDTO permanenciaCreateDTO) {
        Permanencia permanencia = new Permanencia();
        Veiculo veiculo = buscarVeiculo(permanenciaCreateDTO.getPlaca());

        if (veiculo == null) {
            veiculo = new Veiculo();
            veiculo.setPlaca(permanenciaCreateDTO.getPlaca());
            veiculo.setTipo(permanenciaCreateDTO.getTipoVeiculo());
            veiculoRepository.persist(veiculo);
        }

        Boolean veiculoJaEstacionado = checkVeiculoEstacionado(veiculo.getPlaca());
        if (veiculoJaEstacionado) throw new IllegalArgumentException("Esse veículo já está com uma entrada em andamento!");

        Vaga vaga = definirVaga(permanenciaCreateDTO.getTipoVaga());
        vaga.setStatus(VagaStatus.ocupada);

        permanencia.setDataEntrada(LocalDateTime.now());
        permanencia.setStatus(PermanenciaStatus.em_andamento);
        permanencia.setVeiculo(veiculo);
        permanencia.setVaga(vaga);
        permanenciaRepository.persist(permanencia);
    }

    private Veiculo buscarVeiculo(String placa) {
        VeiculoResponseDTO veiculoResponseDTO = veiculoService.buscarVeiculoPelaPlaca(placa);
        return VeiculoMapper.INSTANCE.toVeiculo(veiculoResponseDTO);
    }

    private Vaga definirVaga(VagaTipo vagaTipo) {
        return vagaRepository.find("status = 'disponivel' and tipoVaga = ?1", vagaTipo)
                .firstResultOptional()
                .orElseThrow(
                        () -> new EntityNotFoundException("Nenhuma vaga disponível encontrada")
                );
    }

    private Boolean checkVeiculoEstacionado(String placa) {
        return permanenciaRepository.find("veiculo.placa = ?1 and status = em_andamento", placa)
                .firstResultOptional()
                .isPresent();
    }

    public int contar() {
        return (int) permanenciaRepository.count();
    }

    public List<PermanenciaResponseDTO> listarPaginado(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Permanencia> permanencias = permanenciaRepository.findAll(Sort.by("dataEntrada"))
                .range(first, first + pageSize - 1)
                .list();

        return PermanenciaMapper.INSTANCE.toPermanenciaDtoList(permanencias);
    }

}
