package ads.upf.model.enums;

public enum ParcelaStatus {

    pendente("Pendente"),
    paga("Paga"),
    atrasada("Atrasada"),
    cancelada("Cancelada");

    private final String descricao;

    ParcelaStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }

}
