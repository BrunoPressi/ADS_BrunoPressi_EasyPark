insert into vagas (id, nome, status) values (9997, 'V01', 'disponivel');
insert into vagas (id, nome, status) values (9998, 'V02', 'em_manutencao');
insert into vagas (id, nome, status) values (9999, 'V03', 'ocupada');

insert into usuarios (id, nomeCompleto, email, telefone, dataNascimento)
    values (9997, 'John Doe', 'John@email.com', '54996322831', '03/06/2005');

insert into funcionarios (usuarioId, senha, role) values (9997,'$2a$12$p9/pzh1.aeR7HOxNql0UdOC1GgfdHTzOeynhaGfHNPPQxMg/YKE4u', 'gerente');

insert into usuarios (id, nomeCompleto, email, telefone, dataNascimento)
values (9998, 'Alex Green', 'Alex@email.com', '54999726854', '23/11/2003');

insert into funcionarios (usuarioId, senha, role) values (9998,'$2a$12$dT/KMvGXDfgag7CvOjDcme/hXA6LrZtGBml0IBLVa3UKyBJ9Sb7NK', 'atendente');