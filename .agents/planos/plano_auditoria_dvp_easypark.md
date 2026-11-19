# Auditoria de Conformidade — DVP × Implementação EasyPark

> Modo somente leitura: nenhum arquivo do projeto foi alterado. Este relatório foi salvo apenas no diretório de artefatos.

## 1. Resumo executivo

A base do sistema segue o DVP: arquitetura em camadas, Quarkus/JSF/PrimeFaces, PostgreSQL, integração Asaas, login com perfis, CRUDs, entrada/saída, cálculo de permanência, comprovante, dashboard e relatórios em PDF. Das 22 histórias avaliadas, a maioria tem implementação real. Porém **há divergências comprovadas em regras de negócio relevantes**:

- A multa de 0,15% para mensalistas atrasados **não existe**.
- A senha do funcionário **não é gerada** no padrão 5 minúsculas + 2 maiúsculas + 3 dígitos.
- O link de recuperação vale **2 h, não 15 min**.
- O valor da saída rotativa **vem da tela, não é recalculado no servidor**.
- O valor do contrato **é ignorado** (parcelas sempre R$ 180).
- Nenhum código **marca contratos como `inadimplente`**.
- O perfil `atendente` **não consulta vagas** (HU10).
- O filtro de saídas por placa **não encontra saídas concluídas** (HU13).

| Status | Qtde (histórias HU + RFs sem HU próprio) |
|---|---|
| Atendido | 8 |
| Parcialmente atendido | 11 |
| Divergente | 5 |
| Não atendido | 1 (multa) |
| Não verificável | 3 |

**Limitações:**
- A análise foi só por leitura de código. Nada foi executado: nem a aplicação, nem os testes, nem medições de tempo (RNF01/03/07).
- O texto do DVP foi extraído dos streams do PDF. Os diagramas (casos de uso, modelo lógico) não foram interpretados.
- Não li integralmente os SQLs de `RelatorioRepository` e `DashboardRepository`, nem todos os XHTML.

## 2. Entendimento do DVP

- **Domínio:** gestão de estacionamento de pequeno e médio porte, com mensalistas (contrato) e rotativos (cobrança por permanência).
- **RF01–RF11:** login, funcionários, clientes, entrada/saída, vagas, contratos mensais, cálculo de permanência, pagamento (mock/gateway), comprovantes, relatórios e dashboard.
- **RNF01–RNF08:**
  - comprovante em menos de 5 s;
  - interface intuitiva;
  - resposta em menos de 3 s;
  - autenticação obrigatória;
  - restrição por perfil (gerente/funcionário);
  - disponibilidade;
  - navegadores Chrome, Edge e Firefox;
  - persistência permanente de entradas, saídas e pagamentos.
- **Histórias:** HU01–HU24, organizadas em UC01–UC11.
- **Regras-chave:**
  - mensalidade de R$ 180,00 com vencimento no dia da criação do contrato, cobrada 7 dias antes;
  - multa de 0,15% por atraso;
  - rotativo a R$ 70/h e R$ 35 por fração de 30 min;
  - placa Mercosul ou padrão antigo;
  - vaga no formato LNN;
  - CPF validado pelo dígito verificador;
  - listas paginadas (20 ou 50 por página).

**Ambiguidades e inconsistências do próprio DVP** (não assumi interpretação):
1. O vencimento é "no dia em que o contrato foi criado" (HU05), mas o cronograma diz "vencimento no dia 10".
2. Numeração de HUs: HU08 aparece duas vezes (Cancelar contrato e Criar vaga). HU11 e HU15 não aparecem no texto, embora o cronograma as cite (UC07 Emitir Comprovantes / HU15).
3. HU04/HU13: "ordenadas pelo status em andamento" e "trocar de filtro", sem definir quais filtros existem.
4. HU09: "só pode ser editada se disponível" não deixa claro como uma vaga em manutenção volta a disponível.
5. HU05 lista meios de pagamento Pix/Crédito/Débito/Boleto para mensalidade, mas HU14 lista dinheiro/Pix/Crédito/Débito para rotativo.
6. HU03 item 5 está truncado ("selecionar o Tipo de Vaga a ser reservada d").
7. A HU03 menciona "ano" do veículo, que não existe no modelo de dados.
8. RF08 se chama "Mock de Pagamentos" no sumário e "Integração com Gateway" no corpo.
9. Tabela de tecnologias com versões duvidosas (Jakarta Faces "4.15.17", JUnit "3.39.1").
10. O DVP não define o perfil "gerente" para operar entrada/saída.

