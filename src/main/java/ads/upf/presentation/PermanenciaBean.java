package ads.upf.presentation;

import ads.upf.model.DTOs.pagamento.PagamentoResponseDTO;
import ads.upf.model.DTOs.permanencia.EntradaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.DTOs.permanencia.SaidaCreateDTO;
import ads.upf.model.mappers.PagamentoMapper;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.EntradaUseCase;
import ads.upf.services.PagamentoService;
import ads.upf.services.PermanenciaService;
import ads.upf.services.SaidaUseCase;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.faces.application.FacesMessage.SEVERITY_ERROR;

@ViewScoped
@Named("permanenciaBean")
@Getter @Setter
public class PermanenciaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EntradaUseCase entradaService;

    @Inject
    private SaidaUseCase saidaService;

    @Inject
    private PermanenciaService permanenciaService;

    @Inject
    private PagamentoService pagamentoService;

    private GenericLazyDataModel<PermanenciaResponseDTO> lazyDataModel;

    private EntradaCreateDTO entrada;
    private SaidaCreateDTO saida;
    private String chaveUnicaFiltro;

    @PostConstruct
    protected void postConstruct() {
        this.entrada = new EntradaCreateDTO();
        this.saida = new SaidaCreateDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> permanenciaService.contar(),
                ( first, pageSize) -> permanenciaService.listarPaginado(first, pageSize),
                PermanenciaResponseDTO::getId,
                (placa) -> permanenciaService.buscarPermanenciaPelaPlaca(placa)
        );
    }

    public void processarEntrada() {
        try {
            entradaService.novaEntrada(entrada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Sucesso", "Entrada registrada!"));
            PrimeFaces.current().executeScript("PF('dialogEntrada').hide()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR,"Erro ao registrar entrada", e.getMessage()));
        }
    }

    public void processarSaida() {
        try {
            saidaService.novaSaida(saida);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Sucesso", "Saída registrada!"));
            PrimeFaces.current().executeScript("PF('dialogSaida').hide()");
            this.saida = new SaidaCreateDTO();
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR,"Erro ao registrar saída", e.getMessage()));
        }
    }

    public void novaSaida(PermanenciaResponseDTO permanencia) {
        // Se for rotativo gera dados para o pagamento
        if (permanencia.getIsRotativo()) {
            PrimeFaces.current().executeScript("PF('dialogSaida').show()");
            LocalDateTime dataSaida= LocalDateTime.now();
            BigDecimal valor = pagamentoService.calcularValorPermanencia(
                    permanencia.getDataEntrada(),
                    dataSaida);
            saida.setValor(valor);
            saida.setPlaca(permanencia.getVeiculo().getPlaca());
            saida.setDataSaida(dataSaida);

            // É setado no dialogSaida
            // saida.setMeioPagamento(null);
        }
        else {
            // Nulls são tratados no Use Case de saída
            saida.setValor(null);
            saida.setMeioPagamento(null);
            saida.setPlaca(permanencia.getVeiculo().getPlaca());
            processarSaida();
        }
    }

    public void pesquisar() {
        lazyDataModel.buscar(chaveUnicaFiltro);
    }

    public void limparFiltro() {
        this.chaveUnicaFiltro = null;
        lazyDataModel.limpar();
    }

    public void novaEntrada() {
        this.entrada = new EntradaCreateDTO();
    }

}
