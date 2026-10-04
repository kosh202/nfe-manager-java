package br.com.nfe.manager.dto;

import br.com.nfe.manager.model.ItemNFe;
import java.math.BigDecimal;

/** Dados de um item de NF-e usados no contrato de resposta da API. */
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
        // O produto também é convertido para DTO, evitando entidades aninhadas na resposta.
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
