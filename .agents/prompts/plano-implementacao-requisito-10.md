# Plano de Implementação do Requisito nº 10 (Relatórios Gerenciais) — EasyPark (ERP)

Este documento consolida o diagnóstico técnico, as decisões de negócio validadas e o plano de execução incremental para a implementação do **Requisito Funcional nº 10 (RF10 / UC11 — Visualizar Relatórios Financeiros e de Ocupação)** no projeto **EasyPark**.

---

## 1. Arquitetura e Base Existente Relacionada ao RF10

### Stack e Tecnologias
* **Runtime & Framework Web:** Quarkus `3.39.1` (Java 21) + **Apache MyFaces `4.1.3`** (Jakarta Faces 4.0 / Jakarta EE 10).
* **Persistência & Banco de Dados:** Hibernate ORM with Panache + **PostgreSQL `15`** (consultas analíticas via `EntityManager.createNativeQuery` em repositórios `@ApplicationScoped`).
* **Componentes UI & Estilo:** **PrimeFaces `15.0.7`** (`quarkus-primefaces 4.15.17`), **PrimeFlex `3.3.1`** e folha de estilo centralizada `src/main/resources/META-INF/resources/css/easypark.css`.
* **Exportação de Documentos:** **OpenPDF** (`com.github.librepdf:openpdf`, biblioteca oficial integrada ao PrimeFaces para geração de documentos PDF em memória via `DefaultStreamedContent` / `<p:fileDownload>`) + exportação tabular CSV nativa do PrimeFaces (`<p:dataExporter type="csv" />`).
* **Segurança:** Rota `/pages/gestao/*` protegida pela política `policy-gerente` (`roles-allowed=gerente`) em `src/main/resources/application.properties`.

### Entidades e Tabelas Fontes de Dados
* **Financeiro (HU23):**
  * `src/main/java/ads/upf/model/entities/Pagamento.java` (`pagamentos`): `id`, `valor`, `dataPagamento`, `status` (`pago`, `pendente`), `meioPagamento` (`PIX`, `CREDIT_CARD`, `DEBIT_CARD`, `DINHEIRO`), `permanencia_id`.
  * `src/main/java/ads/upf/model/entities/Parcela.java` (`Parcelas`): `id`, `numeroParcela`, `dataVencimento`, `dataPagamento`, `valor`, `meioPagamento` (`PIX`, `BOLETO`, `CREDIT_CARD`, `DEBIT_CARD`, `UNDEFINED`), `status` (`paga`, `pendente`, `atrasada`, `cancelada`), `contrato_id`.
  * `src/main/java/ads/upf/model/entities/Contrato.java` (`contratos`), `Cliente.java` (`clientes`) e `Usuario.java` (`usuarios`).
* **Ocupação e Fluxo de Pátio (HU24):**
  * `src/main/java/ads/upf/model/entities/Permanencia.java` (`permanencias`): `id`, `dataEntrada`, `dataSaida`, `status` (`em_andamento`, `concluida`), `isRotativo`, `veiculo_id`, `vaga_id`, `pagamento_id`.
  * `src/main/java/ads/upf/model/entities/Vaga.java` (`vagas`): `id`, `nome` (padrão `LNN`), `status` (`disponivel`, `ocupada`, `em_manutencao`, `reservada`), `tipoVaga` (`comum`, `moto`, `pne`, `idoso`).
  * `src/main/java/ads/upf/model/entities/Veiculo.java` (`veiculos`): `id`, `placa`, `marca`, `modelo`, `cor`, `tipo` (`carro`, `moto`, `van`).

---

## 2. Definições de Negócio e Escopo Validados (RF10 / UC11)

1. **Composição da Receita no Relatório Financeiro (HU23):**
   * O faturamento considera a soma consolidada dos **pagamentos avulsos de rotativos** (`pagamentos` com `status = 'pago'` filtrados por `dataPagamento`) e das **mensalidades quitadas** (`parcelas` com `status = 'paga'` filtradas por `dataPagamento`).
   * O relatório permite filtrar por origem da receita: `Todas`, `Apenas Rotativo` ou `Apenas Mensalistas`.
   * Apresenta: **Faturamento Total** (com discriminação Rotativo vs. Mensalistas), **Faturamento Médio** por subperíodo (diário, semanal, mensal ou anual), **Período de Maior Faturamento**, **Período de Menor Faturamento**, resumo por **Meio de Pagamento**, tabela consolidada por subperíodo e listagem auditável de lançamentos.
2. **Relatório de Ocupação e Fluxo de Pátio (HU24):**
   * Apresenta: **Total de Veículos Estacionados (Entradas)** no período (discriminando Rotativos vs. Mensalistas), **Média de Veículos Estacionados** por subperíodo, **Período com Maior Movimento**, **Período com Menor Movimento**, **Tempo Médio de Permanência**, tabela consolidada por subperíodo, tabela de utilização por **Tipo de Vaga** (`comum`, `moto`, `pne`, `idoso`), distribuição por **Faixa Horária** e listagem detalhada das permanências do período.
