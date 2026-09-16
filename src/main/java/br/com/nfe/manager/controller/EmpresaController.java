package br.com.nfe.manager.controller;

import br.com.nfe.manager.dto.EmpresaDTO;
import br.com.nfe.manager.exception.ResourceNotFoundException;
import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.service.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<EmpresaDTO>> listarTodas() {
        List<Empresa> empresas = empresaService.listarTodas();
        List<EmpresaDTO> dtos = empresas.stream()
                .map(EmpresaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<EmpresaDTO> buscarPorCnpj(@PathVariable String cnpj) {
        Empresa empresa = empresaService.buscarPorCnpj(cnpj)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com CNPJ: " + cnpj));
        return ResponseEntity.ok(EmpresaDTO.fromEntity(empresa));
    }
}
