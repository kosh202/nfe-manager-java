package br.com.nfe.manager.model;

public class Produto {

    private String codigo;
    private String nome;

    public Produto(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }
}
