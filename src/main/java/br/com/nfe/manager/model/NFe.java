package br.com.nfe.manager.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class NFe {

    private String chaveAcesso;
    private String numero;
    private OffsetDateTime data;
    private Empresa empresa;
    private BigDecimal valorTotal;
    private List<ItemNFe> itens;

    public NFe(
            String chaveAcesso,
            String numero,
            OffsetDateTime data,
            Empresa empresa,
            BigDecimal valorTotal,
            List<ItemNFe> itens) {

        this.chaveAcesso = chaveAcesso;
        this.numero = numero;
        this.data = data;
        this.empresa = empresa;
        this.valorTotal = valorTotal;
        this.itens = itens;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public String getNumero() {
        return numero;
    }

    public OffsetDateTime getData() {
        return data;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemNFe> getItens() {
        return itens;
    }
}
