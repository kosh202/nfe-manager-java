package br.com.nfe.manager.repository.specification;

import br.com.nfe.manager.dto.NFeFilterDTO;
import br.com.nfe.manager.model.Empresa;
import br.com.nfe.manager.model.ItemNFe;
import br.com.nfe.manager.model.NFe;
import br.com.nfe.manager.model.Produto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NFeSpecification {

    public static Specification<NFe> comFiltros(NFeFilterDTO filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // Filtro por CNPJ da Empresa
            if (filter.getCnpj() != null && !filter.getCnpj().trim().isEmpty()) {
                Join<NFe, Empresa> empresaJoin = root.join("empresa", JoinType.INNER);
                predicates.add(cb.equal(empresaJoin.get("cnpj"), filter.getCnpj().trim()));
            }

            // Filtro por Nome da Empresa (case-insensitive substring)
            if (filter.getNomeEmpresa() != null && !filter.getNomeEmpresa().trim().isEmpty()) {
                Join<NFe, Empresa> empresaJoin = root.join("empresa", JoinType.INNER);
                predicates.add(cb.like(cb.lower(empresaJoin.get("nome")), "%" + filter.getNomeEmpresa().trim().toLowerCase() + "%"));
            }

            // Filtro por Intervalo de Data (Início)
            if (filter.getDataInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), filter.getDataInicio()));
            }

            // Filtro por Intervalo de Data (Fim)
            if (filter.getDataFim() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("data"), filter.getDataFim()));
            }

            // Filtro por Valor Mínimo
            if (filter.getValorMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("valorTotal"), filter.getValorMin()));
            }

            // Filtro por Valor Máximo
            if (filter.getValorMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("valorTotal"), filter.getValorMax()));
            }

            // Filtro por Código ou Nome do Produto
            boolean temCodigoProduto = filter.getCodigoProduto() != null && !filter.getCodigoProduto().trim().isEmpty();
            boolean temNomeProduto = filter.getNomeProduto() != null && !filter.getNomeProduto().trim().isEmpty();

            if (temCodigoProduto || temNomeProduto) {
                Join<NFe, ItemNFe> itemJoin = root.join("itens", JoinType.INNER);
                Join<ItemNFe, Produto> produtoJoin = itemJoin.join("produto", JoinType.INNER);

                if (temCodigoProduto) {
                    predicates.add(cb.equal(produtoJoin.get("codigo"), filter.getCodigoProduto().trim()));
                }

                if (temNomeProduto) {
                    predicates.add(cb.like(cb.lower(produtoJoin.get("nome")), "%" + filter.getNomeProduto().trim().toLowerCase() + "%"));
                }
            }

            // Evita registros duplicados no resultado devido aos joins de itens
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
