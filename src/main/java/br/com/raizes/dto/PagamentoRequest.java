package br.com.raizes.dto;

import br.com.raizes.model.FormaPagamento;
import br.com.raizes.model.StatusPagamento;
import jakarta.validation.constraints.NotNull;

/**
 * Pedido de pagamento. "simularResultado" é opcional e serve só para os testes:
 * APROVADO (padrão) ou RECUSADO.
 */
public record PagamentoRequest(
        @NotNull(message = "A formaPagamento é obrigatória.")
        FormaPagamento formaPagamento,

        StatusPagamento simularResultado) {
}
