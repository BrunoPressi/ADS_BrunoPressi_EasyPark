# Planejamento Estratégico de Apresentação — EasyPark

> **Formato do Seminário:** 15 minutos totais (1 min Contextualização + 1 min Arquitetura + 10 min Demonstração Prática + 3 min Perguntas).
>
> **Base técnica analisada:** [DVP_EasyPark.pdf](DVP_EasyPark.pdf), [UseCaseDiagram_EasyPark.png](astah/UseCaseDiagram_EasyPark.png), [Logico_Final.png](modelagem-dados/Logico_Final.png), [pom.xml](../pom.xml), [import.sql](../src/main/resources/import.sql) e código-fonte em Quarkus.

---

## 1. Visão Geral da Estratégia Recomendada

A distribuição imposta pelo professor (**1 min contexto + 1 min arquitetura + 10 min demo + 3 min perguntas**) deixa uma mensagem clara: **os slides são apenas um mapa de 2 minutos; a nota será decidida nos 10 minutos de software rodando**.

Como o EasyPark é um sistema robusto (11 requisitos funcionais, 2 perfis segregados por rota, integração real com gateway Asaas via API + Webhook, criptografia de CPF com *blind index*, dashboard de KPIs e relatórios PDF/CSV), o maior erro seria tentar fazer um "tour de CRUDs" mostrando todas as telas superficialmente.

**Estratégia central — "Do Pátio ao Caixa e à Diretoria":**
1. **Slides ultracurtos (4 slides no máximo)** para cravar os 2 minutos iniciais sem estourar o relógio.
2. **Demonstração orientada por narrativa contínua** usando os dados já semeados em [import.sql](../src/main/resources/import.sql), dividindo os 10 minutos em **2 atos**:
   - **Ato 1 — Operação no Pátio (Perfil Atendente, ~4 min):** provar o *core business* (entrada, alocação automática de vaga, distinção automática rotativo vs. mensalista, cálculo no servidor, pagamento e comprovante).
   - **Ato 2 — Gestão, Recorrência e Inteligência (Perfil Gerente, ~6 min):** provar o diferencial técnico do projeto (contrato mensal + integração Asaas/Webhook, gestão de vagas/tarifas, Dashboard com os reflexos em tempo real do Ato 1 e exportação de relatório PDF).
3. **Zero desperdício com cadastros triviais:** validações de CPF, CRUD básico de funcionários e edição simples de clientes só entram como *menção rápida* ou *efeito colateral* de um fluxo maior (ex.: criar cliente/veículo dentro do próprio diálogo de contrato, ou mostrar rapidamente a lista já populada).

---

## 2. Estrutura Sugerida dos Slides (2 minutos exatos)

> **Regra de ouro:** não ultrapasse **4 slides** (excluindo uma tela de agradecimento opcional para ficar projetada nas perguntas). Mais slides do que isso garantem que você estoure os 2 minutos.

