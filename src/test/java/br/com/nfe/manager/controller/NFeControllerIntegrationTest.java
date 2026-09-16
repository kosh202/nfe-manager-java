package br.com.nfe.manager.controller;

import br.com.nfe.manager.Main;
import br.com.nfe.manager.dto.ImportResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class NFeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_XML = """
            <NFe xmlns="http://www.portalfiscal.inf.br/nfe">
                <infNFe versao="4.00" Id="NFe43211105730928000145650010000002401717268120">
                    <ide>
                        <nNF>240</nNF>
                        <dhEmi>2021-11-11T18:56:55-03:00</dhEmi>
                    </ide>
                    <emit>
                        <CNPJ>42530613000180</CNPJ>
                        <xNome>Empresa Integracao Ltda</xNome>
                    </emit>
                    <det nItem="1">
                        <prod>
                            <cProd>PROD-INT-01</cProd>
                            <xProd>Caderno Universitario</xProd>
                            <qCom>2.0000</qCom>
                            <vUnCom>15.0000</vUnCom>
                            <vProd>30.00</vProd>
                        </prod>
                    </det>
                    <total>
                        <ICMSTot>
                            <vNF>30.00</vNF>
                        </ICMSTot>
                    </total>
                </infNFe>
            </NFe>
            """;

    @Test
    @DisplayName("Deve executar o fluxo completo de importação, consulta, filtros e download do XML original")
    void deveExecutarFluxoCompletoDeImportacaoEConsulta() throws Exception {
        // 1. Importar XML via Body application/xml (201 CREATED)
        MvcResult result = mockMvc.perform(post("/api/nfe/importar")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(VALID_XML))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chaveAcesso", is("43211105730928000145650010000002401717268120")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ImportResultDTO importResult = objectMapper.readValue(responseJson, ImportResultDTO.class);
        Long idGerado = importResult.getId();

        // 2. Tentar re-importar a mesma nota (409 CONFLICT)
        mockMvc.perform(post("/api/nfe/importar")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(VALID_XML))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflito de Duplicidade")));

        // 3. Consultar notas fiscais combinando filtros (200 OK)
        mockMvc.perform(get("/api/nfe")
                        .param("cnpj", "42530613000180")
                        .param("codigoProduto", "PROD-INT-01")
                        .param("valorMin", "10.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].numero", is("240")))
                .andExpect(jsonPath("$.content[0].empresa.nome", is("Empresa Integracao Ltda")));

        // 4. Testar download do XML original (200 OK com headers corretos)
        MvcResult xmlResult = mockMvc.perform(get("/api/nfe/" + idGerado + "/xml"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_XML_VALUE)))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, is("attachment; filename=\"nfe-" + idGerado + ".xml\"")))
                .andReturn();

        assertEquals(VALID_XML, xmlResult.getResponse().getContentAsString());

        // 5. Consultar empresas cadastradas
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cnpj", is("42530613000180")));

        // 6. Consultar produtos cadastrados
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo", is("PROD-INT-01")));
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 Bad Request ao enviar XML vazio ou malformatado")
    void deveRetornarErroParaXmlInvalido() throws Exception {
        mockMvc.perform(post("/api/nfe/importar")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Erro no XML")));
    }

    @Test
    @DisplayName("Deve permitir importação via Multipart File upload")
    void deveImportarViaMultipartFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nota.xml",
                MediaType.APPLICATION_XML_VALUE,
                VALID_XML.getBytes()
        );

        mockMvc.perform(multipart("/api/nfe/importar").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chaveAcesso", is("43211105730928000145650010000002401717268120")));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 Not Found ao solicitar XML de nota fiscal inexistente")
    void deveRetornar404ParaXmlInexistente() throws Exception {
        mockMvc.perform(get("/api/nfe/999999/xml"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Não Encontrado")));
    }
}
