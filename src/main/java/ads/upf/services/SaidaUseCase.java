package ads.upf.services;

import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.DTOs.permanencia.SaidaCreateDTO;
import ads.upf.model.entities.Pagamento;
import ads.upf.model.entities.Permanencia;
import ads.upf.model.enums.PagamentoStatus;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.model.mappers.PermanenciaMapper;
import ads.upf.repositories.PermanenciaRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class SaidaUseCase {

    private final PermanenciaRepository permanenciaRepository;
    private final VagaService vagaService;
    private final ContratoService contratoService;
    private final PagamentoService pagamentoService;

    @Inject
    public SaidaUseCase(PermanenciaRepository permanenciaRepository,
                        PagamentoService pagamentoService,
                        VagaService vagaService,
                        ContratoService contratoService) {
        this.permanenciaRepository = permanenciaRepository;
        this.pagamentoService = pagamentoService;
        this.contratoService = contratoService;
        this.vagaService = vagaService;
    }

    @Transactional
    public PermanenciaResponseDTO novaSaida(SaidaCreateDTO saida) {
        Permanencia permanencia = permanenciaRepository.verificarVeiculoEstacionado(saida.getPlaca())
                .orElseThrow(
                        () -> new EntityNotFoundException("Permanência não encontrada.")
                );

        // Só gera pagamento quando for rotativo
        if (saida.getValor() != null || saida.getMeioPagamento() != null) {
            Pagamento pagamento = pagamentoService.gerarPagamento(saida.getValor(), saida.getMeioPagamento());
            permanencia.setPagamento(pagamento);
            pagamento.setPermanencia(permanencia);
        }

        permanencia.setDataSaida(LocalDateTime.now());
        permanencia.setStatus(PermanenciaStatus.concluida);
        vagaService.liberarVaga(permanencia.getVaga().getId());
        return PermanenciaMapper.INSTANCE.toPermanenciaDto(permanencia);
    }
}
