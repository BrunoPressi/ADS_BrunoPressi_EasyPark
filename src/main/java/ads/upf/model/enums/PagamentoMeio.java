package ads.upf.model.enums;

public enum PagamentoMeio {

    CREDIT_CARD("Cartão de Crédito"),
    PIX("Pix"),
    DINHEIRO("Dinheiro"),
    DEBIT_CARD("Cartão de Débito");

    private final String descricao;

    PagamentoMeio(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }

}
