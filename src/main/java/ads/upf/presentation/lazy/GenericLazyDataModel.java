package ads.upf.presentation.lazy;

import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CLASSE GENÉRICA DE PAGINAÇÃO LAZY (SOB DEMANDA) PARA PRIMEFACES + QUARKUS
 *
 * O QUE É O GENERICS (<T>)?
 * ----------------------------------------------------------------------------------------
 * O "<T>" é um "tipo genérico" (Type Parameter). Pense nele como uma "variável de tipo".
 * Em vez de criar um "ClienteLazyDataModel", um "VagaLazyDataModel" e um "FuncionarioLazyDataModel",
 * criamos uma única classe com "<T>".
 *
 * Quando você instancia:
 *   new GenericLazyDataModel<ClienteResponseDTO>(...)
 * O Java substitui automaticamente todo lugar onde está "T" por "ClienteResponseDTO".
 *
 * Vantagens do Generics aqui:
 * 1. Reutilização Máxima: O mesmo arquivo serve para qualquer tabela/DTO do sistema.
 * 2. Type-Safety (Segurança de Tipos): O compilador garante que você não vai misturar
 *    objetos de tipos diferentes por engano, sem precisar fazer casts manuais como "(Cliente) obj".
 */
@Getter @Setter
public class GenericLazyDataModel<T> extends LazyDataModel<T> {

    /*
     * INTERFACES FUNCIONAIS (LAMBDAS) QUE OPERAM SOBRE O TIPO GENÉRICO <T>:
     *
     * 1. Supplier<Integer>:
     *    Representa uma função que NÃO recebe nada () e entrega um Integer.
     *    Uso: chama o método contar() do seu Service.
     */
    private final Supplier<Integer> countSupplier;

    /*
     * 2. BiFunction<Integer, Integer, List<T>>:
     *    Recebe DOIS inteiros (first, pageSize) e devolve uma Lista de <T> (List<T>).
     *    Se T for ClienteResponseDTO, devolverá List<ClienteResponseDTO>.
     *    Uso: chama o listarPaginado(first, pageSize) do seu Service.
     */
    private final BiFunction<Integer, Integer, List<T>> loadFunction;

    /*
     * 3. Function<T, Object>:
     *    Recebe o objeto genérico <T> e extrai o identificador dele (geralmente o ID).
     *    Uso: Você passa a referência do método do DTO, ex: ClienteResponseDTO::getId.
     */
    private final Function<T, Object> idExtractor;

    /*
     * 4. Function<String, T>:
     *    Recebe uma String (o termo ou ID digitado) e devolve exatamente 1 objeto do tipo <T>.
     *    Uso: chama o buscarPorChaveUnica(...) do seu Service.
     */
    private final Function<String, T> findByUniqueKeyFunction;

    // Guarda o texto digitado na barra de pesquisa (se houver busca ativa)
    private String termoBusca;

    /**
     * CONSTRUTOR:
     * Conecta o LazyDataModel com as operações do Service correspondente ao tipo <T>.
     */
    public GenericLazyDataModel(Supplier<Integer> countSupplier,
                                BiFunction<Integer, Integer, List<T>> loadFunction,
                                Function<T, Object> idExtractor,
                                Function<String, T> findByUniqueKeyFunction) {
        this.countSupplier = countSupplier;
        this.loadFunction = loadFunction;
        this.idExtractor = idExtractor;
        this.findByUniqueKeyFunction = findByUniqueKeyFunction;
    }

    /**
     * MÉTODO 1 DO PRIMEFACES: count(...)
     * Pergunta: "Quantos registros existem no total?"
     * O PrimeFaces precisa desse número para calcular quantas páginas (1, 2, 3...) desenhar no rodapé.
     */
    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        // Se o usuário fez uma busca por chave única (ex: CPF ou ID):
        if (temBuscaAtiva()) {
            // Se achou o registro único, o total é 1; se não achou, o total é 0.
            return executarBuscaUnica() != null ? 1 : 0;
        }

        // Caso contrário, executa a contagem total no banco via Service (SELECT count(*))
        return countSupplier != null ? countSupplier.get() : 0;
    }

    /**
     * MÉTODO 2 DO PRIMEFACES: load(...)
     * Pergunta: "Me dê apenas os registros da página atual (do índice 'first' até 'first + pageSize')".
     * Retorna: List<T> -> uma lista tipada com os registros daquela página específica.
     */
    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        // Se houver busca por chave única ativa, devolve apenas uma lista com aquele único objeto <T>
        if (temBuscaAtiva()) {
            T item = executarBuscaUnica();
            return item != null ? java.util.List.of(item) : java.util.Collections.emptyList();
        }

        // Se for paginação normal, busca a fatia solicitada no banco (OFFSET first LIMIT pageSize)
        return loadFunction != null ? loadFunction.apply(first, pageSize) : java.util.Collections.emptyList();
    }

    /**
     * MÉTODO 3 DO PRIMEFACES: getRowKey(T object)
     * Pergunta: "Qual é o identificador único (String) desta linha específica <T>?"
     * Usa a função idExtractor para extrair o ID do objeto <T>.
     */
    @Override
    public String getRowKey(T object) {
        return object != null ? String.valueOf(idExtractor.apply(object)) : null;
    }

    /**
     * MÉTODO 4 DO PRIMEFACES: getRowData(String rowKey)
     * Pergunta: "Dado este ID (rowKey), qual é o objeto <T> correspondente?"
     * O PrimeFaces chama este método quando uma linha é selecionada ou clicada para editar.
     */
    @Override
    public T getRowData(String rowKey) {
        if (rowKey == null || findByUniqueKeyFunction == null) return null;
        return findByUniqueKeyFunction.apply(rowKey);
    }

    /**
     * Define o termo de busca por chave única (ex: ID ou CPF) e reseta para a página 1
     */
    public void buscar(String termo) {
        this.termoBusca = termo != null && !termo.isBlank() ? termo.trim() : null;
    }

    /**
     * Limpa o filtro de busca única, voltando à paginação de todos os registros
     */
    public void limpar() {
        this.termoBusca = null;
    }

    private boolean temBuscaAtiva() {
        return termoBusca != null && !termoBusca.isBlank() && findByUniqueKeyFunction != null;
    }

    private T executarBuscaUnica() {
        return findByUniqueKeyFunction.apply(termoBusca);
    }
}