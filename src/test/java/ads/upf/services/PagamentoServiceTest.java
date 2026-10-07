package ads.upf.services;

import ads.upf.model.entities.Pagamento;
import ads.upf.model.enums.PagamentoStatus;
import ads.upf.repositories.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoService - Suíte de Testes Unitários")
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private LocalDateTime entradaBase;

    @BeforeEach
    void setUp() {
        entradaBase = LocalDateTime.of(2026, 10, 6, 10, 0, 0);
    }

    @Nested
    @DisplayName("Cálculo de permanência - Cenários de sucesso")
    class CalculoPermanenciaSucesso {

        @Test
        @DisplayName("Deve cobrar taxa mínima da 1ª hora (R$ 70,00) quando saída for no mesmo instante (0 segundos)")
        void deveCobrarPrimeiraHoraQuandoZeroSegundos() {
            LocalDateTime saida = entradaBase;

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("70.00");
        }

        @ParameterizedTest(name = "Permanência de {0} minutos deve custar R$ 70.00")
        @CsvSource({
                "1",
                "15",
                "30",
                "45",
                "59",
                "60"
        })
        @DisplayName("Deve cobrar taxa fixa de 1ª hora (R$ 70,00) para qualquer tempo de até 60 minutos")
        void deveCobrarPrimeiraHoraAte60Minutos(int minutos) {
            LocalDateTime saida = entradaBase.plusMinutes(minutos);

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("70.00");
        }

        @Test
        @DisplayName("Deve cobrar 1ª hora + 1 fração (R$ 105,00) ao exceder 60 minutos por 1 segundo")
        void deveCobrarUmaFracaoAdicionalNoPrimeiroSegundoExcedente() {
            LocalDateTime saida = entradaBase.plusMinutes(60).plusSeconds(1);

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("105.00");
        }

        @ParameterizedTest(name = "Permanência de {0} minutos deve custar R$ 105.00 (1h + 1 fração de 30m)")
        @CsvSource({
                "61",
                "75",
                "89",
                "90"
        })
        @DisplayName("Deve cobrar 1ª hora + 1 fração (R$ 105,00) para permanências entre 61 e 90 minutos")
        void deveCobrarUmaFracaoEntre61E90Minutos(int minutos) {
            LocalDateTime saida = entradaBase.plusMinutes(minutos);

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("105.00");
        }

        @Test
        @DisplayName("Deve cobrar 1ª hora + 2 frações (R$ 140,00) ao exceder 90 minutos por 1 segundo")
        void deveCobrarDuasFracoesApos90MinutosE1Segundo() {
            LocalDateTime saida = entradaBase.plusMinutes(90).plusSeconds(1);

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("140.00");
        }

        @ParameterizedTest(name = "Permanência de {0} minutos deve custar R$ 140.00 (1h + 2 frações de 30m)")
        @CsvSource({
                "91",
                "105",
                "119",
                "120"
        })
        @DisplayName("Deve cobrar 1ª hora + 2 frações (R$ 140,00) para permanências entre 91 e 120 minutos (2 horas)")
        void deveCobrarDuasFracoesEntre91E120Minutos(int minutos) {
            LocalDateTime saida = entradaBase.plusMinutes(minutos);

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("140.00");
        }

        @Test
        @DisplayName("Deve calcular corretamente para permanência de 24 horas (70 + 46 * 35 = R$ 1680,00)")
        void deveCalcularCorretamentePermanenciaDe24Horas() {
            LocalDateTime saida = entradaBase.plusHours(24);

            BigDecimal valor = pagamentoService.calcularValorPermanencia(entradaBase, saida);

            assertThat(valor).isEqualByComparingTo("1680.00");
        }
    }

    @Nested
    @DisplayName("Cálculo de permanência - Validações e exceções")
    class CalculoPermanenciaValidacoes {

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando data de saída for anterior à data de entrada")
        void deveLancarExcecaoQuandoDataSaidaAnteriorADataEntrada() {
            LocalDateTime saida = entradaBase.minusMinutes(10);

            assertThatThrownBy(() -> pagamentoService.calcularValorPermanencia(entradaBase, saida))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de saída não pode ser anterior à data de entrada.");
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando data de entrada for nula")
        void deveLancarExcecaoQuandoDataEntradaNula() {
            LocalDateTime saida = entradaBase.plusHours(1);

            assertThatThrownBy(() -> pagamentoService.calcularValorPermanencia(null, saida))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("As datas de entrada e saída são obrigatórias.");
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando data de saída for nula")
        void deveLancarExcecaoQuandoDataSaidaNula() {
            assertThatThrownBy(() -> pagamentoService.calcularValorPermanencia(entradaBase, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("As datas de entrada e saída são obrigatórias.");
        }
    }

    @Nested
    @DisplayName("Geração e persistência do Pagamento")
    class GeracaoPagamento {

        @Test
        @DisplayName("Deve gerar entidade Pagamento com status pendente e valor correto")
        void deveGerarPagamentoComSucesso() {
            LocalDateTime saida = entradaBase.plusMinutes(75); // 1h + 1 fração = 105.00

            Pagamento pagamento = pagamentoService.gerarPagamento(entradaBase, saida);

            assertThat(pagamento).isNotNull();
            assertThat(pagamento.getValor()).isEqualByComparingTo("105.00");
            assertThat(pagamento.getStatus()).isEqualTo(PagamentoStatus.pendente);

            ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
            verify(pagamentoRepository).persist(captor.capture());

            Pagamento pagamentoPersistido = captor.getValue();
            assertThat(pagamentoPersistido.getValor()).isEqualByComparingTo("105.00");
            assertThat(pagamentoPersistido.getStatus()).isEqualTo(PagamentoStatus.pendente);
        }
    }
}
