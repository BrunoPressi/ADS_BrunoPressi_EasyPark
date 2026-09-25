INSERT INTO vagas (id, nome, status, tipoVaga) VALUES (9997, 'V01', 'disponivel', 'comum');
INSERT INTO vagas (id, nome, status, tipoVaga) VALUES (9998, 'V02', 'em_manutencao', 'idoso');
INSERT INTO vagas (id, nome, status, tipoVaga) VALUES (9999, 'V03', 'ocupada', 'moto');

-- =========================================================================
-- Funcionário 1: John Doe (Senha: john123)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (9997, 'John Doe', 'John@email.com', '54996322831');

INSERT INTO funcionarios (usuarioId, senha, role)
VALUES (9997,'$2a$12$p9/pzh1.aeR7HOxNql0UdOC1GgfdHTzOeynhaGfHNPPQxMg/YKE4u', 'gerente');

-- =========================================================================
-- Funcionário 2: Alex Green (Senha: alex123)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (9998, 'Alex Green', 'Alex@email.com', '54999726854');

INSERT INTO funcionarios (usuarioId, senha, role)
VALUES (9998,'$2a$12$dT/KMvGXDfgag7CvOjDcme/hXA6LrZtGBml0IBLVa3UKyBJ9Sb7NK', 'atendente');

-- =========================================================================
-- Cliente 1: Carlos Eduardo Silva (CPF original: 123.456.789-09)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (9995, 'Carlos Eduardo Silva', 'carlos.silva@email.com', '54991234567');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (9995, 'pj+YtG62ko/P9bTBkTU7GxLMl7LUsIDMkB4mXF8Ct68weN+hB7B+5vKj', 'Fl20s0l3r42YaMwRs9PiijIGvVp+vZOzBJPRMl5/lsw=', 'cliente');


-- =========================================================================
-- Cliente 2: Mariana Souza Lima (CPF original: 987.654.321-00)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (9996, 'Mariana Souza Lima', 'mariana.lima@email.com', '54998765432');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (9996, 'D1D5mAQ7qS0xO+pW4M9j2cErumN5k5ScNOGBKtRkGMUUgtubUQvFhT5+', 'J1okrJglqlleHhvBxJWpKNFN7ZmqtC7dsfwQY8SErvU=', 'cliente');