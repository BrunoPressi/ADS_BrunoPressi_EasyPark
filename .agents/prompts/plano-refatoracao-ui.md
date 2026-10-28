# Plano de Refatoração UI/UX — EasyPark (ERP)

Este documento consolida o diagnóstico da camada de apresentação do projeto **EasyPark** e o plano de execução incremental para modernização visual (padrão ERP), incluindo a adoção de **Menu Lateral Esquerdo (Sidebar)**.

---

## 1. Arquitetura Atual da Camada de Apresentação

### Stack e Tecnologias
* **Runtime & Framework Web:** Quarkus `3.39.1` (Java 21) + **Apache MyFaces `4.1.3`** (Jakarta Faces 4.0 / Jakarta EE 10).
* **Componentes UI:** **PrimeFaces `15.0.7`** (`quarkus-primefaces 4.15.17`) com tema **`saga-blue`** (`src/main/resources/META-INF/web.xml`) e ícones **PrimeIcons** (`pi pi-*`).
* **Utilitários CSS:** **PrimeFlex `3.3.1`** via WebJars (`primeflex/3.3.1/primeflex.min.css`).
* **Permissões de Recursos Públicos:** Configuradas em `src/main/resources/application.properties` (`/pages/public/*`, `/login.xhtml`, `/logout.xhtml`, `/jakarta.faces.resource/*`).

### Mapa de Arquivos da Camada de Apresentação
* **Template Principal:**
  * `src/main/resources/META-INF/resources/templates/layout.xhtml` (usa `UserSessionBean`)
* **Páginas Públicas / Autenticação (sem template):**
  * `src/main/resources/META-INF/resources/login.xhtml`
  * `src/main/resources/META-INF/resources/login-success.xhtml` (`UserSessionBean.redirecionar`)
  * `src/main/resources/META-INF/resources/logout.xhtml` (`LogoutBean`)
  * `src/main/resources/META-INF/resources/pages/public/esqueci-senha.xhtml` (`EsqueciSenhaBean`)
  * `src/main/resources/META-INF/resources/pages/public/definir-senha.xhtml` (`NovaSenhaBean`)
  * `src/main/resources/META-INF/resources/pages/public/nao-encontrado.xhtml`
* **Módulo de Gestão (`/pages/gestao/*` — role `gerente`):**
  * `src/main/resources/META-INF/resources/pages/gestao/dashboard.xhtml` (`DashboardBean`)
  * `src/main/resources/META-INF/resources/pages/gestao/vagas.xhtml` (`VagaBean`)
  * `src/main/resources/META-INF/resources/pages/gestao/funcionarios.xhtml` (`FuncionarioBean`)
  * `src/main/resources/META-INF/resources/pages/gestao/clientes.xhtml` (`ClienteBean`)
  * `src/main/resources/META-INF/resources/pages/gestao/contratos.xhtml` (`ContratoBean`, `VeiculoBean`)
  * **Dialogs incluídos:**
    * `src/main/resources/META-INF/resources/pages/gestao/dialogs/dialog-cliente.xhtml`
    * `src/main/resources/META-INF/resources/pages/gestao/dialogs/dialog-veiculo.xhtml`
  * **DataTables em Dialogs incluídos:**
    * `src/main/resources/META-INF/resources/pages/gestao/dataTables/cliente-datatable.xhtml`
    * `src/main/resources/META-INF/resources/pages/gestao/dataTables/veiculo-datatable.xhtml`
    * `src/main/resources/META-INF/resources/pages/gestao/dataTables/parcelas-datatable.xhtml`
* **Módulo de Operação (`/pages/operacao/*` — role `atendente`):**
  * `src/main/resources/META-INF/resources/pages/operacao/fluxo.xhtml` (`PermanenciaBean`)
  * **Dialogs incluídos:**
    * `src/main/resources/META-INF/resources/pages/operacao/dialogs/dialog-entrada.xhtml`
    * `src/main/resources/META-INF/resources/pages/operacao/dialogs/dialog-saida.xhtml`
    * `src/main/resources/META-INF/resources/pages/operacao/dialogs/dialog-pagamento.xhtml`

---

