package ads.upf.services;

import ads.upf.model.DTOs.permanencia.EntradaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.Permanencia;
import ads.upf.model.entities.Vaga;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.model.mappers.PermanenciaMapper;
import ads.upf.repositories.PermanenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class EntradaUseCase {

    private final PermanenciaRepository permanenciaRepository;
    private final VeiculoService veiculoService;
    private final VagaService vagaService;
    private final FuncionarioService funcionarioService;

    @Inject
    public EntradaUseCase(PermanenciaRepository permanenciaRepository,
                          VeiculoService veiculoService,
                          VagaService vagaService,
                          FuncionarioService funcionarioService) {
        this.permanenciaRepository = permanenciaRepository;
        this.veiculoService = veiculoService;
        this.vagaService = vagaService;
        this.funcionarioService = funcionarioService;
    }

    @Transactional
    public PermanenciaResponseDTO novaEntrada(EntradaCreateDTO permanenciaCreateDTO) {
        Boolean veiculoJaEstacionado = verificarVeiculoEstacionado(permanenciaCreateDTO.getPlaca());
        if (veiculoJaEstacionado)
            throw new IllegalArgumentException("Esse veículo já está com uma entrada em andamento!");

        Funcionario funcionario = funcionarioService
                .definirFuncionario(permanenciaCreateDTO.getFuncionarioEmail());
        Veiculo veiculo = veiculoService
                .obterOuCriar(permanenciaCreateDTO.getPlaca(), permanenciaCreateDTO.getTipoVeiculo());
        Vaga vaga = vagaService
                .ocuparVaga(permanenciaCreateDTO.getTipoVaga());

        Permanencia permanencia = new Permanencia();
        permanencia.setDataEntrada(LocalDateTime.now());
        permanencia.setStatus(PermanenciaStatus.em_andamento);
        permanencia.setFuncionario(funcionario);
        permanencia.setVeiculo(veiculo);
        permanencia.setVaga(vaga);
        permanenciaRepository.persist(permanencia);

        return PermanenciaMapper.INSTANCE.toPermanenciaDto(permanencia);
    }

    private Boolean verificarVeiculoEstacionado(String placa) {
        return permanenciaRepository.verificarVeiculoEstacionado(placa).isPresent();
    }

}
