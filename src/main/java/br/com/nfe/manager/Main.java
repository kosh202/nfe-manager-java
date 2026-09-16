package br.com.nfe.manager;

import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.parser.NFeXmlParser;

import org.w3c.dom.Document;

public class Main {

    public static void main(String[] args) throws Exception {

        NFeXmlParser parser = new NFeXmlParser();

        Document documento = parser.carregarXml(
                "src/main/resources/xml/nfe-exemplo-01.xml");

        NFe nfe = parser.extrairNFe(documento);

        System.out.println("Chave de acesso: " + nfe.getChaveAcesso());
        System.out.println("Número da NF-e: " + nfe.getNumero());
        System.out.println("CNPJ: " + nfe.getEmpresa().getCnpj());
        System.out.println("Empresa: " + nfe.getEmpresa().getNome());
        System.out.println("Data: " + nfe.getData());
        System.out.println("Valor total: " + nfe.getValorTotal());
        System.out.println("Quantidade de itens: " + nfe.getItens().size());

        for (ItemNFe item : nfe.getItens()) {

            System.out.println("Código: " + item.getProduto().getCodigo());
            System.out.println("Nome: " + item.getProduto().getNome());
            System.out.println("Quantidade: " + item.getQuantidade());
            System.out.println("Preço unitário: " + item.getPrecoUnitario());
            System.out.println("Valor total: " + item.getValorTotal());
        }
    }
}