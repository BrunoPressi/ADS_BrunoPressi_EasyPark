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
    private final ContratoService contratoService;

    @Inject
    public EntradaUseCase(PermanenciaRepository permanenciaRepository,
                          VeiculoService veiculoService,
                          VagaService vagaService,
                          FuncionarioService funcionarioService,
                          ContratoService contratoService) {
        this.permanenciaRepository = permanenciaRepository;
        this.veiculoService = veiculoService;
        this.vagaService = vagaService;
        this.contratoService = contratoService;
    }

    @Transactional
    public PermanenciaResponseDTO novaEntrada(EntradaCreateDTO entrada) {
        Boolean veiculoJaEstacionado = verificarVeiculoEstacionado(entrada.getPlaca());
        if (veiculoJaEstacionado)
            throw new IllegalArgumentException("Esse veículo já está com uma entrada em andamento!");

        Veiculo veiculo = veiculoService
                .obterOuCriar(entrada.getPlaca(), entrada.getTipoVeiculo());
        Vaga vaga = vagaService
                .ocuparVaga(entrada.getTipoVaga());

        Permanencia permanencia = new Permanencia();
        permanencia.setIsRotativo(!contratoService.verificarPossuiContratoAtivo(entrada.getPlaca()));
        permanencia.setDataEntrada(LocalDateTime.now());
        permanencia.setStatus(PermanenciaStatus.em_andamento);
        permanencia.setVeiculo(veiculo);
        permanencia.setVaga(vaga);
        permanenciaRepository.persist(permanencia);

        return PermanenciaMapper.INSTANCE.toPermanenciaDto(permanencia);
    }

    private Boolean verificarVeiculoEstacionado(String placa) {
        return permanenciaRepository.verificarVeiculoEstacionado(placa).isPresent();
    }

}
