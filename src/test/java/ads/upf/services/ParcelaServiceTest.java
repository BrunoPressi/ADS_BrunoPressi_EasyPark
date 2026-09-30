package ads.upf.services;

import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.StatusParcela;
import ads.upf.repositories.ParcelaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParcelaService - Suíte de Testes Unitários")
public class ParcelaServiceTest {

    @Mock
    private ParcelaRepository parcelaRepository;

    @InjectMocks
    private ParcelaService parcelaService;

    @Nested
    @DisplayName("Cálculo de Competências e Geração de Parcelas")
    class GeracaoParcelasCompetenciaTests {

        @ParameterizedTest(name = "De {0} até {1} deve gerar exatamente {2} parcela(s)")
        @CsvSource({
                // Cenário 1: Mesmo mês civil (Fevereiro completo) -> Math.max(0, 1) = 1
                "2026-02-01, 2026-02-28, 1",

                // Cenário 2: Fim de mês para fim de mês (Janeiro a Fevereiro) -> 1 mês
                "2026-01-31, 2026-02-28, 1",

                // Cenário 3: Mesmo mês com dias diferentes -> Math.max(0, 1) = 1
                "2026-05-10, 2026-05-31, 1",

                // Cenário 4: Semestral regular (Janeiro a Julho) -> 6 meses
                "2026-01-10, 2026-07-10, 6",

                // Cenário 5: Anual (Janeiro a Janeiro do ano seguinte) -> 12 meses
                "2026-01-01, 2027-01-01, 12",

                // Cenário 6: Virada de Ano (Novembro a Fevereiro) -> 3 meses
                "2026-11-15, 2027-02-15, 3",

                // Cenário 7: Contrato longo de 2 anos -> 24 meses
                "2026-01-01, 2028-01-01, 24"
        })
        @DisplayName("Deve calcular a quantidade correta de parcelas por competência mensal")
        void deveCalcularQuantidadeCorretaDeParcelas(LocalDate dataInicio, LocalDate dataTermino, int totalEsperado) {
            List<Parcela> parcelas = parcelaService.gerarParcelas(dataInicio, dataTermino);

            assertThat(parcelas)
                    .isNotNull()
                    .hasSize(totalEsperado);
        }

        @Test
        @DisplayName("Deve garantir integridade e consistência dos atributos de cada parcela gerada")
        void deveGarantirAtributosConsistentesNaParcela() {
            LocalDate inicio = LocalDate.of(2026, 1, 10);
            LocalDate termino = LocalDate.of(2026, 4, 10); // 3 competências

            List<Parcela> parcelas = parcelaService.gerarParcelas(inicio, termino);

            assertThat(parcelas).hasSize(3);

            // Valida sequência dos números de parcelas (1, 2, 3)
            for (int i = 0; i < parcelas.size(); i++) {
                Parcela p = parcelas.get(i);
                assertThat(p.getNumeroParcela()).isEqualTo(i + 1);
                assertThat(p.getStatus()).isEqualTo(StatusParcela.pendente);
                assertThat(p.getValor()).isEqualByComparingTo(BigDecimal.valueOf(180.00));
                assertThat(p.getCobrancaEnviada()).isFalse();
                assertThat(p.getDataPagamento()).isNull();
            }

            // Valida evolução sequencial dos vencimentos mês a mês
            assertThat(parcelas.get(0).getDataVencimento()).isEqualTo(LocalDate.of(2026, 2, 10));
            assertThat(parcelas.get(1).getDataVencimento()).isEqualTo(LocalDate.of(2026, 3, 10));
            assertThat(parcelas.get(2).getDataVencimento()).isEqualTo(LocalDate.of(2026, 4, 10));
        }
    }

    @Nested
    @DisplayName("Processamento Agendado de Parcelas Vencidas")
    class ProcessamentoVencidasTests {

        @Test
        @DisplayName("Deve invocar o update em lote atômico no repositório e retornar o total afetado")
        void deveExecutarBulkUpdateComSucesso() {
            when(parcelaRepository.update(any(), any(), any(), any(), any())).thenReturn(7);

            int totalAtualizadas = parcelaService.processarParcelasVencidasTest();

            assertThat(totalAtualizadas).isEqualTo(7);

            verify(parcelaRepository).update(
                    any(),
                    eq(StatusParcela.atrasada),
                    any(),
                    eq(StatusParcela.pendente),
                    eq(LocalDate.now())
            );
        }

        @Test
        @DisplayName("Deve retornar 0 quando não existirem parcelas vencidas a atualizar")
        void deveRetornarZeroQuandoNaoHouverParcelasVencidas() {
            when(parcelaRepository.update(any(), any(), any(), any(), any())).thenReturn(0);

            int totalAtualizadas = parcelaService.processarParcelasVencidasTest();

            assertThat(totalAtualizadas).isZero();
        }
    }

}
