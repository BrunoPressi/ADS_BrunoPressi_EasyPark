package ads.upf.services;

import ads.upf.model.entities.Parcela;
import ads.upf.model.enums.StatusParcela;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ParcelaService {

    @Transactional
    public List<Parcela> gerarParcelas(LocalDate dataInicio, LocalDate dataTermino) {

        Long totalMeses = ChronoUnit.MONTHS.between(dataInicio, dataTermino);
        List<Parcela> parcelas = new ArrayList<>();

        for (int i = 1; i < totalMeses + 1; i++) {
            Parcela parcela = new Parcela();

            LocalDate dataVencimento = dataInicio.plusMonths(i).withDayOfMonth(10);

            parcela.setDataVencimento(dataVencimento);
            parcela.setNumeroParcela(i);
            parcela.setStatus(StatusParcela.pendente);

            parcelas.add(parcela);
        }
        return parcelas;
    }
}
