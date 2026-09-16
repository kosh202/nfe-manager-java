# Registro de Decisões de Arquitetura (ADR) - nfe-manager-java

Este documento registra as principais decisões arquiteturais do projeto, suas motivações, contexto e consequências.

---

## ADR-001: Adoção do Spring Boot 3, Spring Data JPA e H2 Database

### Contexto
O projeto exigia evolução de um script Java standalone para um sistema gerenciador de NF-e estruturado, persistente e exposto via API REST.

### Decisão
Adotar **Spring Boot 3.2.x** com **Spring Data JPA / Hibernate** e banco de dados **H2** em memória (configurável para PostgreSQL).

### Consequências
- **Positivas**:
  - Abstração eficiente do banco de dados relacional através de interfaces Repository.
  - Facilidade de desenvolvimento e execução imediata sem necessidade de subir contêineres externos (banco H2 in-memory).
  - Suporte completo a APIs REST via Spring WebController e Bean Validation.
- **Negativas**:
  - Dependência do ecossistema Spring.

---

## ADR-002: Segurança contra XXE (XML External Entity) Diretamente no Parser

### Contexto
Como o sistema recebe e processa arquivos XML arbitrários de notas fiscais enviados pelos usuários, havia o risco de vulnerabilidade **XXE (XML External Entity)**, que permite vazamento de arquivos do sistema operacional ou ataques SSRF através de entidades DTD externas.

### Decisão
Implementar as proteções de segurança diretamente na configuração do `DocumentBuilderFactory` dentro da classe `NFeXmlParser`, desativando declarações `DOCTYPE` e entidades externas.

```java
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
factory.setXIncludeAware(false);
factory.setExpandEntityReferences(false);
```

### Justificativa de Design
Evitou-se a criação de um "pipeline" separado ou de wrappers redundantes apenas para validação de segurança. Colocar o hardening dentro do próprio parser garante que todo documento XML processado pelo sistema esteja naturalmente seguro na fonte.

---

## ADR-003: Rejeição de Overengineering (Sem Chain of Responsibility ou Strategy Artificiais)

### Contexto
Avaliou-se a inclusão dos padrões *Chain of Responsibility* (para o fluxo de importação) e *Strategy* (para o armazenamento de XML).

### Decisão
**Não utilizar** Chain of Responsibility nem Strategy neste momento.

### Justificativa
1. **Chain of Responsibility**: O fluxo de importação atual possui apenas uma sequência linear e coesa de passos (`Parse XML` -> `Verificar Duplicidade por Chave` -> `Persistir Empresa/Produto/NFe`). Criar classes para cada etapa aumentaria drasticamente a complexidade do código sem trazer benefícios práticos.
2. **Strategy para Armazenamento**: O armazenamento do XML original é feito de forma simples e direta no banco de dados (`CLOB`). Não há necessidade de múltiplos provedores (ex.: S3 vs File System) no escopo atual.

---

## ADR-004: Uso do Specification Pattern (JPA Specification) para Consultas Dinâmicas

### Contexto
O sistema precisa permitir que os usuários pesquisem notas fiscais usando combinações arbitrárias de filtros (CNPJ, nome da empresa, código/nome do produto, faixa de valores e intervalo de datas).

### Decisão
Implementar o padrão **Specification** através de `org.springframework.data.jpa.domain.Specification<NFe>`.

### Justificativa
Sem o Specification Pattern, a consulta exigiria SQL/JPQL dinâmico concatenado manualmente ou métodos redundantes de repositório. O Specification permite compor predicados de forma *type-safe*, legível e altamente sustentável.

---

## ADR-005: Preservação e Armazenamento do XML Original

### Contexto
Notas Fiscais Eletrônicas exigem a manutenção do arquivo XML original para fins de auditoria e download posterior.

### Decisão
Armazenar a string XML integral no campo `xmlOriginal` anotado com `@Lob` (`CLOB`) na entidade `NFe`. Disponibilizar o endpoint `GET /api/nfe/{id}/xml` para recuperar o arquivo original com os headers de download apropriados.

---

## ADR-006: Compatibilidade com WSL (Windows Subsystem for Linux)

### Contexto
O ambiente de desenvolvimento do projeto é baseado em WSL (Ubuntu/Linux) executando Java 21 OpenJDK e Maven 3.8.7.

### Decisão
Manter total compatibilidade com execução de build e testes via bash no WSL (`mvn clean test`), garantindo que o target de compilação permaneça Java 17 no `pom.xml`.

---

## ADR-007: Solução para o Problema N+1 em Consultas Paginadas de NF-e

### Contexto
Na consulta paginada de NF-e (`NFeService.pesquisar`), o mapeamento da entidade `NFe` para `NFeResponseDTO` acessa a empresa emitente e a lista de itens. O uso ingênuo de `JOIN FETCH` ou `@EntityGraph` para a coleção `@OneToMany` (`itens`) em uma consulta paginada (`Pageable`) faz com que o Hibernate realize a paginação em memória (emitindo um aviso e carregando todas as linhas do banco), pois o JOIN multiplica as linhas do resultado SQL antes de aplicar o `LIMIT/OFFSET`.

### Decisão
Adotar uma solução combinada eficiente e 100% nativa do JPA/Hibernate:
1. **`@EntityGraph(attributePaths = {"empresa"})`** no método `NFeRepository.findAll(spec, pageable)`: Como `Empresa` é `@ManyToOne` (To-One), o EAGER fetch via EntityGraph não multiplica linhas e é efetuado diretamente na query SQL paginada em 1 único JOIN.
2. **`@BatchSize(size = 25)`** na coleção `itens` na classe `NFe`: Quando a página de notas é mapeada para DTO, o Hibernate busca os itens de todas as NF-es daquela página em um **único lote SQL** usando a cláusula `WHERE nfe_id IN (?, ?, ...)`.

### Consequências
- **Positivas**:
  - Elimina completamente o problema N+1 (reduz N+1 queries para apenas 2 queries SQL: a query da página de notas com Empresa e a query em lote dos itens).
  - Preserva a paginação SQL nativa no banco de dados (`LIMIT / OFFSET` / `FETCH FIRST n ROWS ONLY`).
  - Totalmente compatível com `NFeSpecification` e `Pageable`.

