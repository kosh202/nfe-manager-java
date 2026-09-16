package br.com.nfe.manager.controller;

import br.com.nfe.manager.dto.ProdutoDTO;
import br.com.nfe.manager.exception.ResourceNotFoundException;
import br.com.nfe.manager.model.Produto;
import br.com.nfe.manager.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        List<Produto> produtos = produtoService.listarTodos();
        List<ProdutoDTO> dtos = produtos.stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProdutoDTO> buscarPorCodigo(@PathVariable String codigo) {
        Produto produto = produtoService.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com código: " + codigo));
        return ResponseEntity.ok(ProdutoDTO.fromEntity(produto));
    }
}
