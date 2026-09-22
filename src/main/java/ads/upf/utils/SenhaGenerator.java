package ads.upf.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SenhaGenerator {

    public static String gerarSenha() {

        // Para gerar a senha
        PasswordGenerator gerador = new PasswordGenerator();

        // Para criptografar
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Define as regras/critérios
        CharacterRule minusculas = new CharacterRule(EnglishCharacterData.LowerCase, 5);
        CharacterRule maiusculas = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitos = new CharacterRule(EnglishCharacterData.Digit, 3);

        CharacterData caracteresEspeciais = new CharacterData() {
            public String getErrorCode() { return "INSUFFICIENT_SPECIAL"; }
            public String getCharacters() { return "!@#$%&*()_-+=<>?"; }
        };
        CharacterRule especiais = new CharacterRule(caracteresEspeciais, 1);

        List<CharacterRule> regras = Arrays.asList(minusculas, maiusculas, digitos, especiais);

        // Gera e criptografa uma senha aleatória de 11 caracteres respeitando os critérios acima
        return encoder.encode(gerador.generatePassword(11, regras));
    }

}
