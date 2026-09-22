package ads.upf.services;

import ads.upf.exceptions.FuncionarioExistsException;
import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.Usuario;
import ads.upf.model.mappers.FuncionarioMapper;
import ads.upf.repositories.FuncionarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;


import java.util.List;

import static ads.upf.utils.senhaGenerator.gerarSenha;

@ApplicationScoped
public class FuncionarioService {

    @Inject
    FuncionarioRepository funcionarioRepository;

    @Transactional
    public void salvarFuncionario(FuncionarioCreateDTO funcionarioCreateDTO) {

        if (funcionarioCreateDTO.getId() == null) {
            checkFuncionarioExists(null, funcionarioCreateDTO.getNomeCompleto(), funcionarioCreateDTO.getEmail());
            Funcionario funcionario = FuncionarioMapper.INSTANCE.toFuncionario(funcionarioCreateDTO);
            funcionario.setSenha(gerarSenha());

            funcionarioRepository.persist(funcionario);
        } else {
            checkFuncionarioExists(funcionarioCreateDTO.getId(), funcionarioCreateDTO.getNomeCompleto(), funcionarioCreateDTO.getEmail());
            Funcionario funcionario = funcionarioRepository.findById(funcionarioCreateDTO.getId());

            if (funcionario == null) {
                throw new IllegalArgumentException("Funcionário não encontrado");
            }

            funcionario.setNomeCompleto(funcionarioCreateDTO.getNomeCompleto());
            funcionario.setEmail(funcionarioCreateDTO.getEmail());
            funcionario.setTelefone(funcionarioCreateDTO.getTelefone());
            funcionario.setDataNascimento(funcionarioCreateDTO.getDataNascimento());
        }
    }

    @Transactional
    public List<FuncionarioResponseDTO> listarFuncionarios() {
        List<Funcionario> funcionarioList = funcionarioRepository.listAll();
        return FuncionarioMapper.INSTANCE.toDtoList(funcionarioList);
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
