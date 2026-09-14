package br.com.nfe.manager;

import br.com.nfe.manager.parser.NFeXmlParser;
import br.com.nfe.manager.model.ItemNFe;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.w3c.dom.Document;

public class Main {

    public static void main(String[] args) throws Exception {

        NFeXmlParser parser = new NFeXmlParser();

        Document documento = parser.carregarXml(
                "src/main/resources/xml/nfe-exemplo-01.xml");

        String chave = parser.extrairChaveAcesso(documento);
        String numero = parser.extrairNumero(documento);
        String cnpj = parser.extrairCnpj(documento);
        String nomeEmpresa = parser.extrairNomeEmpresa(documento);
        OffsetDateTime data = parser.extrairData(documento);
        BigDecimal valorTotal = parser.extrairValorTotal(documento);

        int quantidadeItens = parser.contarItens(documento);

        ItemNFe item = parser.extrairPrimeiroItem(documento);

        System.out.println("Código: " + item.getProduto().getCodigo());
        System.out.println("Nome: " + item.getProduto().getNome());
        System.out.println("Quantidade: " + item.getQuantidade());
        System.out.println("Preço unitário: " + item.getPrecoUnitario());
        System.out.println("Valor total: " + item.getValorTotal());

        System.out.println("Chave de acesso: " + chave);
        System.out.println("Número da NF-e: " + numero);
        System.out.println("CNPJ: " + cnpj);
        System.out.println("Empresa: " + nomeEmpresa);
        System.out.println("Data: " + data);
        System.out.println("Valor total: " + valorTotal);
        System.out.println("Quantidade de itens: " + quantidadeItens);
    }
}