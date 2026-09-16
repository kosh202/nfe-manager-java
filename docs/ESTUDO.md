# Guia de Estudo - NFe Manager Java

Este documento foi elaborado para servir como material didático para o estudo de **Java, Spring Boot, JPA/Hibernate, Segurança XML e Design Patterns** aplicados em um projeto real de gerenciamento de Notas Fiscais Eletrônicas (NF-e).

---

## 1. O que é a NF-e e a Leitura de XML com DOM Parser

A NF-e (Nota Fiscal Eletrônica) é um documento digital emitido no formato XML. No Java, uma das formas mais tradicionais de ler XML é a **DOM (Document Object Model)**.

### Como funciona o DOM Parser?
O DOM Parser carrega todo o documento XML em memória e cria uma árvore de nós (`Node` e `Element`). Isso nos permite navegar livremente pelo XML usando a tag principal e extrair valores de atributos e subelementos.

**Exemplo no código:**
```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
DocumentBuilder builder = factory.newDocumentBuilder();
Document documento = builder.parse(arquivoXml);

// Extraindo a tag <nNF> (número da nota)
NodeList nNFList = documento.getElementsByTagName("nNF");
String numeroNota = nNFList.item(0).getTextContent();
```

---

## 2. Segurança XML: Entendendo o Ataque XXE (XML External Entity)

### O que é XXE?
O ataque XXE ocorre quando um parser XML aceita referências a **entidades externas** (DTDs). Um atacante pode enviar um XML malicioso para ler arquivos confidenciais do servidor (como `/etc/passwd`) ou disparar requisições internas (SSRF).

**Exemplo de XML Malicioso:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
<NFe>
    <emit><xNome>&xxe;</xNome></emit>
</NFe>
```

### Como prevenimos no `NFeXmlParser`?
No Java, desativamos as declarações DTD diretamente na fábrica de parsers (`DocumentBuilderFactory`):

```java
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
factory.setXIncludeAware(false);
factory.setExpandEntityReferences(false);
```

---

## 3. Modelo de Domínio e Mapeamento Objeto-Relacional (JPA)

O modelo do sistema segue os relacionamentos conceituais exigidos pela regra de negócio:

```text
Empresa (1) ─── (N) NFe (1) ─── (N) ItemNFe (N) ─── (1) Produto
```

### Conceitos Chave Mapeados no JPA:

1. **`@Entity` & `@Table`**: Indica ao Hibernate que a classe representa uma tabela no banco de dados.
2. **`@ManyToOne`**: Mapeia o relacionamento N:1. Por exemplo, várias NF-es pertencem a uma única `Empresa`.
3. **`@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`**: Garante que quando uma `NFe` é salva ou excluída, seus itens (`ItemNFe`) são automaticamente persistidos ou limpos.
4. **`@Lob` / `CLOB`**: Utilizado no campo `xmlOriginal` para armazenar o texto completo do arquivo XML sem limites pequenos de caracteres de colunas `VARCHAR`.

---

## 4. Design Patterns Explicados na Prática

### A. Repository Pattern
* **Problema:** Evitar que o código de negócio escreva consultas SQL brutas ou interaja diretamente com conexões de banco de dados.
* **Solução:** Interfaces Spring Data JPA (`JpaRepository<T, ID>`).

```java
public interface NFeRepository extends JpaRepository<NFe, Long>, JpaSpecificationExecutor<NFe> {
    Optional<NFe> findByChaveAcesso(String chaveAcesso);
    boolean existsByChaveAcesso(String chaveAcesso);
}
```

### B. Specification Pattern (Filtros Dinâmicos)
* **Problema:** Como permitir pesquisas com múltiplos filtros opcionais (ex: buscar por CNPJ E faixa de preço E código do produto) sem criar dezenas de métodos SQL concatenados?
* **Solução:** O Specification Pattern encapsula a lógica de predicados da **Criteria API** do JPA de forma declarativa e combinável:

```java
public static Specification<NFe> comFiltros(NFeFilterDTO filter) {
    return (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        if (filter.getCnpj() != null) {
            predicates.add(cb.equal(root.join("empresa").get("cnpj"), filter.getCnpj()));
        }
        if (filter.getValorMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("valorTotal"), filter.getValorMin()));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    };
}
```

### C. DTO (Data Transfer Object)
* **Problema:** Não expor o modelo interno do banco de dados diretamente na API REST, evitando acoplamento e recursão infinita no JSON em relacionamentos bidirecionais.
* **Solução:** As classes `NFeResponseDTO`, `EmpresaDTO` e `ProdutoDTO` isolam o contrato da API.

---

## 5. Tratamento de Exceções na API REST

Utilizamos o `@RestControllerAdvice` no `GlobalExceptionHandler` para capturar exceções lançadas pela camada de serviço e convertê-las em respostas JSON com códigos de status HTTP semânticos:

- **HTTP 201 Created**: Nota importada com sucesso.
- **HTTP 409 Conflict**: Nota fiscal duplicada (chave de acesso já existente).
- **HTTP 400 Bad Request**: XML inválido ou erro de parsing.
- **HTTP 404 Not Found**: Recursos não encontrados.

---

## 6. O Problema N+1 e a Solução por Batch Fetching + EntityGraph

### O que é o Problema N+1?
Ocorre quando o Hibernate executa 1 consulta inicial para buscar $N$ registros (ex.: 20 notas fiscais em uma página) e, em seguida, dispara **$N$ consultas adicionais no banco de dados** para carregar os relacionamentos de cada registro individualmente (ex.: buscar os itens de cada uma das 20 notas).

### Por que não usar `JOIN FETCH` em consultas paginadas?
Se usarmos `JOIN FETCH` ou `@EntityGraph` para trazer a coleção de itens de uma `@OneToMany` em uma consulta com `Pageable`, o Hibernate faz um JOIN entre NFe e ItemNFe. Isso multiplica as linhas do resultado SQL (uma nota com 5 itens gera 5 linhas na query). Como a paginação SQL (`LIMIT` / `OFFSET`) atua em linhas da tabela e não em entidades de nota fiscal, aplicar pagination em um JOIN de coleção forçaria o Hibernate a trazer **todas as linhas do banco de dados para a memória** para realizar a paginação via código Java, gerando um aviso grave de degradação de performance (`firstResult/maxResults specified with collection fetch; applying in memory!`).

### A Solução Aplicada no Projeto:
1. **`@EntityGraph(attributePaths = {"empresa"})`**: Como `Empresa` é um relacionamento `@ManyToOne` (To-One), ela não multiplica linhas. O `@EntityGraph` no repositório instrui o JPA a fazer o JOIN com a tabela `Empresa` diretamente na SQL paginada em 1 única query.
2. **`@BatchSize(size = 25)`**: Adicionado na coleção `itens` da entidade `NFe`. Quando o controller itera pelas notas fiscais da página e acessa `nfe.getItens()`, o Hibernate busca os itens de até 25 notas em uma **única query em lote** usando a cláusula `WHERE nfe_id IN (?, ?, ...)`.

Com isso, reduzimos o que seriam dezenas de consultas separadas para **apenas 2 consultas SQL extremamente otimizadas**, mantendo a paginação $100\%$ nativa no banco de dados.