## 3. Matriz de rastreabilidade

| Requisito | Especificação do DVP | Implementação encontrada | Status | Evidências |
|---|---|---|---|---|
| RF01 / HU01 | Login por perfil; erro em credencial inválida | Form auth Quarkus + JDBC realm (bcrypt), usuário = e-mail, role em `funcionarios.role`; mensagem de erro no login | **Atendido** | `application.properties` (`quarkus.security.jdbc.*`, `quarkus.http.auth.form.*`); `login.xhtml:38` |
| HU02 | Botão "recuperar senha", link por e-mail **válido 15 min** | Link e token existem, mas validade de **2 horas** | **Divergente** | `esqueci-senha.xhtml`; `TokenService.criarToken` (`plusHours(2)`); `AlterarSenhaUseCase` |
| RF02 / HU19 | Criar funcionário; **senha gerada pelo sistema (5 min + 2 mai + 3 díg.)** e enviada por e-mail; telefone `11 91234-5678` | Cria funcionário (role `atendente`) e envia **link para o próprio funcionário definir a senha** (mín. 8 caracteres). Regex de telefone permissiva | **Divergente** | `FuncionarioService.salvarFuncionario`; `DefinirSenhaDTO` (`@Size(min=8)`); `UsuarioCreateDTO.telefone`; `EmailService` |
| HU20 | Editar nome, telefone, e-mail | Implementado | **Atendido** | `FuncionarioService` (ramo `else`); `FuncionarioMapper.updateFuncionarioFromDto` |
| HU21 | Lista ordenada por ID **e** nome; ≤50/página; filtro por ID e nome | Ordenação só por nome; 10/página; filtro por ID ou nome (um registro) | **Parcialmente atendido** | `FuncionarioService.listarPaginado` (`Sort.by("nomeCompleto")`); `funcionarios.xhtml:64` (rows=10); `buscarPorTermo` |
| RF03 / HU16 | Nome, CPF (formato + dígito verificador), telefone, e-mail válidos | `@CPF` (valida dígito), `@Email`, `@NotBlank`; telefone por regex genérica; CPF criptografado + hash | **Parcialmente atendido** | `ClienteCreateDTO`; `UsuarioCreateDTO`; `Cliente.java`; `StringCryptoConverter` |
| HU17 | Editar nome, CPF, telefone, e-mail | Implementado | **Atendido** | `ClienteService.salvarCliente` |
| HU18 | Lista ordenada por **ID**; ≤50/página; filtro por e-mail ou CPF | Ordenada por **nome**; 10/página; filtro por e-mail ou CPF (um registro) | **Parcialmente atendido** | `ClienteRepository.listarTodosPaginado` (`Sort.by("nomeCompleto")`); `clientes.xhtml:37,64` |
| RF04 / HU03 | Placa obrigatória (Mercosul/antigo); modelo/ano/cor opcionais; hora automática; selecionar tipo de vaga; mensal se contrato ativo, senão rotativo | Placa validada por regex; hora = `now()`; vaga ocupada por tipo; `isRotativo` = sem contrato ativo. **Exige "tipo do veículo"** (fora do DVP); a tela de entrada **não coleta marca/modelo/cor**; regex aceita hífen também no padrão Mercosul | **Parcialmente atendido** | `EntradaUseCase.novaEntrada`; `EntradaCreateDTO`; `PlacaValidator`; `dialog-entrada.xhtml` |
| HU04 | Lista de entradas, em andamento primeiro, ≤20/página | Ordenação `status desc, dataEntrada desc`; 10/página; filtro por placa | **Atendido** | `PermanenciaRepository.listarPaginado`; `fluxo.xhtml:74` |
| HU12 | Buscar por placa; mensal sem cobrança; rotativo **calcula valor e processa pagamento antes de finalizar**; erro se placa sem entrada | Mensal: sem cobrança. Rotativo: usa `saida.getValor()` **enviado pela tela**; o servidor **não recalcula**. Erro "Permanência não encontrada" ok | **Divergente** (cálculo) | `SaidaUseCase.novaSaida`; `PermanenciaBean.novaSaida`; `SaidaCreateDTO` |
| HU13 | Lista de saídas, ≤20/página, **filtro por placa** | Mesma lista de permanências. O filtro por placa usa `verificarVeiculoEstacionado` (`status = em_andamento`) e **não encontra saídas concluídas** | **Divergente** | `PermanenciaService.buscarPermanenciaPelaPlaca`; `PermanenciaRepository.verificarVeiculoEstacionado` |
| RF05 / HU08 (vaga) | Nome LNN, tipo (Comum/Moto/PNE/Idoso), checkbox manutenção/disponível | Regex `^[A-Z]\d{2}$`, 4 tipos, checkbox na UI, nome único. O enum `VagaStatus` tem `reservada` (fora do DVP) e o DTO aceita qualquer status | **Atendido** (com ressalvas) | `VagaFormDTO`; `VagaStatus`; `vagas.xhtml:126-147`; `VagaService.salvarVaga` |
| HU09 | Só edita se **disponível**; manual só → manutenção; nome editável | `Vaga.editar()` bloqueia **apenas `ocupada`**. Nenhuma checagem de transição de status | **Divergente** | `Vaga.editar`; `VagaMapper.toUpdateFromVagaDto` |
| HU10 | **Funcionário ou gerente** consulta vagas; ≤50/página; filtro por nome | `vagas.xhtml` está em `/pages/gestao/*`, restrito a `gerente`. Atendente não acessa. Paginação de 10 e busca por nome exato | **Divergente** (acesso) | `application.properties` (`perm-gerente`); `layout.xhtml:97-99`; `VagaService.buscarPeloNome` |
| RF06 / HU05 | Cliente por CPF ou cadastro; início ≥ hoje; término ≥ início+1 mês; **valor R$ 180**; vencimento no dia da criação; cobrança 7 dias antes; meios Pix/Crédito/Débito/Boleto | Validações de datas presentes; cadastro de cliente no mesmo diálogo. **`valorContratado` é informado pelo usuário** (até 999,99) mas as parcelas **sempre** valem 180. Vencimento = `dataInicio + i meses`. Cobrança D-7 por job diário às 02:00 e ao iniciar. Asaas com `billingType=UNDEFINED` | **Parcialmente atendido** | `ContratoCreateDTO`; `ParcelaService.gerarParcelas` (`VALOR_PADRAO`); `ParcelaRepository.buscarComVencimentoEm7Dias`; `CobrancaService`; `AsaasService.cobrarParcela`; `contratos.xhtml:239,266` |
| HU06 | Lista ≤50/página; filtro por CPF; ver ativos/cancelados/inadimplentes | 10/página; filtro por CPF retorna **um único contrato** (`firstResultOptional`) | **Parcialmente atendido** | `ContratoRepository.buscarPeloCpf`; `ContratoService.buscarPorCliente`; `contratos.xhtml:39,66` |
| HU07 | Só edita sem parcelas pagas; cancelado não edita | Regra implementada (também bloqueia inadimplente/encerrado, mais restritivo). Ao editar, as parcelas são recriadas **sem cancelar as cobranças já emitidas no Asaas** | **Parcialmente atendido** | `Contrato.atualizarVigenciaEParcelas`; `ContratoService.salvarContrato` (ramo de edição) |
| HU08 (contrato) | Cancela só sem parcelas atrasadas **e não inadimplente** | Bloqueia por parcela atrasada. **Nenhum código altera o status do contrato para `inadimplente`** (só o seed `import.sql`), então o bloqueio por contrato inadimplente nunca ocorre via fluxo real. Cancelamento remove cobranças no Asaas | **Parcialmente atendido** | `Contrato.cancelar`; `ParcelaService.marcarComoAtrasada`; busca por `inadimplente` (só `DashboardRepository`, `import.sql`) |
| RF07 / HU14 (cálculo) | R$ 70 hora cheia; R$ 35 por fração de 30 min | 1ª hora = tarifa; frações de 30 min iniciadas; tarifa configurável (mín. R$ 45) com valores `import.sql` | **Atendido** | `PagamentoService.detalharCalculoPermanencia`; `TarifaCreateDTO`; `TarifaService` |
| HU14 (meios) | Dinheiro, Pix, crédito, débito | Enum `PagamentoMeio` com os 4 meios; confirmação imediata como mock | **Atendido** | `PagamentoMeio`; `PagamentoService.gerarPagamento` |
| HU14 (multa) | **Multa de 0,15% do valor mensal** para mensalista atrasado | **Não encontrada** em código, DTO Asaas ou Parcela | **Não atendido** | busca por "multa/0.0015/juros" sem resultado; `AsaasPaymentRequest` sem campos de multa/juros |
| HU14 (erro) | Mensagem de erro se falhar | `catch` exibe FacesMessage de erro | **Atendido** | `PermanenciaBean.processarSaida` |
| RF08 | Integração/Mock de gateway | Asaas sandbox para mensalidades (criação de cobrança, webhook, reconciliação diária); rotativo é mock interno | **Atendido** | `AsaasService`; `AsaasWebhookResource`; `CobrancaService` |
| RF09 | Comprovante do pagamento | Diálogo de comprovante após saída e reabertura pela lista. Para mensalista bloqueia (sem cobrança). Sem PDF/impressão. UC07/HU15 não aparecem no texto do DVP | **Parcialmente atendido** / ambíguo | `dialog-pagamento.xhtml`; `PermanenciaBean.selecionarPagamento`; `PagamentoService.gerarResumoComprovante` |
| RF10 / HU23 | PDF; lucro médio diário/semanal/mensal; maior e menor lucro; total; período dia/semana/mês/ano | PDF (OpenPDF); agrupamento DIA/SEMANA/MES/ANO; receita total, média por período, maior e menor período, ticket médio | **Atendido** | `RelatorioService.calcularResumoFinanceiro`; `RelatorioPdfGenerator`; `PeriodoRelatorioTipo` |
| HU24 | PDF; média de veículos, período com menos entradas, período escolhido | Resumo com média por período, maior e menor movimento, tempo médio | **Atendido** | `RelatorioService.calcularResumoOcupacao`; `RelatorioBean.getArquivoPdfOcupacao` |
| RF11 / HU22 | KPIs: ocupação, veículos no pátio, tempo médio, faturamento do dia, receita do mês (rotativo × mensal), contratos novos/cancelados/saldo, inadimplência (% e R$), estadias >24 h, parcelas a vencer em 7 dias. Atualização em <5 s | Todos os KPIs têm DTO, query e card. O dashboard limpa o cache ao carregar. Não há mapa visual de vagas coloridas (citado só no cronograma). "Tempo real" não verificável | **Parcialmente atendido** | `DashboardService`; `DashboardRepository`; `DashboardBean.carregarDados`; `dashboard.xhtml` |
| RNF01 | Comprovante em <5 s | Geração em memória/tela, sem medição | **Não verificável** | `PermanenciaBean.carregarDadosComprovante` |
| RNF02 | Interface intuitiva | Subjetivo | **Não verificável** | — |
| RNF03 | Resposta <3 s | Lazy pagination; sem medição | **Não verificável** | `GenericLazyDataModel` |
| RNF04 | Autenticação obrigatória | Políticas por caminho. Só `/pages/public/*`, login, logout e recursos são `permit`. Não há política catch-all | **Parcialmente atendido** | `application.properties` (`quarkus.http.auth.permission.*`) |
| RNF05 | Restrição por perfil | `/pages/gestao/*` → gerente; `/pages/operacao/*` → atendente. **Só por caminho**: nenhum `@RolesAllowed` nos serviços/beans. O gerente não acessa operação | **Parcialmente atendido** | `application.properties`; `UserSessionBean`; ausência de `@RolesAllowed` |
| RNF06 / RNF07 | Disponibilidade; Chrome/Edge/Firefox | Não verificável por leitura de código | **Não verificável** | `../../Dockerfile`, `docker-compose.yml` |
| RNF08 | Entradas, saídas e pagamentos persistidos permanentemente | `Permanencia` e `Pagamento` persistidos (entidades auditadas). Sem exclusão física identificada | **Atendido** | `Permanencia.java`, `Pagamento.java`, `Auditado.java` |

