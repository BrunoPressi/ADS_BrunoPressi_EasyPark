package ads.upf.services;

import ads.upf.exceptions.FuncionarioExistsException;
import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.DTOs.vaga.VagaResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.Vaga;
import ads.upf.model.mappers.FuncionarioMapper;
import ads.upf.model.mappers.VagaMapper;
import ads.upf.repositories.FuncionarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

import static ads.upf.utils.SenhaGenerator.gerarSenha;

@ApplicationScoped
public class FuncionarioService {

    @Inject
    protected FuncionarioRepository funcionarioRepository;

    @Transactional
    public void salvarFuncionario(FuncionarioCreateDTO funcionarioCreateDTO) {

        if (funcionarioCreateDTO.getId() == null) {

            checkFuncionarioExists(null,
                    funcionarioCreateDTO.getNomeCompleto(),
                    funcionarioCreateDTO.getEmail()
            );

            Funcionario funcionario = FuncionarioMapper.INSTANCE.toFuncionario(funcionarioCreateDTO);
            funcionario.setSenha(gerarSenha());

            funcionarioRepository.persist(funcionario);
        } else {
            checkFuncionarioExists(funcionarioCreateDTO.getId(),
                    funcionarioCreateDTO.getNomeCompleto(),
                    funcionarioCreateDTO.getEmail()
            );

            Funcionario funcionario = funcionarioRepository.findByIdOptional(
                    funcionarioCreateDTO.getId())
                    .orElseThrow(
                            () -> new RuntimeException("Funcionário não encontrado.")
            );


            funcionario.setNomeCompleto(funcionarioCreateDTO.getNomeCompleto());
            funcionario.setEmail(funcionarioCreateDTO.getEmail());
            funcionario.setTelefone(funcionarioCreateDTO.getTelefone());
            funcionario.setDataNascimento(funcionarioCreateDTO.getDataNascimento());
        }
    }

    public List<FuncionarioResponseDTO> listarFuncionarios() {
        List<Funcionario> funcionarioList = funcionarioRepository.listAll();
        return FuncionarioMapper.INSTANCE.toFuncionarioDtoList(funcionarioList);
    }

    public FuncionarioResponseDTO buscarPorTermo(String termo) {
        if (termo.isEmpty() || termo.isBlank()) return null;

        termo = termo.toLowerCase().trim();

        if (termo.matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s]+$")) {
            return funcionarioRepository.findByNome(termo)
                    .firstResultOptional()
                    .map(FuncionarioMapper.INSTANCE::toFuncionarioDto)
                    .orElse(null);
        }

        if (termo.matches("\\d+")) {
            return funcionarioRepository.findByIdOptional(Long.valueOf(termo))
                    .map(FuncionarioMapper.INSTANCE::toFuncionarioDto)
                    .orElse(null);
        }
        return null;
    }

    public int contar() {
        return (int) funcionarioRepository.count();
    }

    public List<FuncionarioResponseDTO> listarPaginado(int first, int pageSize) {
        List<Funcionario> funcionarios = funcionarioRepository.findAll()
                .range(first, first + pageSize - 1)
                .list();
        return FuncionarioMapper.INSTANCE.toFuncionarioDtoList(funcionarios);
    }

    private void checkFuncionarioExists(Long id, String nomeCompleto, String email) {
        if (funcionarioRepository.existsByEmail(email, id)) {
            throw new FuncionarioExistsException("Já existe um funcionário com esse email.");
        }
        if (funcionarioRepository.existsByNome(nomeCompleto, id)) {
            throw new FuncionarioExistsException("Já existe um funcionário com esse nome.");
        }
    }

}
