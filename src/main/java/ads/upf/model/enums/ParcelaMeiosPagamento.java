package ads.upf.model.enums;

public enum ParcelaMeiosPagamento {

    PIX("Pix"),
    BOLETO("Boleto"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    UNDEFINED("Indefinido");

    private final String descricao;

    ParcelaMeiosPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }

}
