package ads.upf.services;

import ads.upf.exceptions.EntityExistsException;
import ads.upf.exceptions.EntityNotFoundException;
import ads.upf.model.DTOs.funcionario.FuncionarioCreateDTO;
import ads.upf.model.DTOs.funcionario.FuncionarioResponseDTO;
import ads.upf.model.entities.Funcionario;
import ads.upf.model.entities.SenhaToken;
import ads.upf.model.enums.UsuarioRole;
import ads.upf.model.mappers.FuncionarioMapper;
import ads.upf.repositories.FuncionarioRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Inject
    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              TokenService tokenService,
                              EmailService emailService) {
        this.funcionarioRepository = funcionarioRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Transactional
    public void salvarFuncionario(FuncionarioCreateDTO funcionarioCreateDTO) {

        if (funcionarioCreateDTO.getId() == null) {

            checkFuncionarioExists(null,
                    funcionarioCreateDTO.getNomeCompleto(),
                    funcionarioCreateDTO.getEmail()
            );

            Funcionario funcionario = FuncionarioMapper.INSTANCE.toFuncionario(funcionarioCreateDTO);
            funcionario.setRole(UsuarioRole.atendente);
            funcionarioRepository.persist(funcionario);

            SenhaToken token = tokenService.criarToken(funcionario);
            emailService.enviarEmailSenha(token.getToken(), funcionario.getEmail());
        } else {
            checkFuncionarioExists(funcionarioCreateDTO.getId(),
                    funcionarioCreateDTO.getNomeCompleto(),
                    funcionarioCreateDTO.getEmail()
            );

            Funcionario funcionario = funcionarioRepository.findByIdOptional(
                    funcionarioCreateDTO.getId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Funcionário não encontrado.")
            );

            FuncionarioMapper.INSTANCE.updateFuncionarioFromDto(funcionarioCreateDTO, funcionario);
        }
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

    public List<FuncionarioResponseDTO> listarFuncionarios() {
        List<Funcionario> funcionarios = funcionarioRepository.listAll(Sort.by("nomeCompleto"));
        return FuncionarioMapper.INSTANCE.toFuncionarioDtoList(funcionarios);
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

    public Funcionario definirFuncionario(String email) {
        return funcionarioRepository.buscarPorEmail(email)
                .orElseThrow(
                        () -> new EntityNotFoundException("Funcionário não encontrado.")
                );
    }

    public Funcionario buscarPorEmail(String email) {
        return funcionarioRepository.buscarPorEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Funcionário não encontrado.")
        );
    }
}
