package ads.upf.utils;

import org.eclipse.microprofile.config.ConfigProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecurityUtil {

    private static final String HMAC_SECRET = ConfigProvider.getConfig().getValue("app.crypto.secret-hmac", String.class);

    // 1. Gera o HMAC-SHA256 (Blind Index para buscas)
    public static String generateBlindIndex(String rawCpf) {
        if (rawCpf == null) return null;
        try {
            String cleanCpf = rawCpf.replaceAll("\\D", ""); // apenas dígitos
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(cleanCpf.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar blind index", e);
        }
    }
}