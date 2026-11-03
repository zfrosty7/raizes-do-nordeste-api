package br.com.raizes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tabela PEDIDO. Guarda de qual canal veio (canalPedido), a situação (status) e o total. */
@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;
    private Long unidadeId;

    @Enumerated(EnumType.STRING)
    private CanalPedido canalPedido;

    @Enumerated(EnumType.STRING)
    private StatusPedido status = StatusPedido.AGUARDANDO_PAGAMENTO;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private BigDecimal total = BigDecimal.ZERO;
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Um pedido tem vários itens. Ao salvar o pedido, os itens são salvos juntos.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id")
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Long clienteId, Long unidadeId, CanalPedido canalPedido, FormaPagamento formaPagamento) {
        this.clienteId = clienteId;
        this.unidadeId = unidadeId;
        this.canalPedido = canalPedido;
        this.formaPagamento = formaPagamento;
    }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Long getUnidadeId() { return unidadeId; }
    public CanalPedido getCanalPedido() { return canalPedido; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public List<ItemPedido> getItens() { return itens; }
}
