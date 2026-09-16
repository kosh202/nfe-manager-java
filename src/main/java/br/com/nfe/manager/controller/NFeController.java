package br.com.nfe.manager.controller;

import br.com.nfe.manager.dto.ImportResultDTO;
import br.com.nfe.manager.dto.NFeFilterDTO;
import br.com.nfe.manager.dto.NFeResponseDTO;
import br.com.nfe.manager.exception.XmlParsingException;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.service.NFeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/nfe")
public class NFeController {

    private final NFeService nfeService;

    public NFeController(NFeService nfeService) {
        this.nfeService = nfeService;
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importarXmlMultipart(@RequestParam("file") MultipartFile file) {
        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            NFe nfe = nfeService.importarXml(xmlContent);

            ImportResultDTO result = new ImportResultDTO(
                    "Nota fiscal importada com sucesso.",
                    nfe.getChaveAcesso(),
                    nfe.getId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Falha ao ler o arquivo enviado: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/importar", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<ImportResultDTO> importarXmlBody(@RequestBody(required = false) String xmlContent) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new XmlParsingException("Conteúdo XML não pode ser nulo ou vazio.");
        }
        NFe nfe = nfeService.importarXml(xmlContent);

        ImportResultDTO result = new ImportResultDTO(
                "Nota fiscal importada com sucesso.",
                nfe.getChaveAcesso(),
                nfe.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<Page<NFeResponseDTO>> pesquisar(
            NFeFilterDTO filter,
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {

        Page<NFe> pagina = nfeService.pesquisar(filter, pageable);
        Page<NFeResponseDTO> paginaDTO = pagina.map(NFeResponseDTO::fromEntity);
        return ResponseEntity.ok(paginaDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NFeResponseDTO> buscarPorId(@PathVariable Long id) {
        NFe nfe = nfeService.buscarPorId(id);
        return ResponseEntity.ok(NFeResponseDTO.fromEntity(nfe));
    }

    @GetMapping("/chave/{chaveAcesso}")
    public ResponseEntity<NFeResponseDTO> buscarPorChaveAcesso(@PathVariable String chaveAcesso) {
        NFe nfe = nfeService.buscarPorChaveAcesso(chaveAcesso);
        return ResponseEntity.ok(NFeResponseDTO.fromEntity(nfe));
    }

    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> obterXmlOriginal(@PathVariable Long id) {
        String xmlOriginal = nfeService.obterXmlOriginal(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfe-" + id + ".xml\"")
                .body(xmlOriginal);
    }
}