## 4. Problemas identificados

### 4.1 Comprovados (código lido)

**Requisitos ausentes**
- Multa de 0,15% para mensalidades atrasadas (HU14, item 4). Não há cálculo nem envio ao Asaas.

**Divergências**
1. **Senha de funcionário:** não é gerada no padrão do DVP. O funcionário recebe um link e define uma senha com mínimo de 8 caracteres (`DefinirSenhaDTO`).
2. **Validade do link:** 2 h no código, 15 min no DVP (`TokenService`).
3. **Valor da saída rotativa:** o servidor aceita o valor vindo do formulário, sem recalcular (`SaidaUseCase`). Isso permite adulterar o preço e contradiz "o sistema deverá calcular".
4. **Filtro por placa em saídas:** busca somente permanências `em_andamento`, então não encontra saídas concluídas (HU13).
5. **Acesso às vagas (HU10):** o atendente não alcança a consulta de vagas.
6. **Edição de vaga (HU09):** só `ocupada` é bloqueada, não "somente disponível". Também não há regra de transição para manutenção.

**Regras de negócio inconsistentes**
- `valorContratado` é digitado e persistido, mas as parcelas são geradas com R$ 180 fixo (`ParcelaService.VALOR_PADRAO`).
- Nenhum fluxo marca o contrato como `inadimplente`. Com isso a regra "não cancelar se inadimplente" e o KPI de contratos inadimplentes dependem de dados que o sistema não produz.
- Editar contrato recria parcelas sem remover as cobranças já emitidas no Asaas (risco de cobrança órfã). O cancelamento trata isso; a edição não.
- Vencimento calculado a partir da data de **início** e não da data de **criação** (ambíguo; ver §2).
- O número de parcelas é calculado por meses de calendário (`YearMonth`). Um período como 15/01→14/03 gera 2 parcelas, a última vencendo depois do término.
- `Usuario.nomeCompleto` é `unique`. Isso impede cadastrar dois clientes ou funcionários homônimos, e o DVP não prevê essa restrição.
- HU03: o tipo de veículo é obrigatório na entrada (não previsto) e marca/modelo/cor não podem ser informados nessa tela.
- Ordenação e filtros diferem do DVP: clientes por nome (DVP: ID); funcionários só por nome; filtros retornam um registro; tamanho de página é 10 (dentro do limite máximo, mas menor que o previsto).

