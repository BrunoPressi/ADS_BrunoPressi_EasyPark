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
-- Cliente 1: Carlos Eduardo Silva (CPF original: 179.253.352-70)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (9995, 'Carlos Eduardo Silva', 'carlos.silva@email.com', '54991234567');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (9995, 'mO2A9htsuP4y/tte3q9L0gGQePoh22ZaurRearkl+jAyYqbGXfgp', 'anjl8Q0juNi/3emb2WIgCXLJcbjYuNcO+i3otvPoPN0=', 'cliente');

-- =========================================================================
-- Cliente 2: Mariana Souza Lima (CPF original: 179.364.218-40)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (9996, 'Mariana Souza Lima', 'mariana.lima@email.com', '54998765432');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (9996, 'WaniAjOHXsOSQ4DkRfgCSdqgMHV5abSl8nQf2IWCdJSWFEYXVr4Y', 'O/yb7WuWUaMqxVFLh8HhJRnT15zVlSxZRK2RJrlk22E=', 'cliente');

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
-- Cliente 201: Ana Clara Vasconcelos (CPF original: 101.900.560-29)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (201, 'Ana Clara Vasconcelos', 'ana.vasconcelos@email.com', '54996322831');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (201, 'LdAqVux+S311UC8DEcUsTaRlZ2ppEsG5eJtbaE/QHOUavEk08yoP', 'v2w5ZnoP4Cs0mu4heQMlz0sdtr0V5j6jsFQ7G1Nb8+E=', 'cliente');

-- =========================================================================
-- Cliente 202: Bruno Henrique Silveira (CPF original: 102.011.426-60)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (202, 'Bruno Henrique Silveira', 'bruno.silveira@email.com', '54982223344');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (202, 'mhCM5sVyfOhPwBtQlZnrUp4inadv7M0FyJp05retI1ziaankkpot', 'g6j9vI8nUbODOV1cJy7kwVIo6BftqtmdLe3PWW1pIyI=', 'cliente');

-- =========================================================================
-- Cliente 203: Camila Rodrigues Duarte (CPF original: 102.122.292-57)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (203, 'Camila Rodrigues Duarte', 'camila.duarte@email.com', '54983334455');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (203, 'YkDqhQvPBfYmIguuEGkxyROki2iSoN3wt5TKJmYBUdE9Iw84WUmF', 'kduR264XguGWHVZPhSIpaQQMYDM1NGXZnQOcePTeQ+w=', 'cliente');

-- =========================================================================
-- Cliente 204: Diego Fernandes Costa (CPF original: 102.233.158-26)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (204, 'Diego Fernandes Costa', 'diego.costa@email.com', '54984445566');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (204, 'Xpe9gQ/4uIAqW+01egtXGyMYWfimUj5rJDKRx6GlnMgUjon8j0cK', 'L/a6cxVykTHD0LEnrbZdOnrw2rSBLVF+Aqghc1gsc4U=', 'cliente');

-- =========================================================================
-- Cliente 205: Eduarda Martins Rios (CPF original: 102.344.024-58)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (205, 'Eduarda Martins Rios', 'eduarda.rios@email.com', '54985556677');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (205, 'Uj1xJu9iPJXxZpETJAlf25w8I13dMh+UFgmP+XtngLLV8EII1mT9', 'qrE1i/VFJSxyy1A8rDq9Xxcty7YINHgC3dMVgTIbois=', 'cliente');

-- =========================================================================
-- Cliente 206: Fabio Junior de Castro (CPF original: 102.454.890-29)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (206, 'Fabio Junior de Castro', 'fabio.castro@email.com', '54986667788');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (206, 'jj+pzvsS5w03r7Lu0ZtsiLif2I7/JX2dNuZ/HtejELFcq4K/+v62', 'Aga6my9argdIIvO18uIbuPCpVxsdZeeOFF5Q92vG9Kw=', 'cliente');

-- =========================================================================
-- Cliente 207: Gabriela Meireles Santos (CPF original: 102.565.756-06)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (207, 'Gabriela Meireles Santos', 'gabriela.santos@email.com', '54987778899');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (207, 'gXuGmXfW8YxzKRAHax34vZBThsGL6c/q3YShjiFUh08z/kJcwpxP', 'cSqAoJOc1o1TglFp7Nl6Yl0iWjbDXMsV1aZLwTJrkrI=', 'cliente');

