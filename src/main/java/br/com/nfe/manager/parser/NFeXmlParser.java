package br.com.nfe.manager.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;

import java.io.File;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class NFeXmlParser {

    public Document carregarXml(String caminho) throws Exception {

        File arquivo = new File(caminho);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(arquivo);
    }

    public String extrairChaveAcesso(Document documento) {

        Element infNFe = (Element) documento
                .getElementsByTagName("infNFe")
                .item(0);

        String id = infNFe.getAttribute("Id");

        return id.substring(3);
    }

    public String extrairNumero(Document documento) {

        Element nNF = (Element) documento
                .getElementsByTagName("nNF")
                .item(0);

        return nNF.getTextContent();
    }

    public String extrairCnpj(Document documento) {

        Element emit = (Element) documento
                .getElementsByTagName("emit")
                .item(0);

        Element cnpj = (Element) emit
                .getElementsByTagName("CNPJ")
                .item(0);

        return cnpj.getTextContent();
    }

    public String extrairNomeEmpresa(Document documento) {

        Element emit = (Element) documento
                .getElementsByTagName("emit")
                .item(0);

        Element xNome = (Element) emit
                .getElementsByTagName("xNome")
                .item(0);

        return xNome.getTextContent();
    }

    public OffsetDateTime extrairData(Document documento) {

        Element dhEmi = (Element) documento
                .getElementsByTagName("dhEmi")
                .item(0);

        return OffsetDateTime.parse(dhEmi.getTextContent());
    }

    public BigDecimal extrairValorTotal(Document documento) {

        Element vNF = (Element) documento
                .getElementsByTagName("vNF")
                .item(0);

        return new BigDecimal(vNF.getTextContent());
    }

    public int contarItens(Document documento) {

        return documento
                .getElementsByTagName("det")
                .getLength();
    }

    public List<ItemNFe> extrairItens(Document documento) {

        NodeList itens = documento.getElementsByTagName("det");

        List<ItemNFe> listaItens = new ArrayList<>();

        for (int i = 0; i < itens.getLength(); i++) {

            Element item = (Element) itens.item(i);

            Element produtoElement = (Element) item
                    .getElementsByTagName("prod")
                    .item(0);

            Element codigo = (Element) produtoElement
                    .getElementsByTagName("cProd")
                    .item(0);

            Element nome = (Element) produtoElement
                    .getElementsByTagName("xProd")
                    .item(0);

            Element quantidade = (Element) produtoElement
                    .getElementsByTagName("qCom")
                    .item(0);

            Element precoUnitario = (Element) produtoElement
                    .getElementsByTagName("vUnCom")
                    .item(0);

            Element valorTotal = (Element) produtoElement
                    .getElementsByTagName("vProd")
                    .item(0);

            Produto produto = new Produto(
                    codigo.getTextContent(),
                    nome.getTextContent());

            BigDecimal quantidadeValor = new BigDecimal(
                    quantidade.getTextContent());

            BigDecimal precoUnitarioValor = new BigDecimal(
                    precoUnitario.getTextContent());

            BigDecimal valorTotalValor = new BigDecimal(
                    valorTotal.getTextContent());

            ItemNFe itemNFe = new ItemNFe(
                    produto,
                    quantidadeValor,
                    precoUnitarioValor,
                    valorTotalValor);

            listaItens.add(itemNFe);
        }

        return listaItens;
    }

    public Empresa extrairEmpresa(Document documento) {

        String cnpj = extrairCnpj(documento);
        String nome = extrairNomeEmpresa(documento);

        return new Empresa(cnpj, nome);
    }

    public NFe extrairNFe(Document documento) {

        String chaveAcesso = extrairChaveAcesso(documento);
        String numero = extrairNumero(documento);
        OffsetDateTime data = extrairData(documento);
        Empresa empresa = extrairEmpresa(documento);
        BigDecimal valorTotal = extrairValorTotal(documento);
        List<ItemNFe> itens = extrairItens(documento);

        return new NFe(
                chaveAcesso,
                numero,
                data,
                empresa,
                valorTotal,
                itens);
    }
}