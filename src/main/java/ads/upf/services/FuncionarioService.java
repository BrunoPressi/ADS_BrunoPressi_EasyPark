package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.mappers.FuncionarioMapper;
import ads.upf.repositories.FuncionarioRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ads.upf.utils.SenhaGenerator.gerarSenha;

@ApplicationScoped
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    @Inject
    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public void salvarFuncionario(FuncionarioCreateDTO funcionarioCreateDTO) {
        Objects.requireNonNull(funcionarioCreateDTO, "Os dados do funcionário não podem ser nulos.");
        Objects.requireNonNull(funcionarioCreateDTO.getNomeCompleto(), "O nome completo é obrigatório.");
        Objects.requireNonNull(funcionarioCreateDTO.getEmail(), "O e-mail é obrigatório.");
        Objects.requireNonNull(funcionarioCreateDTO.getTelefone(), "O telefone é obrigatório");

        String nomeCompletoNormalizado = funcionarioCreateDTO.getNomeCompleto().trim();
        String emailNormalizado = funcionarioCreateDTO.getEmail().trim().toLowerCase();
        String telefoneNormalizado = funcionarioCreateDTO.getTelefone().trim();

        if (funcionarioCreateDTO.getId() == null) {

            checkFuncionarioExists(null,
                    nomeCompletoNormalizado,
                    emailNormalizado
            );

            Funcionario funcionario = FuncionarioMapper.INSTANCE.toFuncionario(funcionarioCreateDTO);
            funcionario.setNomeCompleto(nomeCompletoNormalizado);
            funcionario.setEmail(emailNormalizado);
            funcionario.setTelefone(telefoneNormalizado);
            funcionario.setSenha(gerarSenha());

            funcionarioRepository.persist(funcionario);
        } else {
            checkFuncionarioExists(funcionarioCreateDTO.getId(),
                   nomeCompletoNormalizado,
                    emailNormalizado
            );

            Funcionario funcionario = funcionarioRepository.findByIdOptional(
                    funcionarioCreateDTO.getId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Funcionário não encontrado.")
            );

            funcionario.setNomeCompleto(nomeCompletoNormalizado);
            funcionario.setEmail(emailNormalizado);
            funcionario.setTelefone(telefoneNormalizado);
        }
    }

    public List<FuncionarioResponseDTO> listarFuncionarios() {
        List<Funcionario> funcionarioList = funcionarioRepository.listAll(Sort.by("nomeCompleto"));
        return FuncionarioMapper.INSTANCE.toFuncionarioDtoList(funcionarioList);
    }

    public FuncionarioResponseDTO buscarPorTermo(String termo) {
        if (termo == null || termo.isBlank()) {
            return null;
        }

        String termoTratado = termo.trim();

        // 1. Tenta identificar se é uma busca por ID numérico (protegido contra overflow)
        if (termoTratado.matches("^\\d{1,18}$")) {
            try {
                Long id = Long.valueOf(termoTratado);
                return funcionarioRepository.findByIdOptional(id)
                        .map(FuncionarioMapper.INSTANCE::toFuncionarioDto)
                        .orElse(null);
            } catch (NumberFormatException ignored) {
                // Se exceder a capacidade de Long, continua para busca por texto
            }
        }

        return funcionarioRepository.findByNome(termoTratado)
                .firstResultOptional()
                .map(FuncionarioMapper.INSTANCE::toFuncionarioDto)
                .orElse(null);
    }

    public int contar() {
        return (int) funcionarioRepository.count();
    }

    public List<FuncionarioResponseDTO> listarPaginado(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            return Collections.emptyList();
        }

        List<Funcionario> funcionarios = funcionarioRepository.findAll(Sort.by("nomeCompleto"))
                .range(first, first + pageSize - 1)
                .list();

        return FuncionarioMapper.INSTANCE.toFuncionarioDtoList(funcionarios);
    }

    private void checkFuncionarioExists(Long id, String nomeCompleto, String email) {
        if (funcionarioRepository.existsByEmail(email, id)) {
            throw new EntityExistsException("Já existe um funcionário com esse email.");
        }
        if (funcionarioRepository.existsByNome(nomeCompleto, id)) {
            throw new EntityExistsException("Já existe um funcionário com esse nome.");
        }
    }

}
