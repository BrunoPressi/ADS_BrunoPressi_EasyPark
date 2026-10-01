package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.model.DTOs.veiculo.VeiculoCreateDTO;
import ads.upf.model.DTOs.veiculo.VeiculoResponseDTO;
import ads.upf.model.entities.Veiculo;
import ads.upf.model.mappers.VeiculoMapper;
import ads.upf.repositories.VeiculoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    @Inject
    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public void salvarVeiculo(VeiculoCreateDTO veiculoCreateDTO) {
        boolean veiculoExists = veiculoRepository.find("placa = ?1",
                        veiculoCreateDTO.getPlaca().trim())
                        .firstResultOptional().isPresent();

        if (veiculoExists) {
            throw new EntityExistsException("Esse veículo já está cadastrado.");
        }

        Veiculo veiculo = VeiculoMapper.INSTANCE.toVeiculo(veiculoCreateDTO);
        veiculoRepository.persist(veiculo);
    }

    public VeiculoResponseDTO buscarVeiculoPelaPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return null;
        }
        return veiculoRepository.find("placa = ?1", placa.trim())
                .firstResultOptional()
                .map(VeiculoMapper.INSTANCE::toVeiculoDto)
                .orElse(null);
    }

}
