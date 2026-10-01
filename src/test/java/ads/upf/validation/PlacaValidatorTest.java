package ads.upf.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlacaValidator - Testes Unitários")
class PlacaValidatorTest {

    private PlacaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PlacaValidator();
    }

    @Nested
    @DisplayName("Cenários com Placas Válidas no Padrão Mercosul")
    class MercosulValidas {

        @ParameterizedTest(name = "Deve aceitar placa Mercosul: {0}")
        @ValueSource(strings = {
                "ABC1D23",
                "BRA2E19",
                "XYZ9K99",
                "abc1d23",      // Minúsculas
                "bra2e19",      // Minúsculas
                "ABC-1D23",     // Com hífen
                "BRA-2E19",     // Com hífen
                "  ABC1D23  "   // Com espaços nas pontas
        })
        void deveAceitarPlacasMercosulValidas(String placa) {
            assertThat(validator.isValid(placa, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("Cenários com Placas Válidas no Padrão Anterior")
    class PadraoAnteriorValidas {

        @ParameterizedTest(name = "Deve aceitar placa padrão anterior: {0}")
        @ValueSource(strings = {
                "ABC-1234",     // Com hífen
                "ABC1234",      // Sem hífen
                "XYZ-9999",     // Com hífen
                "XYZ9999",      // Sem hífen
                "abc-1234",     // Minúsculas com hífen
                "abc1234",      // Minúsculas sem hífen
                "  ABC-1234  "  // Com espaços nas pontas
        })
        void deveAceitarPlacasPadraoAnteriorValidas(String placa) {
            assertThat(validator.isValid(placa, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("Cenários com Valores Nulos (Contrato Jakarta Bean Validation)")
    class NulosEBrancos {

        @ParameterizedTest
        @NullSource
        void deveAceitarValorNuloParaDelegarParaNotNull(String placaNula) {
            assertThat(validator.isValid(placaNula, null)).isTrue();
        }

        @ParameterizedTest(name = "Deve rejeitar valores vazios ou apenas espaços: \"{0}\"")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void deveRejeitarValoresVaziosOuEspacos(String placaVazia) {
            assertThat(validator.isValid(placaVazia, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Cenários com Placas Inválidas")
    class PlacasInvalidas {

        @ParameterizedTest(name = "Deve rejeitar placa inválida: {0}")
        @ValueSource(strings = {
                "ABC123",       // Quantidade insuficiente de dígitos
                "ABC12345",     // Dígitos em excesso
                "AB1234",       // Quantidade insuficiente de letras
                "ABCD1234",     // Letras em excesso
                "123ABCD",      // Letras e números invertidos
                "1234-ABC",     // Números antes do hífen
                "ABC12D3",      // Posição incorreta de letra/número (deveria ser letra na 5ª posição alfanumérica)
                "ABC-12D3",     // Posição incorreta de letra no padrão antigo com hífen
                "ABC1DD23",     // Formato informado acidentalmente no prompt (8 caracteres)
                "ABC--1234",    // Hífen duplicado
                "ABC@1234",     // Caracteres especiais não permitidos
                "ABC.1234"      // Ponto não permitido
        })
        void deveRejeitarPlacasComFormatoInvalido(String placaInvalida) {
            assertThat(validator.isValid(placaInvalida, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Integração com Validador Jakarta Bean Validation")
    class IntegracaoBeanValidation {

        private static Validator beanValidator;

        @BeforeAll
        static void initValidator() {
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                beanValidator = factory.getValidator();
            }
        }

        static class TestDTO {
            @Placa
            private final String placa;

            TestDTO(String placa) {
                this.placa = placa;
            }
        }

        @Test
        void deveValidarComSucessoViaAnotacaoBeanValidation() {
            TestDTO dto = new TestDTO("BRA2E19");
            var violations = beanValidator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        void deveDetectarViolacaoQuandoPlacaForInvalidaViaAnotacao() {
            TestDTO dto = new TestDTO("INVALIDA");
            var violations = beanValidator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .contains("Placa inválida");
        }
    }
}