| Slide | Tempo | Objetivo | Conteúdo Visual Obrigatório |
|---|---:|---|---|
| **1. Capa + Problema e Proposta** | **0:40** | Situar o avaliador no domínio e na dor real. | Título (*EasyPark — Gestão e Controle de Estacionamentos*), seu nome. Em 3 bullets curtos: **Dor** (controle manual/planilhas → erros de cobrança, filas, falta de visibilidade de vagas e caixa) → **Solução** (aplicação web completa que integra operação de pátio, contratos recorrentes com gateway real e inteligência gerencial). |
| **2. Visão Funcional (Casos de Uso)** | **0:20** | Mostrar o escopo e a segregação de perfis sem ler bolha por bolha. | Imagem limpa de [UseCaseDiagram_EasyPark.png](astah/UseCaseDiagram_EasyPark.png) destacando visualmente os 2 blocos: **Operação (Funcionário)** vs. **Gestão (Gerente)** + ponto comum (**Realizar Login**). |
| **3. Arquitetura e Stack Tecnológica** | **0:30** | Demonstrar maturidade de engenharia em um único golpe de vista. | Diagrama em camadas simplificado (extraído da pág. 15 do DVP): `Apresentação (JSF/PrimeFaces)` → `Serviço/UseCases (CDI)` → `Repositórios (Panache)` → `PostgreSQL 15`. Ao lado, selos/blocos de destaque: **Java 21 + Quarkus 3.39**, **Elytron JDBC (BCrypt + RBAC)**, **Asaas API v3 + Webhook (Ngrok)**, **OpenPDF** e **18 suítes de testes (JUnit 5/Mockito)**. |
| **4. Modelagem de Dados (Modelo Lógico)** | **0:30** | Evidenciar consistência relacional e decisões de segurança no banco. | Imagem legível de [Logico_Final.png](modelagem-dados/Logico_Final.png) com **3 círculos/setas de destaque** sobrepostos no slide: (1) Herança `Usuario` → `Funcionario`/`Cliente` (com `cpf` criptografado + `cpf_hash`); (2) Núcleo operacional `Veiculo`–`Permanencia`–`Vaga`–`Pagamento`; (3) Núcleo recorrente `Contrato`–`Parcela` (com `asaas_payment_id` e `invoice_url`). |
| *(Transição)* | **2:00** | Fechar slides e abrir o navegador já posicionado no sistema. | — |

---

## 3. Roteiro Detalhado da Demonstração Prática (10 minutos)

### Preparação Pré-Apresentação (faça antes de começar a contar o tempo)
- Suba o banco PostgreSQL e o Quarkus **antes** da sua vez (lembre-se de que [CobrancaService.java](../src/main/java/ads/upf/services/CobrancaService.java) executa a rotina de cobrança no Asaas no `StartupEvent`, então iniciar o app na frente da banca gera risco de lentidão).
- Deixe **2 janelas do navegador** abertas lado a lado ou em abas/perfis distintos:
  - **Janela Normal:** na tela `/login.xhtml` (para fazer o login real ao vivo).
  - **Janela Anônima:** já pronta para logar como Gerente (ou vice-versa), evitando perder tempo deslogando/logando várias vezes.
- Deixe o painel **Asaas Sandbox** aberto em uma aba separada (para provar a cobrança criada/sincronizada).
- Tenha um **bloco de notas discreto** (ou colinha no papel) com as placas e credenciais do [import.sql](../src/main/resources/import.sql):
  - Atendente: `Alex@email.com` / `alex123`
  - Gerente: `John@email.com` / `john123`
  - Placa Mensalista (fora do pátio): `BRA1A11` (Contrato 401 ativo)
  - Placa Mensalista (já no pátio na vaga `A03`): `BRA2B22`
  - Placa Rotativa (já no pátio na vaga `A01`, há ~2h15min): `ROT1001`
  - Placa Nova Rotativa (para entrada ao vivo): ex. `ABC1D23`

### Sequência Cronometrada dos 10 Minutos