**Sem correspondência clara no DVP**
- Módulo de tarifas (`tarifas.xhtml`, `TarifaService`).
- Status de vaga `reservada`.
- KPIs extras: giro de vagas, RevPAS, vagas comprometidas por mensalistas.
- Gerente não opera entrada/saída.
- Cache de KPIs.

**Arquitetura e organização**
- O controle de acesso é apenas por URL. Beans e serviços não têm `@RolesAllowed`.
- Não há política catch-all de autenticação (RNF04).
- O `GenericLazyDataModel` usa busca por chave única e retorna no máximo 1 registro.

### 4.2 Possíveis melhorias (não são não conformidades)
- Medir RNF01, RNF03 e a atualização "em tempo real" com testes de carga.
- Revisar `web.xml`: `PROJECT_STAGE=Development` e `STATE_SAVING_METHOD=client`. O ambiente `dev` usa `drop-and-create` no schema.
- Inconsistências de encoding em comentários (`web.xml`, `application.properties`).
- Alterar CPF no cliente não atualiza `asaasCustomerId`.
- Tela de entrada poderia consultar a API de placa (o cronograma cita; o DVP não exige).
- Definir formalmente o RF09/UC07 (HU15) no DVP.

## 5. Pontos positivos
- Arquitetura em camadas conforme o DVP (presentation / services / repositories / entities) com DTOs e MapStruct.
- Validação de placa nos dois padrões, nome de vaga LNN, CPF com dígito verificador, datas do contrato.
- CPF criptografado no banco com *blind index* para busca.
- Cálculo de permanência com frações e tarifa parametrizável.
- Fluxo mensalista: cobrança D-7 agendada, reconciliação diária, webhook com token, cancelamento e exclusão de cobranças.
- Relatórios financeiros e de ocupação em PDF, com período selecionável e indicadores pedidos.
- Dashboard cobre praticamente todos os KPIs listados.
- Auditoria de entidades (`Auditado`) e testes unitários para serviços principais.
- Perfis e menus separados por papel (gerente / atendente).

