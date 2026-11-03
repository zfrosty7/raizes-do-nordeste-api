package br.com.raizes.dto;

import br.com.raizes.model.StatusPagamento;
import br.com.raizes.model.StatusPedido;

import java.math.BigDecimal;

/** Resposta do pagamento: o resultado e como ficou o pedido. */
public record PagamentoResponse(Long pagamentoId, Long pedidoId, StatusPagamento status, BigDecimal valor,
                                String referencia, String mensagem, StatusPedido statusPedido) {
}