| Bloco | Tempo | Ação no Sistema | O que Falar e Evidenciar na Tela |
|---|---:|---|---|
| **1. Autenticação e Segregação de Rotas (RF01 / RNF04-05)** | **0:45** | Faça login como **Atendente** (`Alex@email.com`). Rapidamente, tente digitar `/pages/gestao/dashboard.xhtml` na URL para mostrar o bloqueio (HTTP 403 / restrição de perfil). Volte para `/pages/operacao/fluxo.xhtml`. | Evidencie que a segurança não é apenas "esconder botão no menu", mas políticas de autorização por rota no Quarkus Elytron JDBC com senha em BCrypt. |
| **2. Entrada de Veículos: Rotativo vs. Mensalista (RF04)** | **1:30** | **Teste A (Validação):** digite uma placa inválida rapidamente para mostrar o `PlacaValidator` (Mercosul/antigo) barrando o envio.<br>**Teste B (Rotativo):** registre entrada da placa `ABC1D23` (Carro, Vaga Comum). Mostre a vaga sendo alocada automaticamente e marcada como *Rotativo*.<br>**Teste C (Mensalista):** registre entrada da placa `BRA1A11`. Mostre que o sistema consulta contratos ativos ([EntradaUseCase.java](../src/main/java/ads/upf/services/EntradaUseCase.java)) e classifica automaticamente como **Mensalista** sem o atendente precisar escolher. | Prove que a regra de negócio protege contra erro humano: não deixa duplicar placa já no pátio, aloca vaga disponível do tipo escolhido e distingue mensalista automaticamente. |
| **3. Saída, Cálculo Automático, Pagamento e Comprovante (RF04, RF07, RF08, RF09)** | **2:00** | **Teste A (Saída Mensalista):** dê saída na placa `BRA2B22` (já no pátio na `A03`). Mostre que finaliza **sem gerar cobrança** e libera a vaga.<br>**Teste B (Saída Rotativo):** dê saída na placa `ROT1001` (já está há ~2h15min na `A01`, gerando valor maior que a 1ª hora cheia: R$ 70 + frações de 30 min de R$ 35). Selecione um meio de pagamento (ex.: PIX), confirme e exiba o **Comprovante em tela** com memória de cálculo.<br>*Dica:* explique em 1 frase que o cálculo é refeito no servidor ([SaidaUseCase.java](../src/main/java/ads/upf/services/SaidaUseCase.java)), ignorando qualquer valor vindo do front-end. | **Ponto alto do Ato 1.** Usar `ROT1001` (que já tem > 2h no `import.sql`) é muito mais convincente do que dar saída em um carro que acabou de entrar há 10 segundos, pois evidencia a fórmula real de `1ª hora + frações de 30 min`. |
| **4. Gestão de Contratos e Integração Asaas + Webhook (RF06, RF08)** | **2:30** | Troque para a janela do **Gerente** (`John@email.com`). Acesse **Contratos** (`/pages/gestao/contratos.xhtml`).<br>1. Mostre a listagem paginada com contratos ativos, inadimplente (`Contrato 404`) e cancelado (`Contrato 407`).<br>2. Abra as **parcelas** de um contrato cobrado no startup (ex.: Contrato 401 — parcela 4) e mostre o `asaasPaymentId` e o link `invoiceUrl` gerado na janela de 7 dias.<br>3. *(Se a internet/Ngrok estiver ok)*: simule/confirme o pagamento de uma parcela no **Asaas Sandbox** e recarregue a tela mostrando o status mudar para **Paga** via Webhook ([AsaasWebhookResource.java](../src/main/java/ads/upf/integration/asaas/AsaasWebhookResource.java)).<br>4. Mostre rapidamente o modal de **Novo Contrato** (destacando a busca por CPF via *blind index* HMAC e a opção de cadastrar cliente/veículo no mesmo fluxo) ou tente editar/cancelar o contrato inadimplente para evidenciar a proteção da regra de negócio. | **Ponto alto técnico do projeto.** Prova integração real de ponta a ponta (REST Client + Scheduler + Webhook autenticado por token), indo muito além de um CRUD acadêmico comum. |
| **5. Gestão Operacional Rápida: Vagas e Tarifas (RF05, RF07)** | **1:00** | Passe brevemente em **Vagas** (`/pages/gestao/vagas.xhtml`): mostre os status (`disponivel`, `ocupada`, `em_manutencao` como `A09`/`A10`) e que vagas ocupadas não permitem edição.<br>Passe em **Tarifas** (`/pages/gestao/tarifas.xhtml`): mostre a tarifa ativa (R$ 70 / R$ 35) e a regra de valor mínimo configurável (R$ 45). | Fecha o ciclo de parametrização sem gastar tempo cadastrando registros repetitivos. |
| **6. Dashboard de KPIs e Relatórios Gerenciais (RF11, RF10)** | **2:15** | **Dashboard (`/pages/gestao/dashboard.xhtml`):** mostre os KPIs refletindo o que acabou de acontecer no Ato 1 (taxa de ocupação, veículos no pátio, faturamento do dia atualizado com o pagamento da `ROT1001`, giro de vagas, RevPAS, inadimplência do Contrato 404, parcelas a vencer em 7 dias e o alerta de **estadia excessiva > 24h** do veículo `BRA3C33` na `I01`).<br>**Relatórios (`/pages/gestao/relatorios.xhtml`):** alterne entre a aba **Financeiro** e **Ocupação**, aplique um atalho de período (ex.: *Mês Atual* ou *Hoje*) e clique em **Exportar PDF** (OpenPDF), abrindo o PDF gerado na tela para fechar a demonstração com impacto visual. | Encerra a história provando que os dados operacionais viram decisão gerencial e documento auditável em segundos. |

