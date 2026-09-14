package br.com.nfe.manager.model;

public class Empresa {
    private String cnpj;
    private String nome;

    public Empresa(String cnpj, String nome) {
        this.cnpj = cnpj;
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getNome() {
        return nome;
    }
}