3. **Regra para Cálculo de "Menor Faturamento" e "Menor Ocupação" (RN3 de HU23 e HU24 — Opção A):**
   * Tanto para o menor faturamento quanto para o menor número de entradas de veículos, são considerados **apenas os subperíodos (dia, semana, mês ou ano) que registraram operação/movimentação efetiva** dentro do intervalo filtrado, evitando distorções causadas por dias sem expediente ou datas futuras zeradas.
4. **Filtros de Período e Agrupamento (RN5 de HU23 e HU24):**
   * Atalhos rápidos de período: `Hoje (Dia)`, `Esta Semana`, `Este Mês`, `Este Ano`, além de seleção livre de `Data Inicial` e `Data Final` (`<p:datePicker>`).
   * Seletor de agrupamento temporal (`PeriodoRelatorioTipo`): `DIA` (Diário), `SEMANA` (Semanal), `MES` (Mensal) e `ANO` (Anual).
5. **Escopo de Interface e Exportação (Critérios de Aceite HU23, HU24 e RNF03):**
   * Página única em `src/main/resources/META-INF/resources/pages/gestao/relatorios.xhtml` estruturada em **duas abas (`<p:tabView>`)**: **Relatório Financeiro** e **Relatório de Ocupação**, compartilhando a barra superior de filtros.
   * Estilo visual limpo baseado exclusivamente em cards `.erp-card` e tabelas `<p:dataTable>` (sem gráficos adicionais), mantendo 100% de padronização com `dashboard.xhtml` e `easypark.css`.
   * Exportação server-side para **PDF** (via OpenPDF com layout institucional executivo em menos de 10 segundos) e exportação tabular para **CSV** (`<p:dataExporter type="csv" />`).

---

## 3. Diretrizes Técnicas e Arquiteturais

* **Padrão de Camadas:**
  * `presentation` (`RelatorioBean` `@ViewScoped @Named("relatorioBean")`) → `services` (`RelatorioService` e `RelatorioPdfGenerator` `@ApplicationScoped`) → `repositories` (`RelatorioRepository` `@ApplicationScoped`) → `model/DTOs/relatorio` e `model/enums/PeriodoRelatorioTipo`.
* **Consultas SQL Nativas Seguras e Otimizadas:**
  * Utilizar `EntityManager.createNativeQuery` com parâmetros nomeados (`:dataInicio`, `:dataFim`, `:origem`, `:modalidade`, `:tipoVaga`) no `RelatorioRepository`, seguindo o padrão já consolidado em `DashboardRepository.java`.
* **Geração de PDF Executiva (`RelatorioPdfGenerator`):**
  * Documento A4 gerado em memória (`ByteArrayOutputStream`) contendo: cabeçalho institucional do EasyPark, título do relatório, filtros aplicados, data/hora de emissão, usuário autenticado emissor, quadro de KPIs de resumo (Total, Média, Maior e Menor registro), tabela agregada por período, tabelas secundárias (meios de pagamento / tipos de vaga) e listagem analítica.

---

## 4. Plano de Execução Incremental (Etapa por Etapa)

> **Regra de ouro para todas as etapas:** Preservar integralmente as funcionalidades existentes, mantendo a compatibilidade com o design system de `easypark.css`, as políticas de segurança de `application.properties` e a suíte de testes unitários do projeto.

### Etapa 1 — Dependência OpenPDF, Enum e DTOs de Relatório (`[x] Concluída`)
* **Arquivos:**
  * `[MODIFICADO]` `pom.xml`
  * `[CRIADO]` `src/main/java/ads/upf/model/enums/PeriodoRelatorioTipo.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioFiltroDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioFinanceiroResumoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioFinanceiroPeriodoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioFinanceiroMeioPagamentoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioFinanceiroLancamentoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioOcupacaoResumoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioOcupacaoPeriodoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioOcupacaoVagaTipoDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioOcupacaoFaixaHorariaDTO.java`
  * `[CRIADO]` `src/main/java/ads/upf/model/DTOs/relatorio/RelatorioOcupacaoDetalheDTO.java`
* **Entregas:**
  * Declarada a dependência `com.github.librepdf:openpdf` (`2.2.5`) no `pom.xml`.
  * Criado o enum `PeriodoRelatorioTipo` (`DIA`, `SEMANA`, `MES`, `ANO`) com rótulos amigáveis e unidade correspondente para `DATE_TRUNC` no PostgreSQL (`day`, `week`, `month`, `year`).
  * Criados os DTOs de transporte para filtros, KPIs de resumo, séries agrupadas por período e listagens analíticas dos relatórios Financeiro e de Ocupação.

