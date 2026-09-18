package ads.upf.services;

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
    public void createUsuario(FuncionarioCreateDTO funcionarioCreateDTO) {
        Usuario usuario = FuncionarioMapper.INSTANCE.toFuncionario(funcionarioCreateDTO);
        Funcionario funcionario = new Funcionario();

        funcionario.setNomeCompleto(usuario.getNomeCompleto());
        funcionario.setEmail(usuario.getEmail());
        funcionario.setTelefone(usuario.getTelefone());
        funcionario.setDataNascimento(usuario.getDataNascimento());

        funcionario.setSenha(gerarSenha());

        funcionarioRepository.persist(funcionario);
    }

    @Transactional
    public List<FuncionarioResponseDTO> listarFuncionarios() {
        List<Funcionario> funcionarioList = funcionarioRepository.listAll();
        return FuncionarioMapper.INSTANCE.toDtoList(funcionarioList);
    }

}
