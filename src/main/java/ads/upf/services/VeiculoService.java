package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.veiculo.VeiculoCreateDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.enums.VeiculoTipo;
import ads.upf.model.mappers.VeiculoMapper;
import ads.upf.repositories.VeiculoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    @Inject
    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public void salvarVeiculo(VeiculoCreateDTO veiculoCreateDTO) {
        Optional<Veiculo> veiculoExistente = veiculoRepository.buscarPelaPlaca(veiculoCreateDTO.getPlaca());

        if (veiculoExistente.isPresent()) {
            throw new EntityExistsException("Esse veículo já está cadastrado.");
        }

        Veiculo veiculo = VeiculoMapper.INSTANCE.toVeiculo(veiculoCreateDTO);
        veiculoRepository.persist(veiculo);
    }

    public VeiculoResponseDTO buscarVeiculoPelaPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return null;
        }
        return veiculoRepository.buscarPelaPlaca(placa)
                .map(VeiculoMapper.INSTANCE::toVeiculoDto)
                .orElse(null);
    }

    public Veiculo verificarVeiculoExiste(String placa) {
        return veiculoRepository.buscarPelaPlaca(placa)
                .orElseThrow(
                        () -> new EntityNotFoundException("Veículo não encontrado")
                );
    }

    public Veiculo obterOuCriar(String placa, VeiculoTipo tipoVeiculo) {
        return veiculoRepository.buscarPelaPlaca(placa)
                .orElseGet(() -> {
                    Veiculo novo = new Veiculo();
                    novo.setPlaca(placa);
                    novo.setTipo(tipoVeiculo);
                    veiculoRepository.persist(novo);
                    return novo;
                });
    }

}
