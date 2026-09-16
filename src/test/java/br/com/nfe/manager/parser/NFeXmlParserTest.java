package br.com.nfe.manager.parser;

import br.com.nfe.manager.exception.XmlParsingException;
import br.com.nfe.manager.model.NFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NFeXmlParserTest {

    private NFeXmlParser parser;

    private static final String VALID_XML = """
            <NFe xmlns="http://www.portalfiscal.inf.br/nfe">
                <infNFe versao="4.00" Id="NFe43211105730928000145650010000002401717268120">
                    <ide>
                        <nNF>240</nNF>
                        <dhEmi>2021-11-11T18:56:55-03:00</dhEmi>
                    </ide>
                    <emit>
                        <CNPJ>42530613000180</CNPJ>
                        <xNome>Empresa Teste Ltda</xNome>
                    </emit>
                    <det nItem="1">
                        <prod>
                            <cProd>PROD-001</cProd>
                            <xProd>Caneta Azul</xProd>
                            <qCom>10.0000</qCom>
                            <vUnCom>2.5000</vUnCom>
                            <vProd>25.00</vProd>
                        </prod>
                    </det>
                    <total>
                        <ICMSTot>
                            <vNF>25.00</vNF>
                        </ICMSTot>
                    </total>
                </infNFe>
            </NFe>
            """;

    private static final String XXE_ATTACK_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
            <NFe xmlns="http://www.portalfiscal.inf.br/nfe">
                <infNFe versao="4.00" Id="NFe43211105730928000145650010000002401717268120">
                    <emit>
                        <CNPJ>42530613000180</CNPJ>
                        <xNome>&xxe;</xNome>
                    </emit>
                </infNFe>
            </NFe>
            """;

    @BeforeEach
    void setUp() {
        parser = new NFeXmlParser();
    }

    @Test
    @DisplayName("Deve extrair com sucesso todos os dados de um XML válido de NF-e")
    void deveExtrairDadosDeXmlValido() {
        Document doc = parser.carregarXmlFromString(VALID_XML);
        NFe nfe = parser.extrairNFe(doc);

        assertNotNull(nfe);
        assertEquals("43211105730928000145650010000002401717268120", nfe.getChaveAcesso());
        assertEquals("240", nfe.getNumero());
        assertEquals("42530613000180", nfe.getEmpresa().getCnpj());
        assertEquals("Empresa Teste Ltda", nfe.getEmpresa().getNome());
        assertEquals(OffsetDateTime.parse("2021-11-11T18:56:55-03:00"), nfe.getData());
        assertEquals(new BigDecimal("25.00"), nfe.getValorTotal());
        assertEquals(1, nfe.getItens().size());
        assertEquals("PROD-001", nfe.getItens().get(0).getProduto().getCodigo());
        assertEquals("Caneta Azul", nfe.getItens().get(0).getProduto().getNome());
    }

    @Test
    @DisplayName("Deve rejeitar DTD e prevenir ataque XXE ao carregar XML com entidade externa")
    void devePrevenirAtaqueXXE() {
        assertThrows(XmlParsingException.class, () -> parser.carregarXmlFromString(XXE_ATTACK_XML));
    }

    @Test
    @DisplayName("Deve lançar XmlParsingException ao tentar processar XML malformatado")
    void deveLancarExcecaoParaXmlInvalido() {
        String xmlInvalido = "<NFe><infNFe>XML Quebrado";
        assertThrows(XmlParsingException.class, () -> parser.carregarXmlFromString(xmlInvalido));
    }
}
