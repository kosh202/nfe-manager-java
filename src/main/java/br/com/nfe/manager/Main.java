package br.com.nfe.manager;

import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Empresa empresa = new Empresa(
                "00822602000124",
                "Plotag Sistemas e Suprimentos Ltda"
        );

        Produto produto = new Produto(
                "B17025056",
                "PAPEL MAXPLOT"
        );

        ItemNFe item = new ItemNFe(
                produto,
                new BigDecimal("1.0000"),
                new BigDecimal("138.3000"),
                new BigDecimal("138.30")
        );

        NFe nfe = new NFe(
                "35150300822602000124550010009923461099234656",
                "992346",
                OffsetDateTime.parse("2015-03-27T09:40:00-03:00"),
                empresa,
                new BigDecimal("689.91"),
                List.of(item)
        );

        System.out.println("NF-e: " + nfe.getNumero());
        System.out.println("Chave: " + nfe.getChaveAcesso());
        System.out.println("Empresa: " + nfe.getEmpresa().getNome());
        System.out.println("CNPJ: " + nfe.getEmpresa().getCnpj());
        System.out.println("Valor total: " + nfe.getValorTotal());

        System.out.println("\nItens:");

        for (ItemNFe itemNFe : nfe.getItens()) {
            System.out.println(
                    itemNFe.getProduto().getNome()
                    + " - "
                    + itemNFe.getQuantidade()
                    + " x "
                    + itemNFe.getPrecoUnitario()
            );
        }
    }
}
