# Refatoração de UI/UX das telas XHTML

## Contexto

Quero modernizar a interface visual da minha aplicação, que utiliza arquivos `.xhtml`.

O objetivo é transformar as telas existentes em uma interface mais moderna, responsiva, intuitiva e consistente, com aparência profissional de um **sistema de gestão e controle (ERP)**.

## Objetivo principal

Analise as telas `.xhtml` existentes e identifique oportunidades de melhoria na estrutura visual, estilização, usabilidade, responsividade e consistência entre as páginas.

A interface deve transmitir organização, clareza e profissionalismo, priorizando a eficiência das tarefas realizadas pelos usuários.

## Etapa 1 — Análise do projeto

Antes de modificar qualquer arquivo:

1. Inspecione a estrutura do projeto e identifique as tecnologias utilizadas na camada de apresentação.
2. Analise os arquivos `.xhtml` relevantes, seus templates, componentes reutilizáveis, folhas de estilo CSS e recursos JavaScript associados.
3. Identifique o framework visual existente, como Bootstrap, PrimeFaces ou outras bibliotecas, caso estejam presentes.
4. Verifique como as páginas compartilham layouts, menus, cabeçalhos, formulários, tabelas, botões, diálogos e outros componentes.
5. Identifique inconsistências visuais, estilos duplicados, problemas de responsividade e possíveis dificuldades de navegação.
6. Verifique as convenções e os padrões já adotados pelo projeto antes de propor mudanças.

Não presuma quais tecnologias, bibliotecas ou componentes estão sendo utilizados. Confirme essas informações por meio da inspeção do código.

## Etapa 2 — Proposta de melhorias

Com base na análise, apresente um plano de refatoração priorizado, contemplando:

- **Identidade visual:** paleta de cores coerente, tipografia legível, espaçamentos consistentes, bordas, sombras e hierarquia visual.
- **Layout:** organização de cabeçalhos, menus laterais, barras de navegação, áreas de conteúdo e painéis informativos, quando aplicável.
- **Formulários:** alinhamento de campos, agrupamento lógico, identificação clara de campos obrigatórios, mensagens de validação e organização das ações.
- **Tabelas e listagens:** melhor legibilidade, hierarquia das informações, espaçamento, ações por registro e adaptação a telas menores.
- **Botões e ações:** diferenciação visual entre ações primárias, secundárias, destrutivas e auxiliares.
- **Responsividade:** adaptação para desktops e notebooks.
- **Usabilidade e acessibilidade:** contraste adequado, navegação por teclado, rótulos claros, estados de foco e feedback visual para as ações do usuário.
- **Consistência:** criação de padrões visuais reutilizáveis para evitar que cada tela tenha uma aparência diferente.

Priorize soluções adequadas a um sistema administrativo, evitando excesso de elementos decorativos, animações desnecessárias ou estilos que prejudiquem a produtividade.

## Etapa 3 — Estratégia de implementação

Após concluir a análise e apresentar o plano, aguarde minha aprovação antes de iniciar as alterações.

Com a aprovação:

1. Implemente as melhorias de forma incremental, começando pelos elementos compartilhados e pelas telas de maior impacto.
2. Reutilize templates, componentes e estilos existentes sempre que fizer sentido.
3. Centralize os padrões visuais em arquivos CSS compartilhados ou em uma estrutura equivalente já adotada pelo projeto.
4. Evite duplicação de código e regras CSS excessivamente específicas.
5. Preserve a compatibilidade com a arquitetura e as tecnologias existentes.
6. Mantenha a separação adequada entre estrutura XHTML, estilização CSS e comportamento JavaScript.
7. Faça alterações pequenas e verificáveis, validando os resultados antes de avançar para outras telas.

## Restrições e cuidados

- Não altere regras de negócio, consultas, serviços, entidades ou outras partes da aplicação que não sejam necessárias para a refatoração visual.
- Preserve os atributos, expressões EL, componentes JSF, namespaces, IDs, bindings, ações, validações e demais elementos necessários ao funcionamento das páginas.
- Não remova funcionalidades existentes para simplificar a interface.
- Não substitua bibliotecas ou frameworks sem justificativa técnica e aprovação prévia.
- Evite adicionar dependências externas quando os recursos existentes forem suficientes.
- Não reescreva telas inteiras sem necessidade: prefira mudanças pontuais, consistentes e justificadas.
- Não considere uma tela responsiva apenas porque utiliza larguras relativas; avalie o comportamento real dos componentes e do conteúdo em diferentes resoluções.
- Não declare testes ou validações como concluídos sem executá-los efetivamente.

## Critérios de qualidade

Considere a refatoração bem-sucedida quando:

- As telas apresentarem identidade visual consistente.
- A hierarquia das informações facilitar a leitura e a localização das ações.
- Formulários, tabelas, filtros e botões forem visualmente organizados.
- A interface funcionar adequadamente em diferentes tamanhos de tela.
- Os componentes reutilizáveis reduzirem a duplicação de estilos.
- A navegação e as interações existentes permanecerem funcionais.
- O código continuar compatível com a arquitetura atual.
- As alterações puderem ser verificadas por inspeção visual e pelos testes disponíveis no projeto.

## Entregáveis

Na fase inicial, apresente:

1. Um resumo da arquitetura atual da camada de apresentação.
2. Os principais problemas identificados nas telas.
3. As oportunidades de melhoria, organizadas por prioridade e impacto.
4. Uma proposta de identidade visual e organização dos componentes.
5. Um plano de execução dividido em etapas, indicando os arquivos ou grupos de telas afetados.

Após a aprovação e a implementação, apresente:

1. Um resumo das alterações realizadas.
2. Os arquivos modificados e a justificativa de cada mudança relevante.
3. Os testes e as verificações efetivamente executados, com seus resultados.
4. Os problemas encontrados que ainda precisem de atenção.
5. As sugestões de melhorias futuras que ficaram fora do escopo.

## Instrução final

**Neste primeiro momento, execute somente a análise e elabore o plano de refatoração. Não modifique arquivos até que eu aprove a proposta.**

Baseie todas as recomendações no código real do projeto. Quando houver dúvidas sobre a arquitetura ou sobre o comportamento esperado de uma tela, investigue os arquivos relacionados antes de propor uma solução.