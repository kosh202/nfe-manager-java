# NF-e Manager Java 📄🧾

Sistema Java desenvolvido com **Spring Boot 3** e **JPA/Hibernate** para gerenciamento de Notas Fiscais Eletrônicas (NF-e) a partir do processamento e armazenamento de arquivos XML.

O projeto foi construído com uma **arquitetura em camadas**, com foco em **segurança no processamento de XML (defesa contra XXE)**, **consultas dinâmicas flexíveis (Specification Pattern)** e documentação didática sobre decisões de design.

---

## 📋 Funcionalidades Principais

- 📥 **Importação de NF-e**: Upload e parsing de arquivos XML de Notas Fiscais Eletrônicas.
- 🔒 **Proteção contra XXE**: Parser XML configurado para rejeitar DTDs e entidades externas maliciosas.
- 🚫 **Prevenção de Duplicidade**: Detecção e bloqueio de notas fiscais com chave de acesso já importada.
- 🏢 **Gestão de Empresas e Produtos**: Mapeamento e consulta centralizada de empresas emitentes e produtos.
- 💾 **Armazenamento do XML Original**: Preservação do XML integral (`CLOB`) para auditoria e download via API.
- 🔍 **Consultas e Filtros Dinâmicos**: Pesquisa por CNPJ, nome da empresa, código/nome de produtos, intervalo de datas e faixa de valores com paginação.
- 🚀 **API REST Completa**: Endpoints estruturados com respostas JSON padronizadas e controle semântico de erros (HTTP 201, 400, 409, 404).

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 17 (compatível com Java 21)
- **Framework**: Spring Boot 3.2.5
- **Persistência**: Spring Data JPA / Hibernate
- **Banco de Dados**: H2 (In-Memory para desenvolvimento/testes; configurável para PostgreSQL)
- **Testes**: JUnit 5, Mockito, Spring Boot Test (`MockMvc`)
- **Gerenciador de Dependências**: Maven 3.8.7
- **Ambiente Recomendado**: Linux / WSL (Windows Subsystem for Linux)

---

## 📐 Arquitetura e Modelo de Domínio

O sistema respeita rigorosamente os relacionamentos do domínio fiscal:

```text
Empresa (1) ─── (N) NFe (1) ─── (N) ItemNFe (N) ─── (1) Produto
```

- **`Empresa`**: Representa o emitente da nota (CNPJ único, Nome).
- **`NFe`**: Representa a Nota Fiscal Eletrônica (Chave de Acesso única, Número, Data de Emissão, Valor Total, XML Original).
- **`Produto`**: Representa o produto informado pelo emitente, identificado pelo código utilizado na NF-e e seu nome. Nesta primeira versão, o código é tratado como globalmente único como uma simplificação do modelo.
- **`ItemNFe`**: Item da nota relacionando a NF-e e o Produto com quantidade, preço unitário e valor total.

---

## 🧱 Design Patterns Aplicados e Justificados

| Padrão | Onde é Aplicado | Problema que Resolve |
| :--- | :--- | :--- |
| **Repository** | `br.com.nfe.manager.repository.*` | Abstração da camada de persistência com Spring Data JPA (`NFeRepository`, `EmpresaRepository`, `ProdutoRepository`), eliminando a necessidade de SQL manual. |
| **Specification** | `br.com.nfe.manager.repository.specification.*` | Construção de consultas dinâmicas tipo *type-safe* via Criteria API para buscas por múltiplos parâmetros sem concatenação manual de queries. |
| **DTO (Data Transfer Object)** | `br.com.nfe.manager.dto.*` | Desacoplamento entre os modelos de banco de dados e os contratos de requisição/resposta da API REST. |

> 📌 **Pragmático e Sem Overengineering:** Padrões complexos como *Chain of Responsibility* e *Strategy* foram avaliados e descartados por não agregarem valor real para a complexidade atual do sistema. Consulte [`docs/DECISOES.md`](docs/DECISOES.md) para os registros detalhados (ADRs).

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Java 17** ou superior (testado no OpenJDK 21)
- **Maven 3.8.7** ou superior

### Executando via Terminal / WSL

1. Navegue até o diretório do projeto:
   ```bash
   cd /mnt/e/github/nfe-manager-java
   ```

2. Inicie a aplicação via Maven:
   ```bash
   mvn spring-boot:run
   ```

3. A API estará disponível em: `http://localhost:8080`
   - Console do H2 Database: `http://localhost:8080/h2-console`
     - **JDBC URL:** `jdbc:h2:mem:nfedb`
     - **User:** `sa`
     - **Password:** *(vazio)*

---

## 🧪 Como Executar os Testes

Execute a suíte de testes unitários e de integração com o comando:

```bash
mvn clean test
```

---

## 📡 Exemplos de Utilização da API REST

### 1. Importar um XML de NF-e

**Body `application/xml` (Raw XML):**
```bash
curl -X POST http://localhost:8080/api/nfe/importar \
  -H "Content-Type: application/xml" \
  -d @src/main/resources/xml/nfe-exemplo-01.xml
```

**Ou Multipart Form Upload (`file`):**
```bash
curl -X POST http://localhost:8080/api/nfe/importar \
  -F "file=@src/main/resources/xml/nfe-exemplo-01.xml"
```

**Resposta de Sucesso (HTTP 201 Created):**
```json
{
  "mensagem": "Nota fiscal importada com sucesso.",
  "chaveAcesso": "43211105730928000145650010000002401717268120",
  "id": 1
}
```

---

### 2. Pesquisar Notas Fiscais com Filtros e Paginação

```bash
curl "http://localhost:8080/api/nfe?cnpj=42530613000180&valorMin=1.00&page=0&size=10"
```

---

### 3. Obter Detalhes de uma NF-e por ID ou Chave de Acesso

```bash
curl http://localhost:8080/api/nfe/1
curl http://localhost:8080/api/nfe/chave/43211105730928000145650010000002401717268120
```

---

### 4. Download do XML Original da NF-e

```bash
curl http://localhost:8080/api/nfe/1/xml -o nfe-original.xml
```

---

### 5. Consultar Empresas e Produtos

```bash
curl http://localhost:8080/api/empresas
curl http://localhost:8080/api/produtos
```

---

## 📚 Documentação Adicional para Estudo

- [`docs/ESTUDO.md`](docs/ESTUDO.md): Guia didático sobre parsing de XML, mitigação de XXE, mapeamento JPA e os padrões de projeto aplicados.
- [`docs/DECISOES.md`](docs/DECISOES.md): Registro de Decisões de Arquitetura (ADRs) detalhando as escolhas técnicas.
