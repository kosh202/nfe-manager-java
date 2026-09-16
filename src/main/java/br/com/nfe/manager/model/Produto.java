package br.com.nfe.manager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "produto", indexes = {
    @Index(name = "idx_produto_codigo", columnList = "codigo", unique = true)
})
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    public Produto() {
    }

    public Produto(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