### Riscos da Demonstração ao Vivo e Plano de Mitigação

| Risco Real no EasyPark | Impacto | Como Prevenir / Plano B |
|---|---|---|
| **Internet da sala cair ou Asaas Sandbox ficar lento/fora do ar** | Alto — trava o `StartupEvent` ou impede o Webhook ao vivo. | 1) Suba a aplicação **antes** da apresentação. 2) Deixe 1 print/vídeo curto (15s) de backup do Webhook funcionando ou use um `curl` local para `POST /api/webhooks/asaas` com o header `asaas-access-token` caso o Ngrok/Asaas falhe na rede da universidade. |
| **Ngrok expirar ou trocar URL** | Médio — Webhook do Asaas não chega na sua máquina. | Como você já configurou `NGROK_DOMAIN` no [application.properties](../src/main/resources/application.properties), valide o túnel 10 min antes e confira se o endpoint configurado no painel do Asaas Sandbox aponta exatamente para `<dominio>/api/webhooks/asaas`. |
| **Banco sujo por testes anteriores** | Médio — placas `ROT1001` ou `BRA1A11` já saíram ou já estão no pátio. | Antes de apresentar, reinicie o ambiente em modo `drop-and-create` (ou rode um reset limpo do container Postgres) para garantir o estado exato do [import.sql](../src/main/resources/import.sql). |
| **Troca constante de login (Atendente ↔ Gerente)** | Baixo/Médio — consome 1–2 minutos preciosos. | Use **1 janela normal + 1 janela anônima** (cookies isolados), fazendo login ao vivo apenas uma vez por perfil. |

---

## 4. Priorização de Conteúdo: O que Enfatizar vs. O que Evitar

### O que ENFATIZAR (onde está o valor do seu projeto)
- **Fluxo inteligente de Entrada e Saída:** detecção automática de mensalista, alocação de vaga por tipo, cálculo de tarifa no backend (`1ª hora + frações de 30 min`) e geração de comprovante.
- **Integração real com Asaas (API v3 + Scheduler + Webhook):** cobrança automática 7 dias antes do vencimento, reconciliação diária (`@Scheduled`) e atualização reativa via Webhook.
- **Segurança e LGPD by design:** RBAC por rota no Quarkus Elytron, hash BCrypt, e principalmente a **criptografia do CPF em banco com busca via Blind Index (HMAC)** em [Cliente.java](../src/main/java/ads/upf/model/entities/Cliente.java) — isso impressiona muito avaliadores de Engenharia de Software.
- **Inteligência Gerencial:** KPIs reais de estacionamento no Dashboard (RevPAS, giro de vagas, churn de contratos, estadia excessiva > 24h) e geração de PDF via OpenPDF.