-- =========================================================================
-- Cliente 208: Helio Ferreira Lima (CPF original: 102.676.622-20)
-- =========================================================================
INSERT INTO usuarios (id, nomeCompleto, email, telefone)
VALUES (208, 'Helio Ferreira Lima', 'helio.lima@email.com', '54988889900');

INSERT INTO clientes (usuarioId, cpf, cpfHash, role)
VALUES (208, 'c29HANDGRUPJBJWuWD2GOtRckaH6aTdgfrPCssHHQJZDeBzRBMZb', 'uEZgW9sBjpvjU1UGxlSDIJI5r6aocAcQ3Vf+/J/v6R8=', 'cliente');

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
    -- As datas de inicio sao calculadas para que uma parcela de cada contrato (401 a 405) venca em
    -- exatamente 7 dias (CURRENT_DATE + 7), a janela usada pelo CobrancaService para cobrar no Asaas.
    -- Contrato 401: inicio ha ~4 meses (parcela 4 vence em 7 dias)
    (401, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '4 months')::date, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '4 months' + INTERVAL '12 months')::date, 180.00, 'mensal', 'ativo',        201, 301, CURRENT_TIMESTAMP - INTERVAL '120 days', 'seed'),
    -- Contrato 402: inicio ha ~3 meses (parcela 3 vence em 7 dias)
    (402, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '3 months')::date, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '3 months' + INTERVAL '12 months')::date, 180.00, 'mensal', 'ativo',        202, 302, CURRENT_TIMESTAMP - INTERVAL '90 days',  'seed'),

    -- Novos Contratos adquiridos no mes anterior (parcela 2 vence em 7 dias)
    (403, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '2 months')::date, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '2 months' + INTERVAL '12 months')::date, 180.00, 'mensal', 'ativo',        203, 303, CURRENT_TIMESTAMP - INTERVAL '53 days',  'seed'),
    -- Contrato 404 inadimplente: parcela 1 vencida e nao paga (atrasada), parcela 2 vence em 7 dias
    (404, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '2 months')::date, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '2 months' + INTERVAL '12 months')::date, 180.00, 'mensal', 'inadimplente', 204, 304, CURRENT_TIMESTAMP - INTERVAL '53 days',  'seed'),

    -- Novos Contratos adquiridos no mes corrente
    -- Contrato 405: parcela 1 vence em 7 dias
    (405, (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '1 month')::date,  (CURRENT_DATE + INTERVAL '7 days' - INTERVAL '1 month' + INTERVAL '12 months')::date,  180.00, 'mensal', 'ativo',        205, 305, CURRENT_TIMESTAMP - INTERVAL '23 days',  'seed'),
    -- Contrato 406 (controle): parcela 1 vence em ~25 dias, nao deve ser cobrada ainda
    (406, CURRENT_DATE - INTERVAL '5 days',   (CURRENT_DATE - INTERVAL '5 days' + INTERVAL '12 months')::date, 180.00, 'mensal', 'ativo',        206, 306, CURRENT_TIMESTAMP - INTERVAL '5 days',   'seed'),

    -- Contrato Cancelado no mes corrente (Churn deste mes)
    (407, CURRENT_DATE - INTERVAL '60 days',  CURRENT_DATE - INTERVAL '10 days',  180.00, 'mensal', 'cancelado',    207, 307, CURRENT_TIMESTAMP - INTERVAL '60 days',  'seed'),

    -- Contrato Encerrado / Nao Renovado no mes anterior (Churn mes passado)
    (408, CURRENT_DATE - INTERVAL '180 days', CURRENT_DATE - INTERVAL '25 days',  180.00, 'mensal', 'encerrado',     208, 308, CURRENT_TIMESTAMP - INTERVAL '180 days', 'seed')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 5. PARCELAS DE CONTRATOS (Para KPIs Financeiros, Inadimplencia e teste do Asaas)