## 6. Plano de ação (somente recomendação; aguardar sua avaliação)

| # | Problema | Requisito | Componentes | Alteração recomendada | Prioridade |
|---|---|---|---|---|---|
| 1 | Multa de 0,15% inexistente | HU14.4 | `ParcelaService`, `AsaasService`, `AsaasPaymentRequest`, `Parcela` | Aplicar multa por atraso (local e/ou campo `fine` no Asaas), com testes | Alta |
| 2 | Valor da saída vindo do cliente | HU12 / RF07 | `SaidaUseCase`, `SaidaCreateDTO`, `PermanenciaBean` | Recalcular no servidor com `PagamentoService` e ignorar o valor recebido | Alta |
| 3 | `inadimplente` nunca definido | HU08 / HU06 / RF11 | `ParcelaService.marcarComoAtrasada`, `Contrato`, rotina diária | Definir regra e transição de status; reconciliar | Alta |
| 4 | Valor do contrato ≠ parcelas | HU05 | `ContratoCreateDTO`, `ParcelaService`, `contratos.xhtml` | Fixar R$ 180 (ou usar `valorContratado` com validação) | Alta |
| 5 | Edição de contrato deixa cobrança órfã | HU07 | `ContratoService.salvarContrato` | Cancelar cobranças no Asaas antes de recriar parcelas | Alta |
| 6 | Filtro por placa não acha saídas | HU13 | `PermanenciaRepository`, `PermanenciaService`, `fluxo.xhtml` | Busca por placa em qualquer status e filtrar página | Alta |
| 7 | Senha do funcionário e validade do link | HU19 / HU02 | `FuncionarioService`, `TokenService`, `EmailService` | Alinhar com o DVP (ou ajustar o DVP) e usar 15 min | Média |
| 8 | Atendente sem consulta de vagas | HU10 | `application.properties`, `layout.xhtml`, `vagas.xhtml` | Liberar leitura para atendente (separar consulta da edição) | Média |
| 9 | Regras de edição/status de vaga | HU09 | `Vaga.editar`, `VagaService`, `VagaMapper` | Exigir `disponivel` e validar transições; decidir sobre `reservada` | Média |
| 10 | Ordenação/filtros/paginação | HU18/HU21/HU06/HU13 | Repositórios e `GenericLazyDataModel` | Ordenar por ID; filtros por lista; revisar tamanho de página | Média |
| 11 | Autorização só por URL | RNF04/05 | Serviços/beans, `application.properties` | `@RolesAllowed` e política catch-all | Média |
| 12 | Unicidade de `nomeCompleto` | HU16/HU19 | `Usuario`, `ClienteService`, `FuncionarioService` | Remover a restrição (ou documentá-la no DVP) | Média |
| 13 | HU03: tipo de veículo e dados opcionais | HU03 | `EntradaCreateDTO`, `dialog-entrada.xhtml` | Alinhar ao DVP; incluir campos opcionais | Baixa |
| 14 | Telefone, vencimento/parcelas, mapa de vagas, comprovante PDF | HU16/HU05/RF11/RF09 | Respectivos DTOs, serviços e telas | Resolver ambiguidades antes de alterar | Baixa |
| 15 | Medir RNF01/03 e compatibilidade | RNF01/03/07 | Testes de carga e E2E | Executar e documentar | Baixa |

## 7. Conclusão

A implementação está **majoritariamente alinhada ao escopo funcional do DVP**: a cobertura de funcionalidades é alta e as camadas estão bem organizadas. A **conformidade fina das regras de negócio é parcial**. As lacunas mais graves são multa, recálculo de valor no servidor, status de inadimplência, cobrança órfã na edição e regras de senha/token. Há também divergências de acesso e de listagem (HU10, HU13, ordenações).

Parte do desalinhamento vem de **ambiguidades do próprio DVP** (§2). Recomendo esclarecer esses pontos junto com a sua avaliação antes de qualquer alteração de código.
