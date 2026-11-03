package br.com.raizes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tabela PAGAMENTO: cada tentativa de pagamento (simulada) de um pedido. */
@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    private BigDecimal valor;
    private String referencia;
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected Pagamento() {
    }

    public Pagamento(Long pedidoId, FormaPagamento formaPagamento, StatusPagamento status, BigDecimal valor, String referencia) {
        this.pedidoId = pedidoId;
        this.formaPagamento = formaPagamento;
        this.status = status;
        this.valor = valor;
        this.referencia = referencia;
    }

    public Long getId() { return id; }
    public String getReferencia() { return referencia; }
}
