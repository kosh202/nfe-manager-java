package br.com.nfe.manager.service;

import br.com.nfe.manager.model.Produto;
import br.com.nfe.manager.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorCodigo(String codigo) {
        return produtoRepository.findByCodigo(codigo);
    }

    @Transactional
    public Produto obterOuCriar(String codigo, String nome) {
        return produtoRepository.findByCodigo(codigo)
                .map(produtoExistente -> {
                    if (nome != null && !nome.equals(produtoExistente.getNome())) {
                        produtoExistente.setNome(nome);
                        return produtoRepository.save(produtoExistente);
                    }
                    return produtoExistente;
                })
                .orElseGet(() -> produtoRepository.save(new Produto(codigo, nome)));
    }
}
