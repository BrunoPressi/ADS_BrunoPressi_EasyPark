package ads.upf.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// @Converter avisa ao JPA/Hibernate: "Essa classe sabe converter dados entre o Java e o Banco"
@Converter
public class StringCryptoConverter implements AttributeConverter<String, String> {

    /**
     * MOMENTO DO INSERT OU UPDATE:
     * O Hibernate chama esse método automaticamente logo ANTES de enviar o dado para o banco.
     *
     * @param attribute É o CPF em texto limpo vindo do seu objeto Java (ex: "12345678901")
     * @return O texto criptografado que será gravado fisicamente na tabela
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return CryptoUtil.encrypt(attribute);
    }

    /**
     * MOMENTO DO SELECT:
     * O Hibernate chama esse método automaticamente logo APÓS ler a linha da tabela do banco.
     *
     * @param dbData É o texto criptografado lido da coluna do banco de dados
     * @return O CPF original já descriptografado que vai preencher o atributo da sua Entidade Java
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return CryptoUtil.decrypt(dbData);
    }
}