-- =============================================================================
-- SCRIPT DE POPULAÇÃO DE DADOS (IMPORT.SQL)
-- Compatível com PostgreSQL e a modelagem do Quarkus / Hibernate
-- Cobre: Ocupação, Giro, Tempo Médio, Faturamento, Inadimplência, Churn e Contratos
-- =============================================================================

BEGIN;

-- =========================================================================
-- Gerente 1: John Doe (Senha: john123)
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

-- -----------------------------------------------------------------------------
-- 1. VAGAS (Capacidade total: 25 vagas diversificadas)
-- -----------------------------------------------------------------------------
INSERT INTO vagas (id, nome, status, tipovaga, criado_em, criado_por) VALUES
    (101, 'A01', 'ocupada',       'comum', CURRENT_TIMESTAMP, 'seed'),
    (102, 'A02', 'ocupada',       'comum', CURRENT_TIMESTAMP, 'seed'),
    (103, 'A03', 'ocupada',       'comum', CURRENT_TIMESTAMP, 'seed'),
    (104, 'A04', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (105, 'A05', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (106, 'A06', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (107, 'A07', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (108, 'A08', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (109, 'A09', 'em_manutencao', 'comum', CURRENT_TIMESTAMP, 'seed'),
    (110, 'A10', 'em_manutencao', 'comum', CURRENT_TIMESTAMP, 'seed'),
    (111, 'B01', 'ocupada',       'comum', CURRENT_TIMESTAMP, 'seed'),
    (112, 'B02', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (113, 'B03', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (114, 'B04', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (115, 'B05', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed'),
    (116, 'M01', 'ocupada',       'moto',  CURRENT_TIMESTAMP, 'seed'),
    (117, 'M02', 'disponivel',    'moto',  CURRENT_TIMESTAMP, 'seed'),
    (118, 'M03', 'disponivel',    'moto',  CURRENT_TIMESTAMP, 'seed'),
    (119, 'M04', 'disponivel',    'moto',  CURRENT_TIMESTAMP, 'seed'),
    (120, 'M05', 'disponivel',    'moto',  CURRENT_TIMESTAMP, 'seed'),
    (121, 'I01', 'ocupada',       'idoso', CURRENT_TIMESTAMP, 'seed'),
    (122, 'I02', 'disponivel',    'idoso', CURRENT_TIMESTAMP, 'seed'),
    (123, 'P01', 'disponivel',    'pne',   CURRENT_TIMESTAMP, 'seed'),
    (124, 'P02', 'disponivel',    'pne',   CURRENT_TIMESTAMP, 'seed'),
    (125, 'V00', 'disponivel',    'comum', CURRENT_TIMESTAMP, 'seed')
ON CONFLICT (id) DO UPDATE SET 
    status = EXCLUDED.status, 
    tipovaga = EXCLUDED.tipovaga;

-- =============================================================================
-- 2. CLIENTES MENSALISTAS ADICIONAIS (COM CPF CRIPTOGRAFADO E BLIND INDEX)
-- =============================================================================

-- =========================================================================
-- Cliente 201: Ana Clara Vasconcelos (CPF original: 111.222.333-44)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (201, 'Ana Clara Vasconcelos', 'ana.vasconcelos@email.com', '54981112233');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (201, 'Xj38fS77zb3qUq4vLcvobnNGDenZSLDWq84/Rrz3JekHtz2d4zJmUN6G', 'uhMNFMEEtYI7c6gv9FHn8nS8bMsfHoWOOlMbIAS6vsA=', 'cliente');

-- =========================================================================
-- Cliente 202: Bruno Henrique Silveira (CPF original: 222.333.444-55)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (202, 'Bruno Henrique Silveira', 'bruno.silveira@email.com', '54982223344');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (202, 'd2ekuMPmOpK+A70vVItMNdO13FC845bz8LjF3vOYLLFkqt5SFi+n4GZR', 'q+5X83PPMDZ0zaUTaQ/WL+n1+B/lwfonXU7JSX87kwY=', 'cliente');

-- =========================================================================
-- Cliente 203: Camila Rodrigues Duarte (CPF original: 333.444.555-66)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (203, 'Camila Rodrigues Duarte', 'camila.duarte@email.com', '54983334455');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (203, 'hLRXkJqMgJNOGPoHSz4JMzlFEEQHDArZsP9DdblDOJkb7BjM6D03Hrml', 'n5mzcHmj86Q9ZhIYGdsn2Vwy0P9nN0WAm+o/3MAuBOg=', 'cliente');

-- =========================================================================
-- Cliente 204: Diego Fernandes Costa (CPF original: 444.555.666-77)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (204, 'Diego Fernandes Costa', 'diego.costa@email.com', '54984445566');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (204, 'SlNhdC8tcEuNZoZxSjjE070AGzn+WOzxP4VLezUP2aBC5O1wMIZtMgTr', 'nDT8yFa/3Qq+6ROSFS482apMZG+1SVywk86VGCVcIWk=', 'cliente');

-- =========================================================================
-- Cliente 205: Eduarda Martins Rios (CPF original: 555.666.777-88)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (205, 'Eduarda Martins Rios', 'eduarda.rios@email.com', '54985556677');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (205, 'syb5Q8/7MW6IacBWrWl+yUlTiVrGVyxDOgrjmXxAnyNh7CZ37/5Af2+F', 'O3QSO0RnONjei6CXXqKWPYD5gpo0slZAZniPEJGrkhs=', 'cliente');

-- =========================================================================
-- Cliente 206: Fabio Junior de Castro (CPF original: 666.777.888-99)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (206, 'Fabio Junior de Castro', 'fabio.castro@email.com', '54986667788');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (206, 'ljeoA4ABr9EfIjBPy2+b+AgU+5OEa2F03LPVhJJQ1OKmLHnG4WlpYofP', '1Fd9esX4eewrAki4/4LdfCHXbnZOVTqlgCtKGtcQrkA=', 'cliente');

-- =========================================================================
-- Cliente 207: Gabriela Meireles Santos (CPF original: 777.888.999-00)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (207, 'Gabriela Meireles Santos', 'gabriela.santos@email.com', '54987778899');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (207, 'RRJjcvYig39nQszFcit0QpIshDKCqvfnG6ICB94s2B8+jITwPWSDbFwv', 'E7hBdsM/x7cuifKsc9EAXZ6E7laHR0gx9IJCjwxidpc=', 'cliente');

-- =========================================================================
-- Cliente 208: Helio Ferreira Lima (CPF original: 888.999.000-11)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (208, 'Helio Ferreira Lima', 'helio.lima@email.com', '54988889900');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (208, 'DVaQzHNjLlQ9WUt78S2S+ogO6DOiXWbP/6WZF1nOz50odjyQe49HzR04', 'riXDZy3TbBtEqwZCFIkjYLfNTvwFH/tw49KxbQD9IJA=', 'cliente');

-- -----------------------------------------------------------------------------
-- 3. VEÍCULOS (Mensalistas e Rotativos)
-- -----------------------------------------------------------------------------
INSERT INTO veiculos (id, placa, marca, modelo, cor, tipo, criado_em, criado_por) VALUES
    -- Veículos de Mensalistas (201 a 208)
    (301, 'BRA1A11', 'Toyota',     'Corolla',      'Prata',    'carro', CURRENT_TIMESTAMP, 'seed'),
    (302, 'BRA2B22', 'Honda',      'Civic',        'Preto',    'carro', CURRENT_TIMESTAMP, 'seed'),
    (303, 'BRA3C33', 'Volkswagen', 'T-Cross',      'Branco',   'carro', CURRENT_TIMESTAMP, 'seed'),
    (304, 'BRA4D44', 'Jeep',       'Compass',      'Cinza',    'carro', CURRENT_TIMESTAMP, 'seed'),
    (305, 'BRA5E55', 'Hyundai',    'Creta',        'Vermelho', 'carro', CURRENT_TIMESTAMP, 'seed'),
    (306, 'BRA6F66', 'Yamaha',     'MT-03',        'Azul',     'moto',  CURRENT_TIMESTAMP, 'seed'),
    (307, 'BRA7G77', 'Renault',    'Master',       'Prata',    'van',   CURRENT_TIMESTAMP, 'seed'),
    (308, 'BRA8H88', 'Chevrolet',  'Onix',         'Preto',    'carro', CURRENT_TIMESTAMP, 'seed'),
    -- Veículos Avulsos / Rotativos frequentes
    (310, 'ROT1001', 'Fiat',       'Argo',         'Branco',   'carro', CURRENT_TIMESTAMP, 'seed'),
    (311, 'ROT1002', 'Ford',       'Ka',           'Vermelho', 'carro', CURRENT_TIMESTAMP, 'seed'),
    (312, 'ROT1003', 'Honda',      'HR-V',         'Prata',    'carro', CURRENT_TIMESTAMP, 'seed'),
    (313, 'ROT1004', 'Honda',      'CG 160 Titan', 'Vermelha', 'moto',  CURRENT_TIMESTAMP, 'seed'),
    (314, 'ROT1005', 'Volkswagen', 'Nivus',        'Cinza',    'carro', CURRENT_TIMESTAMP, 'seed'),
    (315, 'ROT1006', 'Toyota',     'Yaris',        'Azul',     'carro', CURRENT_TIMESTAMP, 'seed'),
    (316, 'ROT1007', 'Peugeot',    '208',          'Branco',   'carro', CURRENT_TIMESTAMP, 'seed'),
    (317, 'ROT1008', 'Renault',    'Kwid',         'Laranja',  'carro', CURRENT_TIMESTAMP, 'seed'),
    (318, 'ROT1009', 'Nissan',     'Kicks',        'Cinza',    'carro', CURRENT_TIMESTAMP, 'seed'),
    (319, 'ROT1010', 'Yamaha',     'Fazer 250',    'Preta',    'moto',  CURRENT_TIMESTAMP, 'seed')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 4. CONTRATOS (Cenários para Churn, Novos Contratos e Inadimplência)
-- -----------------------------------------------------------------------------
INSERT INTO contratos (id, datainicio, datatermino, valorcontratado, contratotipo, status, cliente_id, veiculo_id, criado_em, criado_por) VALUES
    -- Contratos Ativos de longa data (iniciados há 4 meses)
    (401, CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE + INTERVAL '245 days', 180.00, 'mensal', 'ativo',        201, 301, CURRENT_TIMESTAMP - INTERVAL '120 days', 'seed'),
    (402, CURRENT_DATE - INTERVAL '90 days',  CURRENT_DATE + INTERVAL '275 days', 180.00, 'mensal', 'ativo',        202, 302, CURRENT_TIMESTAMP - INTERVAL '90 days',  'seed'),
    
    -- Novos Contratos adquiridos no mês anterior (iniciados há ~35 dias)
    (403, CURRENT_DATE - INTERVAL '35 days',  CURRENT_DATE + INTERVAL '330 days', 180.00, 'mensal', 'ativo',        203, 303, CURRENT_TIMESTAMP - INTERVAL '35 days',  'seed'),
    (404, CURRENT_DATE - INTERVAL '30 days',  CURRENT_DATE + INTERVAL '335 days', 180.00, 'mensal', 'inadimplente', 204, 304, CURRENT_TIMESTAMP - INTERVAL '30 days',  'seed'),
    
    -- Novos Contratos adquiridos no mês corrente (iniciados nos últimos 15 dias)
    (405, CURRENT_DATE - INTERVAL '10 days',  CURRENT_DATE + INTERVAL '355 days', 180.00, 'mensal', 'ativo',        205, 305, CURRENT_TIMESTAMP - INTERVAL '10 days',  'seed'),
    (406, CURRENT_DATE - INTERVAL '5 days',   CURRENT_DATE + INTERVAL '360 days', 180.00, 'mensal', 'ativo',        206, 306, CURRENT_TIMESTAMP - INTERVAL '5 days',   'seed'),
    
    -- Contrato Cancelado no mês corrente (Churn deste mês)
    (407, CURRENT_DATE - INTERVAL '60 days',  CURRENT_DATE - INTERVAL '10 days',  180.00, 'mensal', 'cancelado',    207, 307, CURRENT_TIMESTAMP - INTERVAL '60 days',  'seed'),
    
    -- Contrato Encerrado / Não Renovado no mês anterior (Churn mês passado)
    (408, CURRENT_DATE - INTERVAL '180 days', CURRENT_DATE - INTERVAL '25 days',  180.00, 'mensal', 'encerrado',     208, 308, CURRENT_TIMESTAMP - INTERVAL '180 days', 'seed')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 5. PARCELAS DE CONTRATOS (Para KPIs Financeiros e Inadimplência)
-- -----------------------------------------------------------------------------
INSERT INTO parcelas (id, contrato_id, numeroparcela, datavencimento, datapagamento, valor, meiopagamento, status, cobrancaenviada, criado_em, criado_por) VALUES
    -- Contrato 401 (Cliente 201 - Totalmente em dia)
    (501, 401, 1, CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '92 days', 180.00, 'pix',    'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (502, 401, 2, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '60 days', 180.00, 'cartão', 'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (503, 401, 3, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '31 days', 180.00, 'pix',    'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (504, 401, 4, CURRENT_DATE + INTERVAL '10 days', NULL,                              180.00, NULL,     'pendente', true, CURRENT_TIMESTAMP, 'seed'),
    (505, 401, 5, CURRENT_DATE + INTERVAL '40 days', NULL,                              180.00, NULL,     'pendente', false,CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 402 (Cliente 202 - Em dia)
    (506, 402, 1, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '58 days', 180.00, 'pix',    'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (507, 402, 2, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '29 days', 180.00, 'cartão', 'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (508, 402, 3, CURRENT_DATE + INTERVAL '5 days',  NULL,                              180.00, NULL,     'pendente', true, CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 403 (Cliente 203 - Novo do mês passado, 1ª paga, 2ª pendente)
    (509, 403, 1, CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE - INTERVAL '25 days', 180.00, 'pix',    'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (510, 403, 2, CURRENT_DATE + INTERVAL '5 days',  NULL,                              180.00, NULL,     'pendente', true, CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 404 (Cliente 204 - Inadimplente: parcela vencida não paga)
    (511, 404, 1, CURRENT_DATE - INTERVAL '20 days', NULL,                              180.00, NULL,     'atrasada', true, CURRENT_TIMESTAMP, 'seed'),
    (512, 404, 2, CURRENT_DATE + INTERVAL '10 days', NULL,                              180.00, NULL,     'pendente', false,CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 405 (Cliente 205 - Novo do mês atual)
    (513, 405, 1, CURRENT_DATE + INTERVAL '20 days', NULL,                              180.00, NULL,     'pendente', true, CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 406 (Cliente 206 - Novo do mês atual)
    (514, 406, 1, CURRENT_DATE + INTERVAL '25 days', NULL,                              180.00, NULL,     'pendente', true, CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 407 (Cliente 207 - Cancelado com 1 paga)
    (515, 407, 1, CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '49 days', 180.00, 'cartão', 'paga',     true, CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 408 (Cliente 208 - Encerrado / Histórico)
    (516, 408, 1, CURRENT_DATE - INTERVAL '150 days', CURRENT_DATE - INTERVAL '150 days', 180.00, 'pix',  'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (517, 408, 2, CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '119 days', 180.00, 'pix',  'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (518, 408, 3, CURRENT_DATE - INTERVAL '90 days',  CURRENT_DATE - INTERVAL '90 days',  180.00, 'pix',  'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (519, 408, 4, CURRENT_DATE - INTERVAL '60 days',  CURRENT_DATE - INTERVAL '58 days',  180.00, 'pix',  'paga',     true, CURRENT_TIMESTAMP, 'seed')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 6. PERMANÊNCIAS (Histórico para Giro, Tempo Médio, Tolerância e Tempo Real)
-- -----------------------------------------------------------------------------
INSERT INTO permanencias (id, dataentrada, datasaida, status, veiculo_id, vaga_id, isrotativo, criado_em, criado_por) VALUES
    -- HISTÓRICO DE ONTEM (Fluxo concluído)
    -- Caso 1: Tolerância / Desistência (saída em 12 minutos - sem pagamento)
    (601, CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 3 hours 48 minutes', 'concluida', 310, 101, true, CURRENT_TIMESTAMP, 'seed'),
    
    -- Caso 2: Curta duração (45 minutos)
    (602, CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 15 minutes', 'concluida', 311, 102, true, CURRENT_TIMESTAMP, 'seed'),
    
    -- Caso 3: Média duração (1h 30min)
    (603, CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 30 minutes', 'concluida', 312, 103, true, CURRENT_TIMESTAMP, 'seed'),
    
    -- Caso 4: Longa duração (3h 40min)
    (604, CURRENT_TIMESTAMP - INTERVAL '1 day 8 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 20 minutes', 'concluida', 314, 104, true, CURRENT_TIMESTAMP, 'seed'),
    
    -- Caso 5: Moto rotativa (2h)
    (605, CURRENT_TIMESTAMP - INTERVAL '1 day 7 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours',            'concluida', 313, 116, true, CURRENT_TIMESTAMP, 'seed'),
    
    -- Caso 6: Mensalista usando o pátio ontem (não gera cobrança avulsa)
    (606, CURRENT_TIMESTAMP - INTERVAL '1 day 9 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour',             'concluida', 301, 101, false,CURRENT_TIMESTAMP, 'seed'),

    -- FLUXO DE HOJE - CONCLUÍDAS (Giro matutino)
    (607, CURRENT_DATE + TIME '07:30:00', CURRENT_DATE + TIME '08:45:00', 'concluida', 315, 101, true, CURRENT_TIMESTAMP, 'seed'),
    (608, CURRENT_DATE + TIME '08:00:00', CURRENT_DATE + TIME '10:15:00', 'concluida', 316, 102, true, CURRENT_TIMESTAMP, 'seed'),
    (609, CURRENT_DATE + TIME '08:15:00', CURRENT_DATE + TIME '08:24:00', 'concluida', 317, 103, true, CURRENT_TIMESTAMP, 'seed'), -- Tolerância (9 min)
    (610, CURRENT_DATE + TIME '09:00:00', CURRENT_DATE + TIME '10:45:00', 'concluida', 318, 104, true, CURRENT_TIMESTAMP, 'seed'),

    -- FLUXO DE HOJE - EM ANDAMENTO (Para testar Ocupação em Tempo Real e Vagas Ocupadas)
    (611, CURRENT_TIMESTAMP - INTERVAL '2 hours 15 minutes', NULL, 'em_andamento', 310, 101, true, CURRENT_TIMESTAMP, 'seed'), -- Carro avulso na A01
    (612, CURRENT_TIMESTAMP - INTERVAL '1 hour 40 minutes',  NULL, 'em_andamento', 311, 102, true, CURRENT_TIMESTAMP, 'seed'), -- Carro avulso na A02
    (613, CURRENT_TIMESTAMP - INTERVAL '45 minutes',         NULL, 'em_andamento', 302, 103, false,CURRENT_TIMESTAMP, 'seed'), -- Mensalista na A03
    (614, CURRENT_TIMESTAMP - INTERVAL '3 hours 10 minutes', NULL, 'em_andamento', 312, 111, true, CURRENT_TIMESTAMP, 'seed'), -- Carro avulso na B01
    (615, CURRENT_TIMESTAMP - INTERVAL '1 hour 10 minutes',  NULL, 'em_andamento', 319, 116, true, CURRENT_TIMESTAMP, 'seed'), -- Moto na M01
    (616, CURRENT_TIMESTAMP - INTERVAL '25 hours',           NULL, 'em_andamento', 303, 121, false,CURRENT_TIMESTAMP, 'seed')  -- Estadia excessiva (> 24h) na I01
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 7. PAGAMENTOS DE PERMANÊNCIAS (Para Ticket Médio, Receita Rotativa e Formas de Pagto)
-- -----------------------------------------------------------------------------
INSERT INTO pagamentos (id, valor, datapagamento, status, meiopagamento, permanencia_id, criado_em, criado_por) VALUES
    -- Pagamento da Permanência 602 (45 min = 1 hora cheia -> R$ 70,00)
    (701, 70.00,  CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 15 minutes', 'pago', 'pix',      602, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 603 (1h 30 min = 1h + fração -> R$ 105.00)
    (702, 105.00, CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 30 minutes', 'pago', 'cartao',   603, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 604 (3h 40 min = 3h + fração -> R$ 245.00)
    (703, 245.00, CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 20 minutes', 'pago', 'dinheiro', 604, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 605 (Moto 2h -> R$ 140.00)
    (704, 140.00, CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours',            'pago', 'pix',      605, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 607 de hoje (1h 15 min -> R$ 105.00)
    (705, 105.00, CURRENT_DATE + TIME '08:45:00',                          'pago', 'pix',      607, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 608 de hoje (2h 15 min -> R$ 175.00)
    (706, 175.00, CURRENT_DATE + TIME '10:15:00',                          'pago', 'cartao',   608, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 610 de hoje (1h 45 min -> R$ 140.00)
    (707, 140.00, CURRENT_DATE + TIME '10:45:00',                          'pago', 'dinheiro', 610, CURRENT_TIMESTAMP, 'seed')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 8. VINCULAÇÃO BIDIRECIONAL (permanencias.pagamento_id)
-- -----------------------------------------------------------------------------
UPDATE permanencias SET pagamento_id = 701 WHERE id = 602;
UPDATE permanencias SET pagamento_id = 702 WHERE id = 603;
UPDATE permanencias SET pagamento_id = 703 WHERE id = 604;
UPDATE permanencias SET pagamento_id = 704 WHERE id = 605;
UPDATE permanencias SET pagamento_id = 705 WHERE id = 607;
UPDATE permanencias SET pagamento_id = 706 WHERE id = 608;
UPDATE permanencias SET pagamento_id = 707 WHERE id = 610;

-- Atualizar sequências do PostgreSQL para evitar conflitos futuros com novos inserts pela aplicação
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), COALESCE(MAX(id), 1)) FROM usuarios;
SELECT setval(pg_get_serial_sequence('vagas', 'id'), COALESCE(MAX(id), 1)) FROM vagas;
SELECT setval(pg_get_serial_sequence('veiculos', 'id'), COALESCE(MAX(id), 1)) FROM veiculos;
SELECT setval(pg_get_serial_sequence('contratos', 'id'), COALESCE(MAX(id), 1)) FROM contratos;
SELECT setval(pg_get_serial_sequence('parcelas', 'id'), COALESCE(MAX(id), 1)) FROM parcelas;
SELECT setval(pg_get_serial_sequence('permanencias', 'id'), COALESCE(MAX(id), 1)) FROM permanencias;
SELECT setval(pg_get_serial_sequence('pagamentos', 'id'), COALESCE(MAX(id), 1)) FROM pagamentos;

COMMIT;
