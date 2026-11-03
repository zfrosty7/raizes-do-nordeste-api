package br.com.raizes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/** Tabela ITEM_PEDIDO: um produto dentro de um pedido (o preço fica "congelado" na hora da compra). */
@Entity
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produtoId;
    private int quantidade;
    private BigDecimal precoUnitario;

    protected ItemPedido() {
    }

    public ItemPedido(Long produtoId, int quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getId() { return id; }
    public Long getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
