package ads.upf.presentation;

import ads.upf.model.DTOs.pagamento.PagamentoResponseDTO;
import ads.upf.model.DTOs.pagamento.ResumoCalculoPermanenciaDTO;
import ads.upf.model.DTOs.permanencia.EntradaCreateDTO;
import ads.upf.model.DTOs.permanencia.PermanenciaResponseDTO;
import ads.upf.model.DTOs.permanencia.SaidaCreateDTO;
import ads.upf.model.enums.PagamentoMeio;
import ads.upf.model.enums.PermanenciaStatus;
import ads.upf.presentation.lazy.GenericLazyDataModel;
import ads.upf.services.EntradaUseCase;
import ads.upf.services.PagamentoService;
import ads.upf.services.PermanenciaService;
import ads.upf.services.SaidaUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    // Estado para o modal de Checkout / Saída Rotativa
    private PermanenciaResponseDTO permanenciaSelecionada;
    private ResumoCalculoPermanenciaDTO resumoCalculo;
    private BigDecimal valorRecebido;

    // Estado para o modal de Comprovante de Pagamento
    private PermanenciaResponseDTO permanenciaComprovante;
    private PagamentoResponseDTO pagamentoSelecionado;
    private ResumoCalculoPermanenciaDTO resumoComprovante;

    private String chaveUnicaFiltro;
    private List<SortMeta> sortBy;

    @PostConstruct
    protected void postConstruct() {
        this.entrada = new EntradaCreateDTO();
        this.saida = new SaidaCreateDTO();
        this.lazyDataModel = new GenericLazyDataModel<>(
                () -> permanenciaService.contar(),
                (first, pageSize) -> permanenciaService.listarPaginado(first, pageSize),
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
                    new FacesMessage(SEVERITY_ERROR, "Erro ao registrar entrada", e.getMessage()));
        }
    }

    public void processarSaida() {
        try {
            if (saida.getMeioPagamento() == PagamentoMeio.DINHEIRO
                    && valorRecebido != null
                    && saida.getValor() != null
                    && valorRecebido.compareTo(saida.getValor()) < 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(SEVERITY_ERROR, "Valor insuficiente",
                                "O valor recebido em dinheiro é menor que o total a pagar."));
                return;
            }

            PermanenciaResponseDTO permanenciaConcluida = saidaService.novaSaida(saida);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Sucesso", "Saída registrada e ocupação finalizada com sucesso!"));

            if (Boolean.TRUE.equals(permanenciaConcluida.getIsRotativo())
                    && permanenciaConcluida.getPagamento() != null) {
                carregarDadosComprovante(permanenciaConcluida);
                PrimeFaces.current().ajax().update("dialogPagamento");
                PrimeFaces.current().executeScript("PF('dialogSaida').hide(); PF('dialogPagamento').show();");
            } else {
                PrimeFaces.current().executeScript("PF('dialogSaida').hide()");
            }

            this.saida = new SaidaCreateDTO();
            this.valorRecebido = null;
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR, "Erro ao registrar saída", e.getMessage()));
        }
    }

    public void novaSaida(PermanenciaResponseDTO permanencia) {
        if (permanencia.getIsRotativo()) {
            this.permanenciaSelecionada = permanencia;
            this.valorRecebido = null;
            LocalDateTime dataSaida = LocalDateTime.now();
            this.resumoCalculo = pagamentoService.detalharCalculoPermanencia(
                    permanencia.getDataEntrada(),
                    dataSaida);

            this.saida = new SaidaCreateDTO();
            this.saida.setValor(resumoCalculo.getValorTotal());
            this.saida.setPlaca(permanencia.getVeiculo().getPlaca());
            this.saida.setMeioPagamento(null);

            PrimeFaces.current().executeScript("PF('dialogSaida').show()");
        }
        else {
            this.permanenciaSelecionada = permanencia;
            this.saida = new SaidaCreateDTO();
            this.saida.setValor(null);
            this.saida.setMeioPagamento(null);
            this.saida.setPlaca(permanencia.getVeiculo().getPlaca());
            processarSaida();
        }
    }

    public void selecionarPagamento(PermanenciaResponseDTO permanencia) {
        if (permanencia.getPagamento() == null && permanencia.getStatus().equals(PermanenciaStatus.em_andamento)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR, "Erro ao visualizar comprovante", "Permanência ainda em andamento!"));
            PrimeFaces.current().ajax().addCallbackParam("sucesso", false);
            return;
        }
        if (!permanencia.getIsRotativo()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(SEVERITY_ERROR, "Não foi possível visualizar o comprovante", "Permanências de mensalistas não geram cobrança avulsa."));
            PrimeFaces.current().ajax().addCallbackParam("sucesso", false);
            return;
        }
        carregarDadosComprovante(permanencia);
        PrimeFaces.current().ajax().addCallbackParam("sucesso", true);
    }

    private void carregarDadosComprovante(PermanenciaResponseDTO permanencia) {
        this.permanenciaComprovante = permanencia;
        this.pagamentoSelecionado = permanencia.getPagamento();
        this.resumoComprovante = pagamentoService.gerarResumoComprovante(permanencia);
    }

    public boolean isPagamentoEmDinheiro() {
        return saida != null && saida.getMeioPagamento() == PagamentoMeio.DINHEIRO;
    }

    public BigDecimal getTroco() {
        if (valorRecebido == null || saida == null || saida.getValor() == null) {
            return BigDecimal.ZERO;
        }
        return valorRecebido.subtract(saida.getValor());
    }

    public boolean isValorRecebidoInsuficiente() {
        return valorRecebido != null
                && saida != null
                && saida.getValor() != null
                && valorRecebido.compareTo(saida.getValor()) < 0;
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