### O que EVITAR ou apenas MENCIONAR brevemente
- **Não demonstre CRUD completo de Clientes e Funcionários** (criar, editar, listar, validar e-mail/telefone). Apenas mencione que existem com validação e que o cadastro de funcionário dispara e-mail com token temporário de 2h via Quarkus Mailer + Qute para definição de senha.
- **Não demonstre o fluxo de "Esqueci minha senha"** ao vivo (depende de SMTP externo/caixa de e-mail e come tempo precioso). Cite-o em 1 frase ao falar de segurança.
- **Não abra a IDE/código-fonte durante os 12 minutos**, a menos que o professor peça explicitamente nos 3 minutos de perguntas.
- **Não tente explicar todas as fórmulas do Dashboard** em detalhe — mostre os números reagindo aos dados reais e deixe as fórmulas para as perguntas.

---

## 5. Recomendações para a Apresentação Técnica (Domínio sem Teoria Excessiva)

### Como apresentar o Diagrama de Casos de Uso em 20 segundos
Não leia os 11 casos de uso. Fale em **blocos de responsabilidade**:
> *"O sistema separa estritamente dois atores: o **Funcionário (Atendente)**, que opera o fluxo diário do pátio — entrada, saída, pagamento rotativo e comprovante — e o **Gerente**, responsável por toda a camada administrativa, contratual, tarifária e analítica. Ambos compartilham apenas a autenticação."*

### Como apresentar a Modelagem de Dados em 30 segundos
Não cite colunas uma a uma. Agrupe o [Logico_Final.png](modelagem-dados/Logico_Final.png) em **3 decisões de modelagem**:
1. **Herança `JOINED` em `Usuario`:** separa dados comuns de credenciais (`Funcionario`) e dados fiscais/gateway (`Cliente`, onde o CPF é salvo criptografado e buscado por `cpf_hash`).
2. **Núcleo Operacional (`Permanencia`):** relaciona `Veiculo` e `Vaga`, guarda o flag `isRotativo` e tem relação `0..1` com `Pagamento` (pois mensalistas não geram pagamento avulso na saída).
3. **Núcleo Recorrente (`Contrato` → `Parcela`):** cada contrato gera suas parcelas mensais com rastreabilidade externa (`asaas_payment_id` e `invoice_url`), além da superclasse `Auditado` garantindo metadados de auditoria em todo o sistema.

### Como falar de Arquitetura e Tecnologias sem virar aula teórica
Conecte cada tecnologia a um **problema concreto resolvido** (1 frase por escolha):
- **Quarkus + Java 21 + PrimeFaces:** produtividade server-side com componentes ricos (tabelas *lazy*, diálogos, filtros) e inicialização rápida.
- **Arquitetura em Camadas + UseCases (`EntradaUseCase`, `SaidaUseCase`):** isola orquestrações complexas de negócio dos CRUDs simples e viabilizou **18 suítes de testes unitários** (JUnit 5 + Mockito + AssertJ).
- **Quarkus Scheduler + REST Client + Webhook:** resolve cobrança recorrente e reconciliação assíncrona sem intervenção manual do gerente.

---

## 6. Preparação para Perguntas da Banca (3 minutos)

### Arquitetura, Segurança e Banco de Dados
1. **"Por que existe `cpf` e `cpf_hash` na tabela `Cliente`?"**
   - *Resposta objetiva:* Por privacidade/LGPD, o CPF é armazenado criptografado no banco via `AttributeConverter` (`StringCryptoConverter`). Como a criptografia usa vetor de inicialização aleatório, não é possível fazer `WHERE cpf = ?` direto. Por isso usamos um *Blind Index* (`cpf_hash` com HMAC) para permitir buscas exatas e restrição de unicidade sem expor o CPF em texto plano.
2. **"No Diagrama Lógico, a tabela `Auditado` aparece solta e não vi tabela de token de recuperação de senha. Por quê?"**
   - *Resposta objetiva:* `Auditado` é uma `@MappedSuperclass` no JPA — ela não vira tabela própria, seus 4 campos (`criado_em`, `criado_por`, `atualizado_em`, `atualizado_por`) são embutidos nas tabelas das entidades de negócio. Já `SenhaToken` foi implementada como entidade auxiliar técnica apenas para expiração de links de definição/recuperação de senha (15 min / 2h), por isso mantivemos o diagrama lógico focado no domínio principal.