## 2. Principais Problemas Diagnosticados

1. **Ausência de CSS centralizado e abuso de estilos inline:** Não há arquivo CSS próprio; espaçamentos, larguras fixas e cores de erro (`style="color:red;"`) estão espalhados inline.
2. **Estrutura do `layout.xhtml`:**
   * Usa `<p:panel header="#{viewTitle}">`, mas nenhuma página define `viewTitle`, gerando um painel externo com cabeçalho vazio.
   * Menu horizontal simples (`<p:menubar>`) sem logo/identidade do sistema, sem indicação da página ativa e sem exibir o usuário autenticado (`#{userSession.username}`) e seu perfil.
3. **Fragmentos `<ui:include>` com HTML duplicado:**
   * Todos os 8 arquivos em `dialogs/` e `dataTables/` declaram `<!DOCTYPE html><html><h:body>...</h:body></html>` em vez de `<ui:composition>`, injetando tags `<html>` e `<body>` duplicadas dentro do DOM.
4. **Inconsistência nas Telas Públicas:**
   * `login.xhtml`, `logout.xhtml` e `nao-encontrado.xhtml` não importam o PrimeFlex.
   * `definir-senha.xhtml` e `esqueci-senha.xhtml` aplicam `text-align: center` no painel inteiro, centralizando labels e mensagens de erro, e não possuem link para voltar ao login.
   * `logout.xhtml` não possui botão/link ativo para cancelar e voltar.
5. **Padronização de CRUDs, Toolbars e Botões:**
   * As páginas `vagas.xhtml`, `funcionarios.xhtml`, `clientes.xhtml`, `contratos.xhtml` e `fluxo.xhtml` não possuem cabeçalho de página (título e subtítulo) como o `dashboard.xhtml`.
   * Botões de ação primária (`Nova Vaga`, `Novo Cliente`, etc.) ficam soltos acima da `<p:toolbar>`. Em `fluxo.xhtml`, o botão `Registrar Entrada` está dentro do arquivo `dialog-entrada.xhtml` em um `<h:form>` separado.
   * Cores de botões sem padrão semântico (`ui-button-help` roxo para Editar, `ui-button-warning` para visualizar Veículo, `rounded-button` em botões com texto longo).
6. **Tabelas (`<p:dataTable>`) e Responsividade:**
   * `contratos.xhtml` possui 5 colunas separadas apenas para botões de ação.
   * `parcelas-datatable.xhtml` tem `<p:dialog width="1250">` fixo e 9 colunas com `width: 80px`, estourando telas de notebooks (`1280px`/`1366px`). `cliente-datatable.xhtml` e `veiculo-datatable.xhtml` usam `width="895"` fixo.
   * Valores monetários concatenados sem `<f:convertNumber>` em `contratos.xhtml` (`R$#{contrato.valorContratado}/mês`) e `parcelas-datatable.xhtml` (`R$#{parcela.valor}`).
   * Textos com erros de digitação: `'EmManutenção'` (`vagas.xhtml`), `'Concluida'` (`fluxo.xhtml`), `'Digite a placa marca do veículo (opcional)'` (`dialog-veiculo.xhtml`).
7. **Formulários e Diálogos (`<p:dialog>`):**
   * Diálogos com `width="370"` fixo e `closable="false"`.
   * Uso de `<h:message style="color:red;" />` e `<br/>` manuais em vez de `<p:message />` e agrupamento `.field`.
   * Em `dialog-saida.xhtml`, os dados de cobrança (Placa e Valor) não têm destaque visual para o operador do caixa.
   * Em `dashboard.xhtml`, todos os cards usam `shadow-5` (sombra excessivamente pesada).

---

## 3. Diretrizes de Identidade Visual e Layout (Padrão ERP com Sidebar)

