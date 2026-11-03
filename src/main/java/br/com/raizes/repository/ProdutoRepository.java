package br.com.raizes.repository;

import br.com.raizes.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByUnidadeId(Long unidadeId);
}
