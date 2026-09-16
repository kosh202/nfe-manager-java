package br.com.nfe.manager.repository.specification;

import br.com.nfe.manager.Main;
import br.com.nfe.manager.dto.NFeFilterDTO;
import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;
import br.com.nfe.manager.repository.EmpresaRepository;
import br.com.nfe.manager.repository.NFeRepository;
import br.com.nfe.manager.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = Main.class)
class NFeSpecificationTest {

    @Autowired
    private NFeRepository nfeRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Empresa empresa1;
    private Empresa empresa2;
    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void setUp() {
        nfeRepository.deleteAll();
        empresaRepository.deleteAll();
        produtoRepository.deleteAll();

        empresa1 = empresaRepository.save(new Empresa("11111111000111", "Empresa Alfa"));
        empresa2 = empresaRepository.save(new Empresa("22222222000222", "Empresa Beta"));

        produto1 = produtoRepository.save(new Produto("PROD-A", "Caderno 10 Materias"));
        produto2 = produtoRepository.save(new Produto("PROD-B", "Caneta Esferografica"));

        // NF-e 1: Alfa, R$ 100.00, Produto A, Data 2023-01-10
        NFe nfe1 = new NFe("CHAVE-1", "001", OffsetDateTime.parse("2023-01-10T10:00:00-03:00"), empresa1, new BigDecimal("100.00"), null);
        nfe1.addItem(new ItemNFe(produto1, new BigDecimal("2"), new BigDecimal("50.00"), new BigDecimal("100.00")));
        nfeRepository.save(nfe1);

        // NF-e 2: Beta, R$ 250.00, Produto B, Data 2023-02-15
        NFe nfe2 = new NFe("CHAVE-2", "002", OffsetDateTime.parse("2023-02-15T14:30:00-03:00"), empresa2, new BigDecimal("250.00"), null);
        nfe2.addItem(new ItemNFe(produto2, new BigDecimal("5"), new BigDecimal("50.00"), new BigDecimal("250.00")));
        nfeRepository.save(nfe2);

        // NF-e 3: Alfa, R$ 500.00, Produto A + Produto B, Data 2023-03-20
        NFe nfe3 = new NFe("CHAVE-3", "003", OffsetDateTime.parse("2023-03-20T16:00:00-03:00"), empresa1, new BigDecimal("500.00"), null);
        nfe3.addItem(new ItemNFe(produto1, new BigDecimal("5"), new BigDecimal("50.00"), new BigDecimal("250.00")));
        nfe3.addItem(new ItemNFe(produto2, new BigDecimal("5"), new BigDecimal("50.00"), new BigDecimal("250.00")));
        nfeRepository.save(nfe3);
    }

    @Test
    @DisplayName("Deve filtrar por intervalo de datas e faixa de valores combinados")
    void deveFiltrarPorDataEValor() {
        NFeFilterDTO filter = new NFeFilterDTO();
        filter.setDataInicio(OffsetDateTime.parse("2023-01-01T00:00:00-03:00"));
        filter.setDataFim(OffsetDateTime.parse("2023-02-28T23:59:59-03:00"));
        filter.setValorMin(new BigDecimal("200.00"));

        Page<NFe> resultado = nfeRepository.findAll(NFeSpecification.comFiltros(filter), PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertEquals("CHAVE-2", resultado.getContent().get(0).getChaveAcesso());
    }

    @Test
    @DisplayName("Deve filtrar por código exato de produto")
    void deveFiltrarPorCodigoProduto() {
        NFeFilterDTO filter = new NFeFilterDTO();
        filter.setCodigoProduto("PROD-B");

        Page<NFe> resultado = nfeRepository.findAll(NFeSpecification.comFiltros(filter), PageRequest.of(0, 10));

        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve filtrar por nome parcial de produto (case insensitive)")
    void deveFiltrarPorNomeProduto() {
        NFeFilterDTO filter = new NFeFilterDTO();
        filter.setNomeProduto("caderno");

        Page<NFe> resultado = nfeRepository.findAll(NFeSpecification.comFiltros(filter), PageRequest.of(0, 10));

        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve filtrar combinando CNPJ da empresa, faixa de valor e produto")
    void deveFiltrarPorCNPJValorEProduto() {
        NFeFilterDTO filter = new NFeFilterDTO();
        filter.setCnpj("11111111000111");
        filter.setValorMin(new BigDecimal("400.00"));
        filter.setCodigoProduto("PROD-A");

        Page<NFe> resultado = nfeRepository.findAll(NFeSpecification.comFiltros(filter), PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertEquals("CHAVE-3", resultado.getContent().get(0).getChaveAcesso());
    }
}
