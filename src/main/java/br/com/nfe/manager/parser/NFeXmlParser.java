package br.com.nfe.manager.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.com.nfe.manager.exception.XmlParsingException;
import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class NFeXmlParser {

    private DocumentBuilderFactory criarDocumentBuilderFactorySegura() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Mitigação de vulnerabilidades XXE (XML External Entity)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory;
    }

    public Document carregarXml(String caminho) {
        return carregarXml(new File(caminho));
    }

    public Document carregarXml(File arquivo) {
        try {
            DocumentBuilderFactory factory = criarDocumentBuilderFactorySegura();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(arquivo);
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao carregar arquivo XML: " + e.getMessage(), e);
        }
    }

    public Document carregarXml(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = criarDocumentBuilderFactorySegura();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao processar stream XML: " + e.getMessage(), e);
        }
    }

    public Document carregarXmlFromString(String xmlContent) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new XmlParsingException("Conteúdo XML não pode ser nulo ou vazio.");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
        return carregarXml(inputStream);
    }

    public String extrairChaveAcesso(Document documento) {
        try {
            NodeList infNFeList = documento.getElementsByTagName("infNFe");
            if (infNFeList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'infNFe' não encontrado no XML.");
            }
            Element infNFe = (Element) infNFeList.item(0);
            String id = infNFe.getAttribute("Id");
            if (id == null || id.isEmpty()) {
                throw new XmlParsingException("Atributo 'Id' da tag 'infNFe' ausente.");
            }
            return id.startsWith("NFe") ? id.substring(3) : id;
        } catch (XmlParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair chave de acesso: " + e.getMessage(), e);
        }
    }

    public String extrairNumero(Document documento) {
        try {
            NodeList nNFList = documento.getElementsByTagName("nNF");
            if (nNFList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'nNF' (número da nota) não encontrado.");
            }
            return nNFList.item(0).getTextContent();
        } catch (XmlParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair número da NF-e: " + e.getMessage(), e);
        }
    }

    public String extrairCnpj(Document documento) {
        try {
            NodeList emitList = documento.getElementsByTagName("emit");
            if (emitList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'emit' (emitente) não encontrado.");
            }
            Element emit = (Element) emitList.item(0);
            NodeList cnpjList = emit.getElementsByTagName("CNPJ");
            if (cnpjList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'CNPJ' do emitente não encontrado.");
            }
            return cnpjList.item(0).getTextContent();
        } catch (XmlParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair CNPJ da empresa emitente: " + e.getMessage(), e);
        }
    }

    public String extrairNomeEmpresa(Document documento) {
        try {
            NodeList emitList = documento.getElementsByTagName("emit");
            if (emitList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'emit' (emitente) não encontrado.");
            }
            Element emit = (Element) emitList.item(0);
            NodeList xNomeList = emit.getElementsByTagName("xNome");
            if (xNomeList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'xNome' do emitente não encontrado.");
            }
            return xNomeList.item(0).getTextContent();
        } catch (XmlParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair nome da empresa emitente: " + e.getMessage(), e);
        }
    }

    public OffsetDateTime extrairData(Document documento) {
        try {
            NodeList dhEmiList = documento.getElementsByTagName("dhEmi");
            if (dhEmiList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'dhEmi' (data de emissão) não encontrado.");
            }
            return OffsetDateTime.parse(dhEmiList.item(0).getTextContent());
        } catch (XmlParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair data de emissão: " + e.getMessage(), e);
        }
    }

    public BigDecimal extrairValorTotal(Document documento) {
        try {
            NodeList vNFList = documento.getElementsByTagName("vNF");
            if (vNFList.getLength() == 0) {
                throw new XmlParsingException("Elemento 'vNF' (valor total) não encontrado.");
            }
            return new BigDecimal(vNFList.item(0).getTextContent());
        } catch (XmlParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair valor total da nota: " + e.getMessage(), e);
        }
    }

    public int contarItens(Document documento) {
        return documento.getElementsByTagName("det").getLength();
    }

    public List<ItemNFe> extrairItens(Document documento) {
        try {
            NodeList itens = documento.getElementsByTagName("det");
            List<ItemNFe> listaItens = new ArrayList<>();

            for (int i = 0; i < itens.getLength(); i++) {
                Element item = (Element) itens.item(i);
                Element produtoElement = (Element) item.getElementsByTagName("prod").item(0);

                String codigo = produtoElement.getElementsByTagName("cProd").item(0).getTextContent();
                String nome = produtoElement.getElementsByTagName("xProd").item(0).getTextContent();
                String quantidadeStr = produtoElement.getElementsByTagName("qCom").item(0).getTextContent();
                String precoUnitarioStr = produtoElement.getElementsByTagName("vUnCom").item(0).getTextContent();
                String valorTotalStr = produtoElement.getElementsByTagName("vProd").item(0).getTextContent();

                Produto produto = new Produto(codigo, nome);
                BigDecimal quantidade = new BigDecimal(quantidadeStr);
                BigDecimal precoUnitario = new BigDecimal(precoUnitarioStr);
                BigDecimal valorTotal = new BigDecimal(valorTotalStr);

                ItemNFe itemNFe = new ItemNFe(produto, quantidade, precoUnitario, valorTotal);
                listaItens.add(itemNFe);
            }

            return listaItens;
        } catch (Exception e) {
            throw new XmlParsingException("Erro ao extrair itens do XML da NF-e: " + e.getMessage(), e);
        }
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