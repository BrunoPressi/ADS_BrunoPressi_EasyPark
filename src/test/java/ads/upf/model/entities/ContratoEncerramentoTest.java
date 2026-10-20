package ads.upf.model.entities;

import ads.upf.model.enums.ContratoStatus;
import ads.upf.model.enums.ParcelaStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato - encerramento automático por quitação")
class ContratoEncerramentoTest {

    private Contrato contratoCom(ContratoStatus status, ParcelaStatus... statusParcelas) {
        Contrato contrato = new Contrato();
        contrato.setStatus(status);
        for (ParcelaStatus s : statusParcelas) {
            Parcela p = new Parcela();
            p.setStatus(s);
            contrato.adicionarParcela(p);
        }
        return contrato;
    }

    @Test
    @DisplayName("Encerra quando todas as parcelas estão pagas")
    void encerraQuandoTodasPagas() {
        Contrato c = contratoCom(ContratoStatus.ativo, ParcelaStatus.paga, ParcelaStatus.paga);

        c.encerrarSeQuitado();

        assertThat(c.getStatus()).isEqualTo(ContratoStatus.encerrado);
    }

    @Test
    @DisplayName("Não encerra enquanto houver parcela pendente")
    void naoEncerraComPendente() {
        Contrato c = contratoCom(ContratoStatus.ativo, ParcelaStatus.paga, ParcelaStatus.pendente);

        c.encerrarSeQuitado();

        assertThat(c.getStatus()).isEqualTo(ContratoStatus.ativo);
    }

    @Test
    @DisplayName("Não encerra com parcela atrasada")
    void naoEncerraComAtrasada() {
        Contrato c = contratoCom(ContratoStatus.ativo, ParcelaStatus.paga, ParcelaStatus.atrasada);

        c.encerrarSeQuitado();

        assertThat(c.getStatus()).isEqualTo(ContratoStatus.ativo);
    }

    @ParameterizedTest
    @EnumSource(value = ContratoStatus.class, names = {"cancelado", "encerrado", "inadimplente"})
    @DisplayName("Só altera contratos ativos")
    void soAlteraContratoAtivo(ContratoStatus statusInicial) {
        Contrato c = contratoCom(statusInicial, ParcelaStatus.paga);

        c.encerrarSeQuitado();

        assertThat(c.getStatus()).isEqualTo(statusInicial);
    }

    @Test
    @DisplayName("Parcela cancelada impede o encerramento")
    void parcelaCanceladaNaoContaComoPaga() {
        Contrato c = contratoCom(ContratoStatus.ativo, ParcelaStatus.paga, ParcelaStatus.cancelada);

        c.encerrarSeQuitado();

        assertThat(c.getStatus()).isEqualTo(ContratoStatus.ativo);
    }
}
