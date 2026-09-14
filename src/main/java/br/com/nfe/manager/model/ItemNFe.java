package br.com.nfe.manager.model;

import java.math.BigDecimal;

public class ItemNFe {

    private Produto produto;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;

    public ItemNFe(
            Produto produto,
            BigDecimal quantidade,
            BigDecimal precoUnitario,
            BigDecimal valorTotal) {

        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.valorTotal = valorTotal;
    }

    public Produto getProduto() {
        return produto;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
