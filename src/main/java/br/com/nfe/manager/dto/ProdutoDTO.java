package br.com.nfe.manager.dto;

import br.com.nfe.manager.model.Produto;

/** Representação pública de um produto para as respostas da API. */
public class ProdutoDTO {

    private Long id;
    private String codigo;
    private String nome;

    public ProdutoDTO() {
    }

    public ProdutoDTO(Long id, String codigo, String nome) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
    }

    public static ProdutoDTO fromEntity(Produto produto) {
        // Retorna null para preservar a ausência do relacionamento na origem.
        if (produto == null) return null;
        return new ProdutoDTO(produto.getId(), produto.getCodigo(), produto.getNome());
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }
}