### Etapa 2 — Camada de Persistência: `RelatorioRepository` (`[x] Concluída`)
* **Arquivos:**
  * `[CRIADO]` `src/main/java/ads/upf/repositories/RelatorioRepository.java`
* **Entregas:**
  * Implementadas consultas nativas para o **Relatório Financeiro (HU23)**:
    * Agregação por subperíodo (`DIA`, `SEMANA`, `MES`, `ANO`) unificando `pagamentos` (`status = 'pago'`) e `parcelas` (`status = 'paga'`), respeitando o filtro de intervalo de datas e de origem (`TODAS`, `ROTATIVO`, `MENSALISTA`).
    * Distribuição de faturamento e quantidade de transações por meio de pagamento.
    * Listagem detalhada de lançamentos financeiros realizados no período (ordenada da mais recente para a mais antiga).
  * Implementadas consultas nativas para o **Relatório de Ocupação (HU24)**:
    * Agregação de entradas/saídas por subperíodo (`DIA`, `SEMANA`, `MES`, `ANO`), separando rotativos e mensalistas e calculando o tempo médio de permanência das estadias concluídas.
    * Utilização agrupada por categoria de vaga (`tipoVaga`: Comum, Moto, PNE, Idoso) e por faixa horária de entrada (`EXTRACT(HOUR FROM dataentrada)`).
    * Listagem analítica de permanências registradas no período com placa, veículo, vaga, modalidade, entrada, saída, duração e valor pago.

### Etapa 3 — Camada de Serviço e Geração de PDF (`RelatorioService` e `RelatorioPdfGenerator`) (`[x] Concluída`)
* **Arquivos:**
  * `[CRIADO]` `src/main/java/ads/upf/services/RelatorioService.java`
  * `[CRIADO]` `src/main/java/ads/upf/services/RelatorioPdfGenerator.java`
* **Entregas:**
  * Implementada em `RelatorioService` a validação de datas (`dataInicio <= dataFim`) e o cálculo consolidado dos resumos executivos (Total, Média por subperíodo ativo, Maior e Menor subperíodo com movimento conforme **Opção A**).
  * Implementada em `RelatorioPdfGenerator` a geração dos documentos PDF A4 para o **Relatório Financeiro** e para o **Relatório de Ocupação**, formatados com cabeçalho institucional do EasyPark, metadados de auditoria (período, agrupamento, data/hora de geração e usuário logado), blocos de indicadores de resumo e tabelas completas.

### Etapa 4 — Camada de Apresentação: `RelatorioBean`, `relatorios.xhtml` e Menu Lateral (`[x] Concluída`)
* **Arquivos:**
  * `[CRIADO]` `src/main/java/ads/upf/presentation/RelatorioBean.java`
  * `[CRIADO]` `src/main/resources/META-INF/resources/pages/gestao/relatorios.xhtml`
  * `[MODIFICADO]` `src/main/resources/META-INF/resources/templates/layout.xhtml`
* **Entregas:**
  * Criado `RelatorioBean` (`@ViewScoped @Named("relatorioBean")`) com carregamento inicial automático do mês corrente, ações de atalho rápido (`aplicarAtalhoHoje`, `aplicarAtalhoSemana`, `aplicarAtalhoMes`, `aplicarAtalhoAno`), ação de filtragem e métodos de download de PDF (`DefaultStreamedContent`) para as duas abas.
  * Criada `pages/gestao/relatorios.xhtml` com cabeçalho `.page-header`, barra de filtros de período e agrupamento, `<p:tabView>` contendo a **Aba 1 (Relatório Financeiro)** e a **Aba 2 (Relatório de Ocupação)**, cards `.erp-card` com bordas laterais semânticas (`border-left-3`), tabelas `<p:dataTable>` compactas e botões de exportação **PDF** (`<p:fileDownload>`) e **CSV** (`<p:dataExporter type="csv" />`).
  * Adicionado o link **Relatórios** (`pi pi-file-pdf`) na seção *Monitoramento* da sidebar do Gerente em `layout.xhtml`.

### Etapa 5 — Testes Unitários e Verificação Final (`[x] Concluída`)
* **Arquivos:**
  * `[CRIADO]` `src/test/java/ads/upf/services/RelatorioServiceTest.java`
* **Entregas:**
  * Criados 8 testes unitários validando as regras de cálculo de resumo financeiro e de ocupação (totais, médias, identificação do maior e menor período com movimento segundo a Opção A, validação de datas inválidas, preenchimento de filtros padrão, delegação ao repositório) e a geração binária dos arquivos PDF (`%PDF-`).
  * Executada a suíte completa de testes unitários com sucesso (`Tests run: 214, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`), garantindo que todos os testes do projeto passam sem regressões.
