package br.com.nfe.manager.dto;

import br.com.nfe.manager.model.ItemNFe;
import java.math.BigDecimal;

public class ItemNFeDTO {

    private Long id;
    private ProdutoDTO produto;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;

    public ItemNFeDTO() {
    }

    public ItemNFeDTO(Long id, ProdutoDTO produto, BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal valorTotal) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.valorTotal = valorTotal;
    }

    public static ItemNFeDTO fromEntity(ItemNFe item) {
        if (item == null) return null;
        return new ItemNFeDTO(
                item.getId(),
                ProdutoDTO.fromEntity(item.getProduto()),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getValorTotal()
        );
    }

    public Long getId() {
        return id;
    }

    public ProdutoDTO getProduto() {
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
