package ads.upf.utils;

// Classes nativas do Java para criptografia e manipulação de bytes
import org.eclipse.microprofile.config.ConfigProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {

    // ALGORITMO:
    // - AES: É o padrão mundial de criptografia simétrica (muito seguro e rápido).
    // - GCM: Modo de operação moderno que além de esconder o dado, impede adulteração (se alguém alterar 1 letra no banco, ele acusa erro).
    // - NoPadding: O modo GCM não precisa de preenchimento de bytes extras.
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    // Tamanho da "etiqueta de autenticação" (Tag) em bits. 128 bits é a recomendação máxima de segurança.
    private static final int TAG_LENGTH_BIT = 128;

    // Tamanho do IV (Vetor de Inicialização) em bytes. No modo GCM, a recomendação oficial é 12 bytes.
    private static final int IV_LENGTH_BYTE = 12;

    // A CHAVE SECRETA:
    // Para AES-256, a chave PRECISA ter exatamente 32 caracteres (32 bytes = 256 bits).
    // Pense nela como a "senha mestra" do cofre. Quem tiver essa chave abre qualquer dado criptografado.
    // (Em um ambiente de produção real, guardamos isso fora do código, em variáveis de ambiente).
    // Lê a propriedade "app.crypto.secret-key" do Quarkus (que veio do seu .env)
    // Lê a propriedade "app.crypto.secret-key" do Quarkus (que veio do seu .env)
    private static String getSecretKey() {
        return ConfigProvider.getConfig().getValue("app.crypto.secret-key", String.class);
    }

    private static SecretKey getKey() {
        return new SecretKeySpec(getSecretKey().getBytes(StandardCharsets.UTF_8), "AES");
    }

    /**
     * MÉTODO DE CRIPTOGRAFAR (Texto legível -> Código ilegível)
     */
    public static String encrypt(String plainText) {
        // Se o CPF for nulo ou vazio, não faz nada
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }

        try {
            // 1. O QUE É O IV?
            // É um número aleatório único gerado para cada gravação.
            // Ele garante que, mesmo que você salve o mesmo CPF 10 vezes, o resultado no banco será sempre diferente.
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv); // Preenche o array com bytes 100% aleatórios e seguros

            // 2. PREPARANDO O "MOTOR" DE CRIPTOGRAFIA (Cipher)
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Informamos: Modo ENCRYPT (criptografar), a nossa Chave Secreta e os parâmetros do GCM com o IV aleatório
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            // 3. EXECUTANDO A CRIPTOGRAFIA
            // Transforma o texto do CPF em bytes e realiza o cálculo criptográfico
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 4. JUNTANDO O IV + TEXTO CIFRADO
            // Para descriptografar no futuro, vamos precisar do mesmo IV que usamos agora.
            // O IV não é segredo (ele pode ser público). Então nós colamos o IV (12 bytes) na frente do texto cifrado.
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);          // Coloca os 12 bytes do IV no início
            byteBuffer.put(cipherText);  // Coloca o resto dos bytes criptografados

            // 5. POR QUE BASE64?
            // O resultado até aqui são bytes binários puros (não imprimíveis).
            // O Base64 converte esses bytes em texto normal (A-Z, 0-9) para podermos salvar numa coluna VARCHAR do banco.
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            // Caso ocorra qualquer erro na criptografia, interrompe o processo com uma mensagem clara
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * MÉTODO DE DESCRIPTOGRAFAR (Código ilegível -> Texto original)
     */
    public static String decrypt(String cipherTextBase64) {
        // Se o dado no banco for nulo ou vazio, retorna ele mesmo
        if (cipherTextBase64 == null || cipherTextBase64.isBlank()) {
            return cipherTextBase64;
        }

        try {
            // 1. DESFAZ O BASE64
            // Pega o texto do banco e volta para o array de bytes binários
            byte[] decoded = Base64.getDecoder().decode(cipherTextBase64);

            // 2. SEPARA O IV DO TEXTO CIFRADO
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // Lê os primeiros 12 bytes (lembra que colocamos o IV lá na criptografia?)
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            // Pega o restante dos bytes (que é o CPF criptografado de fato)
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // 3. PREPARANDO O MOTOR PARA DESCRIPTOGRAFAR
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Informamos: Modo DECRYPT (descriptografar), a mesma Chave Secreta e o IV que acabamos de extrair
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            // 4. DESCRIPTOGRAFA E VERIFICA ADULTERAÇÃO
            // O doFinal aqui desfaz o cálculo. Se a chave estiver errada ou alguém alterou
            // algum byte no banco de dados, esta linha lança um erro de segurança imediatamente!
            byte[] plainText = cipher.doFinal(cipherText);

            // 5. Converte os bytes de volta para texto comum (String) e devolve o CPF original
            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado. Chave incorreta ou dado violado.", e);
        }
    }
}