3. **"Por que escolheram JSF/PrimeFaces com Quarkus em vez de uma SPA em React/Angular?"**
   - *Resposta objetiva:* O projeto tem foco em telas administrativas densas (tabelas paginadas *lazy*, filtros complexos, modais e formulários validados) e foi desenvolvido individualmente em prazo acadêmico. Quarkus + PrimeFaces permitiu entregar o sistema completo — incluindo integrações, segurança e relatórios — em uma única base de código Java, sem o *overhead* de manter dois projetos e contratos de API para cada tela interna.

### Regras de Negócio e Implementação
4. **"Como o sistema garante que um pagamento de mensalista não fique perdido se o Webhook do Asaas falhar ou o servidor estiver fora do ar?"**
   - *Resposta objetiva:* Temos resiliência em duas camadas. O Webhook (`AsaasWebhookResource`) atualiza a parcela em tempo real (validando o header `asaas-access-token`), mas o `CobrancaService` também executa uma rotina agendada diária às 02h (`@Scheduled`) e no `StartupEvent` que consulta parcelas em aberto no Asaas e reconcilia qualquer status pendente ou cancelado.
5. **"O que impede um usuário mal-intencionado de alterar o valor da permanência no navegador antes de confirmar o pagamento rotativo?"**
   - *Resposta objetiva:* O valor exibido no modal de saída é apenas uma prévia. Na confirmação, o `SaidaUseCase` ignora qualquer valor vindo da interface e recalcula o total no servidor a partir de `dataEntrada`, `LocalDateTime.now()` e a tarifa ativa no banco.
6. **"No Diagrama de Casos de Uso não aparece 'Gerenciar Tarifas', mas vi essa tela no sistema. Como isso se encaixa?"**
   - *Resposta objetiva:* No DVP, a parametrização da tarifa rotativa foi especificada dentro do caso de uso `UC06 - Processar Pagamentos` (HU14), mas na interface separamos em uma tela própria no módulo de gestão (`/pages/gestao/tarifas.xhtml`) porque apenas o gerente tem permissão para alterar a tarifa, enquanto o atendente apenas processa o pagamento na saída.
7. **"Como funciona o pagamento rotativo vs. mensalista no gateway?"**
   - *Resposta objetiva:* Contratos mensais integram de fato com o Asaas Sandbox (criação de cliente, emissão da cobrança 7 dias antes, link de fatura e baixa via Webhook/reconciliação). Já o pagamento rotativo no balcão (dinheiro, PIX, débito, crédito) é processado localmente como *mock* para simular o fechamento rápido de caixa na guarita sem depender de maquininha física/TEF.

---

## Recomendação Final para os 12 Minutos

Para causar a melhor impressão possível dentro dos critérios do professor:
1. **Cronometre um ensaio completo falando em voz alta.** Se você passar de **2 minutos nos slides**, corte falas dos slides — nunca corte tempo da demonstração.
2. **Conduza a demonstração como uma história de negócio, não como um checklist de menus:** *"Entrou um carro rotativo e um mensalista → saiu o mensalista sem cobrar e o rotativo gerando comprovante → o gerente acompanhou o contrato no Asaas, viu o caixa e a ocupação subirem no Dashboard e baixou o PDF."*
3. **Deixe o sistema pronto antes de começar** (Postgres + Quarkus + Ngrok + Asaas Sandbox validados). Quando uma demonstração ao vivo flui sem travamentos, sem erros de digitação e conectando operação, integração externa e análise gerencial, o domínio técnico fica evidente antes mesmo de abrirem os 3 minutos de perguntas.

> **Checklist pré-ensaio:** verifique se o ambiente da sala de apresentação permite conexões de saída para `api-sandbox.asaas.com` e se o domínio fixo do Ngrok está ativo na sua conta; caso contrário, deixe preparado um comando `curl` local para simular o `POST` do Webhook do Asaas como plano B.
