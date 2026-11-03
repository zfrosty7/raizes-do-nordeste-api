package br.com.raizes.service;

import br.com.raizes.exception.ApiException;
import br.com.raizes.model.Produto;
import br.com.raizes.repository.ProdutoRepository;
import br.com.raizes.repository.UnidadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/** O cardápio de uma unidade é a lista de produtos que pertencem a ela. */
@Service
public class CardapioService {

    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;

    public CardapioService(UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository) {
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Produto> cardapioDaUnidade(Long unidadeId) {
        if (!unidadeRepository.existsById(unidadeId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "UNIDADE_NAO_ENCONTRADA", "Unidade não encontrada.");
        }
        return produtoRepository.findByUnidadeId(unidadeId);
    }
}