* **Folha de Estilo Centralizada:** Criar `src/main/resources/META-INF/resources/css/easypark.css` (carregada via `<h:outputStylesheet name="css/easypark.css"/>`, que é servida automaticamente por `/jakarta.faces.resource/*` já liberado no `application.properties`).
* **Paleta e Superfícies:**
  * Fundo da aplicação (`body`): `#f1f5f9` (Slate 100) para destacar cards e tabelas em fundo branco (`#ffffff`).
  * Sidebar esquerda: `#0f172a` / `#1e293b` (Slate 900/800) com texto claro `#cbd5e1`, hover `#334155` e item ativo com destaque azul (`#2563eb`).
  * Elevação de cards e painéis: Sombra suave (`0 1px 3px rgba(15, 23, 42, 0.08)`) + borda fina (`1px solid #e2e8f0`), substituindo o `shadow-5`.
* **Estrutura do Menu Lateral Esquerdo (Sidebar) em `layout.xhtml`:**
  * **Largura fixa (Desktop e Notebooks):** `250px` (`230px` em telas `<= 1366px`) à esquerda; área de conteúdo com `flex: 1; min-width: 0; padding: 1.75rem 2rem` (sem estilização mobile, já que o ERP é exclusivo para desktops/notebooks).
  * **3 Seções na Sidebar:**
    1. **Topo (Brand):** Ícone `pi pi-car` + **EasyPark** (`Gestão de Estacionamento`).
    2. **Navegação (com `<h:form id="menuForm">`):**
       * Perfil `gerente` (`#{userSession.gerente}`):
         * Seção *Visão Geral*: **Dashboard** (`/pages/gestao/dashboard.xhtml`, `pi-chart-bar`)
         * Seção *Cadastros & Pátio*: **Vagas** (`/pages/gestao/vagas.xhtml`, `pi-th-large`), **Clientes** (`/pages/gestao/clientes.xhtml`, `pi-users`), **Funcionários** (`/pages/gestao/funcionarios.xhtml`, `pi-id-card`)
         * Seção *Comercial*: **Contratos** (`/pages/gestao/contratos.xhtml`, `pi-briefcase`)
       * Perfil `atendente` (`#{userSession.atendente}`):
         * Seção *Operação*: **Entrada e Saídas** (`/pages/operacao/fluxo.xhtml`, `pi-arrows-h`)
       * Classe `active` aplicada dinamicamente conforme `#{request.requestURI.contains('...')}`.
    3. **Rodapé da Sidebar (Sessão):** E-mail/usuário (`#{userSession.username}`), badge do perfil (`Gerente` ou `Atendente`) e botão de **Sair** (`action="/logout.xhtml?faces-redirect=true"`).

---

## 4. Plano de Execução Incremental (Etapa por Etapa)

> **Regra de ouro para todas as etapas:** Preservar todos os `id`s de componentes, `widgetVar`s, expressões EL, bindings, `action`/`actionListener`, `process`, `update` e validações existentes.

### Etapa 1 — Fundação CSS e Template Principal com Sidebar (`[x] Concluída`)
* **Arquivos:**
  * `[CRIADO]` `src/main/resources/META-INF/resources/css/easypark.css`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/templates/layout.xhtml`
* **Entregas:**
  * Criado `easypark.css` (focado exclusivamente em Desktops e Notebooks) com variáveis CSS, estrutura `.app-layout`, `.app-sidebar`, `.app-main`, `.page-header`, `.erp-card`, padronização de `<p:dataTable>`, `<p:dialog>`, `.field` e layout de páginas públicas (`.auth-page`, `.auth-card`).
  * Refatorado `layout.xhtml` implementando a Sidebar esquerda fixa, removendo o `<p:panel header="#{viewTitle}">` vazio e mantendo `<p:growl id="messages">`, `<p:confirmDialog global="true">` e `<h:form id="menuForm">`.

### Etapa 2 — Telas Públicas e de Autenticação (`[x] Concluída`)
* **Arquivos:**
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/login.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/logout.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/public/esqueci-senha.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/public/definir-senha.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/public/nao-encontrado.xhtml`
* **Entregas:**
  * Importados PrimeFlex e `easypark.css` em todas as 5 telas, incluindo `<meta name="viewport">`.
  * Padronizado card centralizado (`.auth-page` / `.auth-card`) com cabeçalho da marca EasyPark, alinhamento à esquerda para labels e inputs, mensagens de erro com `.field-error`, botão de submissão em largura total e links de retorno ao login/sistema (incluindo link de retorno em `logout.xhtml`).
  * Mantido intacto o `<form method="post" action="#{request.contextPath}/j_security_check">` e os campos `j_username`/`j_password` em `login.xhtml`.

