package br.com.raizes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * Tabela PRODUTO. Cada produto pertence a UMA unidade e guarda a quantidade em estoque dela.
 * Assim, cada unidade tem o seu próprio estoque e o seu próprio cardápio.
 */
@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private BigDecimal preco;
    private int quantidade;   // estoque
    private Long unidadeId;   // a que unidade pertence

    protected Produto() {
    }

    public Produto(String nome, BigDecimal preco, int quantidade, Long unidadeId) {
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
        this.unidadeId = unidadeId;
    }

    public void baixarEstoque(int qtd) {
        this.quantidade -= qtd;
    }

    public void devolverEstoque(int qtd) {
        this.quantidade += qtd;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public int getQuantidade() { return quantidade; }
    public Long getUnidadeId() { return unidadeId; }
}