-- Gera 1 parcela por mes (12 por contrato) para os contratos 401 a 406, seguindo o
-- ParcelaService.gerarParcelas (vencimento = datainicio + N meses). Regras de status:
--   * vencida e contrato nao inadimplente  -> 'paga' (cobrancaenviada = true)
--   * vencida do contrato 404 (1a parcela) -> 'atrasada'
--   * demais                               -> 'pendente' (cobrancaenviada = false, sem asaasPaymentId)
-- TESTE ASAAS: as parcelas que vencem em exatamente 7 dias (401/4, 402/3, 403/2, 404/2 e 405/1)
-- sao selecionadas pelo CobrancaService (startup + cron 02:00), que cria cobranca no sandbox
-- (gera asaasPaymentId e invoiceUrl). Os clientes nao possuem asaasCustomerId: ele sera criado
-- no Asaas na primeira cobranca. As demais parcelas pendentes sao controle (nao devem ser cobradas).
-- IDs das parcelas: contrato_id * 100 + numeroparcela (ex.: 40104)
-- -----------------------------------------------------------------------------
INSERT INTO parcelas (id, contrato_id, numeroparcela, datavencimento, datapagamento, valor, meiopagamento, status, cobrancaenviada, criado_em, criado_por)
SELECT
    c.id * 100 + g.n,
    c.id,
    g.n,
    (c.datainicio + make_interval(months => g.n))::date,
    CASE WHEN (c.datainicio + make_interval(months => g.n))::date < CURRENT_DATE AND c.id <> 404
         THEN (c.datainicio + make_interval(months => g.n))::date END,
    180.00,
    CASE WHEN (c.datainicio + make_interval(months => g.n))::date < CURRENT_DATE AND c.id <> 404
         THEN (ARRAY['PIX','CREDIT_CARD','DEBIT_CARD'])[1 + (g.n % 3)] END,
    CASE WHEN (c.datainicio + make_interval(months => g.n))::date >= CURRENT_DATE THEN 'pendente'
         WHEN c.id = 404 THEN 'atrasada'
         ELSE 'paga' END,
    (c.datainicio + make_interval(months => g.n))::date < CURRENT_DATE AND c.id <> 404,
    CURRENT_TIMESTAMP,
    'seed'
FROM contratos c
CROSS JOIN generate_series(1, 12) AS g(n)
WHERE c.id BETWEEN 401 AND 406
ON CONFLICT (id) DO NOTHING;

INSERT INTO parcelas (id, contrato_id, numeroparcela, datavencimento, datapagamento, valor, meiopagamento, status, cobrancaenviada, criado_em, criado_por) VALUES
    -- Contrato 407 (Cliente 207 - Cancelado com 1 paga)
    (515, 407, 1, CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '49 days', 180.00, 'DEBIT_CARD', 'paga',     true, CURRENT_TIMESTAMP, 'seed'),

    -- Contrato 408 (Cliente 208 - Encerrado / Historico)
    (516, 408, 1, CURRENT_DATE - INTERVAL '150 days', CURRENT_DATE - INTERVAL '150 days', 180.00, 'PIX',  'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (517, 408, 2, CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '119 days', 180.00, 'PIX',  'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (518, 408, 3, CURRENT_DATE - INTERVAL '90 days',  CURRENT_DATE - INTERVAL '90 days',  180.00, 'PIX',  'paga',     true, CURRENT_TIMESTAMP, 'seed'),
    (519, 408, 4, CURRENT_DATE - INTERVAL '60 days',  CURRENT_DATE - INTERVAL '58 days',  180.00, 'PIX',  'paga',     true, CURRENT_TIMESTAMP, 'seed')
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
    (701, 70.00,  CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 15 minutes', 'pago', 'PIX',      602, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 603 (1h 30 min = 1h + fração -> R$ 105.00)
    (702, 105.00, CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 30 minutes', 'pago', 'CREDIT_CARD',   603, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 604 (3h 40 min = 3h + fração -> R$ 245.00)
    (703, 245.00, CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours 20 minutes', 'pago', 'DINHEIRO', 604, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 605 (Moto 2h -> R$ 140.00)
    (704, 140.00, CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours',            'pago', 'PIX',      605, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 607 de hoje (1h 15 min -> R$ 105.00)
    (705, 105.00, CURRENT_DATE + TIME '08:45:00',                          'pago', 'PIX',      607, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 608 de hoje (2h 15 min -> R$ 175.00)
    (706, 175.00, CURRENT_DATE + TIME '10:15:00',                          'pago', 'CREDIT_CARD',   608, CURRENT_TIMESTAMP, 'seed'),
    
    -- Pagamento da Permanência 610 de hoje (1h 45 min -> R$ 140.00)
    (707, 140.00, CURRENT_DATE + TIME '10:45:00',                          'pago', 'DINHEIRO', 610, CURRENT_TIMESTAMP, 'seed')
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