### Etapa 3 — Refinamento do Dashboard (`[x] Concluída`)
* **Arquivos:**
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/gestao/dashboard.xhtml`
* **Entregas:**
  * Definido `<ui:define name="title">EasyPark — Dashboard</ui:define>` e padronizado o cabeçalho com `.page-header`.
  * Substituído `shadow-5` nos cards e painéis por `.erp-card` com bordas e sombras sutis de ERP, mantendo os destaques laterais coloridos nos KPIs operacionais e adicionando `stripedRows="true"` nas duas tabelas de alertas inferiores.

### Etapa 4 — Telas de Gestão: Cadastros Base (Vagas, Funcionários e Clientes) (`[x] Concluída`)
* **Arquivos:**
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/gestao/vagas.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/gestao/funcionarios.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/gestao/clientes.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/pages/gestao/dialogs/dialog-cliente.xhtml`
* **Entregas:**
  * Adicionado cabeçalho de página (`.page-header`) com título, subtítulo e botão primário (`Nova Vaga`, `Novo Funcionário`, `Novo Cliente`).
  * Padronizada barra de filtro e `<p:dataTable>` (`stripedRows="true"`, `size="small"`, `responsiveLayout="scroll"`, correção de `'Em Manutenção'`, botões de ação padronizados).
  * Convertido `dialog-cliente.xhtml` para `<ui:composition>` e padronizados `dialogVaga`, `dialogFuncionario` e `dialogCliente` (`closable="true"`, `.field`, `.field-error`, `.dialog-footer`).

### Etapa 5 — Telas de Gestão: Contratos e Sub-diálogos (`[ ] Pendente`)
* **Arquivos:**
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/gestao/contratos.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/gestao/dialogs/dialog-veiculo.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/gestao/dataTables/cliente-datatable.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/gestao/dataTables/veiculo-datatable.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/gestao/dataTables/parcelas-datatable.xhtml`
* **Entregas:**
  * Converter os 4 arquivos de include para `<ui:composition>`.
  * Adicionar cabeçalho de página em `contratos.xhtml`, formatar valores monetários com `<f:convertNumber type="currency" currencySymbol="R$ " locale="pt_BR"/>` e organizar os botões de consulta (`Cliente`, `Veículo`, `Parcelas`) e ações (`Editar`, `Cancelar`) de forma compacta e semântica.
  * Tornar `parcelas-datatable.xhtml`, `cliente-datatable.xhtml` e `veiculo-datatable.xhtml` responsivos para telas de notebooks (removendo `width="1250"` / `width="895"` travados e `width: 80px` forçado nas colunas) e corrigir os rótulos de `dialog-veiculo.xhtml`.

### Etapa 6 — Módulo Operacional: Fluxo de Pátio (`[ ] Pendente`)
* **Arquivos:**
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/operacao/fluxo.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/operacao/dialogs/dialog-entrada.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/operacao/dialogs/dialog-saida.xhtml`
  * `[MODIFICAR]` `src/main/resources/META-INF/resources/pages/operacao/dialogs/dialog-pagamento.xhtml`
* **Entregas:**
  * Converter os 3 diálogos para `<ui:composition>`.
  * Estruturar o cabeçalho operacional em `fluxo.xhtml` integrando o botão `Registrar Entrada` (`#formBotaoEntrada`) de forma alinhada ao topo da página.
  * Melhorar a tabela de permanências (destaque tipográfico para a placa, tags para tipo `Rotativo`/`Mensalista`, correção de `'Concluída'` e alinhamento das ações).
  * Criar um card de destaque financeiro dentro de `dialog-saida.xhtml` para exibir claramente a Placa e o Valor a Pagar antes da seleção do meio de pagamento.

### Etapa 7 — Verificação e Testes (`[ ] Pendente`)
* Executar `./mvnw test` (ou `mvnw.cmd test`) para garantir que a compilação e a suíte de testes do projeto continuam íntegras e revisar todos os arquivos modificados contra a lista de restrições.
