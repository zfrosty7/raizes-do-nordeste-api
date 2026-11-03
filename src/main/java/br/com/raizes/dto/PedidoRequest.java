package br.com.raizes.dto;

import br.com.raizes.model.CanalPedido;
import br.com.raizes.model.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Dados para criar um pedido. O cliente vem do token (não precisa ir no JSON). */
public record PedidoRequest(
        @NotNull(message = "O canalPedido é obrigatório (APP, TOTEM, BALCAO, PICKUP ou WEB).")
        CanalPedido canalPedido,

        @NotNull(message = "A unidadeId é obrigatória.")
        Long unidadeId,

        @NotNull(message = "A formaPagamento é obrigatória.")
        FormaPagamento formaPagamento,

        @NotEmpty(message = "O pedido deve ter pelo menos um item.")
        @Valid
        List<Item> itens) {

    /** Um item do pedido: qual produto e quantas unidades. */
    public record Item(
            @NotNull(message = "O produtoId é obrigatório.")
            Long produtoId,

            @NotNull(message = "A quantidade é obrigatória.")
            @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
            Integer quantidade) {
    }
}
