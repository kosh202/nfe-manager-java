package br.com.nfe.manager.repository;

import br.com.nfe.manager.model.NFe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NFeRepository extends JpaRepository<NFe, Long>, JpaSpecificationExecutor<NFe> {

    @Override
    @EntityGraph(attributePaths = {"empresa"})
    Page<NFe> findAll(@Nullable Specification<NFe> spec, Pageable pageable);

    Optional<NFe> findByChaveAcesso(String chaveAcesso);
    boolean existsByChaveAcesso(String chaveAcesso);
}
