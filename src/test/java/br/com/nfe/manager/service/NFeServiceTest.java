package br.com.nfe.manager.service;

import br.com.nfe.manager.exception.NFeDuplicadaException;
import br.com.nfe.manager.exception.ResourceNotFoundException;
import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;
import br.com.nfe.manager.repository.NFeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NFeServiceTest {

    @Mock
    private NFeRepository nfeRepository;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private NFeService nfeService;

    private static final String SAMPLE_XML = """
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
                            <cProd>PROD-100</cProd>
                            <xProd>Papel A4</xProd>
                            <qCom>5.0000</qCom>
                            <vUnCom>10.0000</vUnCom>
                            <vProd>50.00</vProd>
                        </prod>
                    </det>
                    <total>
                        <ICMSTot>
                            <vNF>50.00</vNF>
                        </ICMSTot>
                    </total>
                </infNFe>
            </NFe>
            """;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve importar e persistir NF-e válida no banco com sucesso")
    void deveImportarNFeComSucesso() {
        when(nfeRepository.existsByChaveAcesso(anyString())).thenReturn(false);
        when(empresaService.obterOuCriar(anyString(), anyString())).thenReturn(new Empresa("42530613000180", "Empresa Teste Ltda"));
        when(produtoService.obterOuCriar(anyString(), anyString())).thenReturn(new Produto("PROD-100", "Papel A4"));

        NFe nfeMock = new NFe("43211105730928000145650010000002401717268120", "240", OffsetDateTime.now(),
                new Empresa("42530613000180", "Empresa Teste Ltda"), new BigDecimal("50.00"), null);
        when(nfeRepository.save(any(NFe.class))).thenReturn(nfeMock);

        NFe resultado = nfeService.importarXml(SAMPLE_XML);

        assertNotNull(resultado);
        assertEquals("43211105730928000145650010000002401717268120", resultado.getChaveAcesso());
        verify(nfeRepository, times(1)).save(any(NFe.class));
    }

    @Test
    @DisplayName("Deve lançar NFeDuplicadaException ao tentar importar nota fiscal já cadastrada")
    void deveLancarExcecaoParaNotaDuplicada() {
        when(nfeRepository.existsByChaveAcesso("43211105730928000145650010000002401717268120")).thenReturn(true);

        assertThrows(NFeDuplicadaException.class, () -> nfeService.importarXml(SAMPLE_XML));
        verify(nfeRepository, never()).save(any(NFe.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    void deveLancarExcecaoParaIdInexistente() {
        when(nfeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> nfeService.buscarPorId(99L));
    }
}
